#!/usr/bin/env bash
#set -x
echo "Starting deployment to EKS..."
# more bash-friendly output for jq
JQ="jq --raw-output --exit-status"

configure_aws_cli(){
  if [ $# != 1 ] ; then
    echo "AWS Region required."
    exit 1;
  fi
  aws --version
  aws configure set default.region $1
  aws configure set default.output json
}

get_version_tag(){
  tag=$(date +"%g %m" | awk '{q=int($2/4)+1; printf("%s%s\n", $1, q);}')
  let month=$(date +%m)
  version=$1
  mq=$((10#$month % 3 == 0 ? 3 : 10#$month % 3))
  tag+="$mq"."$version"
  VERSION_TAG=$tag
}

generate_tags(){
  get_version_tag $GITHUB_RUN_ID

  SHORT_COMMIT_HASH=$(echo $GITHUB_SHA | cut -c1-7)
  RELEASE_TAG=$(echo $GITHUB_RUN_NUMBER | cut -c1-7)
  RELEASE_BRANCH=$(echo "$GITHUB_REF" | sed -r 's/[//\]+/-/g')

  JAVA_VERSION=$(awk -F"'" '/sourceCompatibility/{print $2}' build.gradle)

  echo "tags generated: version=$VERSION_TAG, Short commit=$SHORT_COMMIT_HASH, \
        release tag=$RELEASE_TAG,branch=$RELEASE_BRANCH,java ver=$JAVA_VERSION"
}

docker_tag(){
  echo "docker tagging $1,$2"
  docker tag "$1" "$2"
  if [ $? -ne 0 ] ; then
    echo "docker tag failed. $1:$2"
    exit 1;
  fi
}

docker_push(){
  echo "docker pushing $1,$2"
  docker push "$1"
  if [ $? -ne 0 ] ; then
    echo "docker push failed. $1"
    exit 1;
  fi
}

push_docker_image(){

  if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ] || [ -z "$5" ] ; then
    echo "Docker image name,current repo url,commit hash,version tag,release branch,release tag required."
    echo "image=$1,repo=$2,commit=$3,version=$4,branch=$5,release tag=$6,java ver=$7"
    exit 1;
  fi
  echo "Docker build & push $1 with tags: latest,$3,$4,$5,$6,$7 to $2"

  docker build -t $1 .
  if [ $? -ne 0 ] ; then
    echo "docker build failed. $1"
    exit 1;
  fi

  docker_tag "$1:latest" "$2:latest"
  docker_tag "$1:latest" "$2:$3"
  docker_tag "$1:latest" "$2:$4"
  [ ! -z "$5" ] && docker_tag "$1:latest" "$2:$5"
  [ ! -z "$6" ] && docker_tag "$1:latest" "$2:$6"
  [ ! -z "$7" ] && docker_tag "$1:latest" "$2:$7"

  docker_push "$2:latest"
  docker_push "$2:$3"
  docker_push "$2:$4"
  [ ! -z "$5" ] && docker_push "$2:$5"
  [ ! -z "$6" ] && docker_push "$2:$6"
  [ ! -z "$7" ] && docker_push "$2:$7"

}

push_docker_image_to_artifactory(){
  echo "Logging into to artifactory-$ARTIFACTORY_REPOSITORY_URL : $ARTIFACTORY_USER"
  docker login $ARTIFACTORY_REPOSITORY_URL --username $ARTIFACTORY_USER --password $ARTIFACTORY_TOKEN
  push_docker_image "$@"
}

main(){

  DOCKER_IMAGE_NAME=$ASSET_DOCKER_IMAGE_NAME
  AWS_REGION="us-east-1"

  K8_STATIC_VALUES="$ASSET_CODE/values.yaml"
  K8_TEMP_VALUES="$ASSET_CODE/k8-temp-values.yaml"
  K8_DYNAMIC_VALUES="$ASSET_CODE/k8-values.yaml"

  configure_aws_cli  $AWS_REGION
  generate_tags

  artifactory_repo_url=$ARTIFACTORY_REPOSITORY_URL/$CIRCLE_PROJECT_REPONAME/$DOCKER_IMAGE_NAME
  echo "Docker artifactory URL:$artifactory_repo_url"

  if [ $BUILD_JFROG_IMAGE == true ]; then
    push_docker_image_to_artifactory $DOCKER_IMAGE_NAME $artifactory_repo_url $SHORT_COMMIT_HASH $VERSION_TAG $RELEASE_BRANCH $RELEASE_TAG $JAVA_VERSION
  fi

  echo $SHORT_COMMIT_HASH   #checking if this env var is getting set
  export TAG=$SHORT_COMMIT_HASH
  source ~/.bashrc
  echo "Image Tag is = $TAG"

  if [ $DEPLOY_TO_EKS == true ]; then
    echo "Inside helm install"
    export KUBECONFIG=$HOME/.kube/config
    export HELM_HOST=127.0.0.1:44134
    helm mapkubeapis ${ASSET_CODE} --namespace $EKS_NAMESPACE --kubeconfig=$KUBECONFIG
    envsubst < $K8_TEMP_VALUES >> $K8_DYNAMIC_VALUES
    result=$(eval helm list -n $EKS_NAMESPACE | grep ${ASSET_CODE})
    if [ $? -ne "0" ]; then
      helm install ${ASSET_CODE} -f $K8_STATIC_VALUES -f $K8_DYNAMIC_VALUES --set image.tag=$SHORT_COMMIT_HASH $GITHUB_WORKSPACE/${ASSET_CODE} -n $EKS_NAMESPACE --kubeconfig=$KUBECONFIG
    else
      kubectl delete secret ${ASSET_CODE}-k8-secret -n $EKS_NAMESPACE
      helm upgrade ${ASSET_CODE} -f $K8_STATIC_VALUES -f $K8_DYNAMIC_VALUES --set image.tag=$SHORT_COMMIT_HASH $GITHUB_WORKSPACE/${ASSET_CODE} -n $EKS_NAMESPACE --kubeconfig=$KUBECONFIG
    fi

    if [ $? -eq 1 ]; then
      echo "Error updating service in cluster "; exit 1;
    fi
  fi

  if [ $DEPLOY_TO_EKS == false ]; then
    if [ $DEPLOY_EKS_TEARDOWN == true ]; then
      echo "helm uninstall"
      helm uninstall ${ASSET_CODE} -n $EKS_NAMESPACE --kubeconfig=$KUBECONFIG
      if [ $? -eq 1 ]; then
        echo "Error in uninstalling helm in cluster "; exit 1;
      fi
    fi
  fi

  # if everything is ok, export the REPO to which it was published.
  if [[ ! -e $dir ]]; then
    mkdir -p workspace
    echo "export STUDIO_CI_CURRENT_IMAGE_TAG="$SHORT_COMMIT_HASH"" > workspace/env_exports
    echo "export STUDIO_CI_CURRENT_IMAGE_REPO_URL="$artifactory_repo_url"" >> workspace/env_exports
    echo $(cat workspace/env_exports)
  fi
}
main "$@"
