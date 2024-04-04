#!/usr/bin/env bash
#set -x
echo "Starting deployment to ECR..."
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

get_current_task_definition(){
  if [ $# != 1 ] ; then
    echo "Task definition family required."
    exit 1;
  fi
  echo "Getting current task definition for family $1."
  CURRENT_TASK_DEF=$(aws ecs describe-task-definition --task-definition $1)

  #Remove the quotes and the last part after the : which is the image tag
  CURRENT_IMAGE_REPO_URL=$(echo $CURRENT_TASK_DEF \
                        | $JQ '.taskDefinition.containerDefinitions' \
                        | $JQ  '.[0].image' | sed 's/"//g' | cut -d: -f-1)
  CONTAINER_NAME=$(echo $CURRENT_TASK_DEF | $JQ '.taskDefinition.containerDefinitions[0].name')
  CONTAINER_PORT=$(echo $CURRENT_TASK_DEF | $JQ '.taskDefinition.containerDefinitions[0].portMappings[0].containerPort')
  if [[ -z "$CURRENT_IMAGE_REPO_URL" ]]; then
    echo "Error: Could not extract the CURRENT_IMAGE_REPO_URL from the task definition"; exit 1;
  fi
}

get_version_tag(){
  #let tag=$(date +%g%q) does not work in circleci/python:2.7.13 image
  tag=$(date +"%g %m" | awk '{q=int($2/4)+1; printf("%s%s\n", $1, q);}')
  #let tag=$(date +%g%q)
  let month=$(date +%m)
  #let prev_release=$1
  #release=$(($prev_release+1))
  version=$1
  mq=$((10#$month % 3 == 0 ? 3 : 10#$month % 3))
  tag+="$mq"."$version"
  VERSION_TAG=$tag
}

generate_tags(){
  get_version_tag $CIRCLE_BUILD_NUM

  SHORT_COMMIT_HASH=$(echo $CIRCLE_SHA1 | cut -c1-7)
  RELEASE_TAG=$(echo $CIRCLE_TAG | cut -c1-7)
  RELEASE_BRANCH=$(echo "$CIRCLE_BRANCH" | sed -r 's/[//\]+/-/g')

  PACKAGE_VERSION=$(sed -nE 's/^\s*"version": "(.*?)",$/\1/p' package.json)

  echo "tags generated: version=$VERSION_TAG, Short commit=$SHORT_COMMIT_HASH, \
        release tag=$RELEASE_TAG,branch=$RELEASE_BRANCH,package ver=$PACKAGE_VERSION"
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
    echo "image=$1,repo=$2,commit=$3,version=$4,branch=$5,release tag=$6,package ver=$7"
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

register_new_task_definition() {
  if [ $# != 4 ] ; then
    echo "Task family,current repo url,commit hash,task definition required."
    exit 1;
  fi
  local new_image=$2:$3
  local jq_replace_image_str=$(echo ".[0].image=\"$new_image\"")
  if [[ $SERVICE_DISCOVERY == true ]]; then
    task_network_mode="awsvpc"
  else
    task_network_mode="bridge"
  fi
  echo "Creating new task definition with $new_image for family $1"
  new_task_def=$(echo $4 \
                | $JQ '.taskDefinition.containerDefinitions' \
                | $JQ $jq_replace_image_str )

  task_role_arn=$(echo $4 \
                | $JQ '.taskDefinition.taskRoleArn')
  execution_role_arn=$(echo $4 \
                | $JQ '.taskDefinition.executionRoleArn')

  if [ -z $execution_role_arn ]; then
    echo "executionRoleArn not found. Cannot deploy from private repository without executionRoleArn"
    exit 0;
  fi

  TASK_REVISON_ARN=$(aws ecs register-task-definition \
                      --container-definitions "$new_task_def" \
                      --family $1 \
                      --network-mode $task_network_mode \
                      --task-role-arn $task_role_arn \
                      --execution-role-arn $execution_role_arn \
                      | $JQ '.taskDefinition.taskDefinitionArn')

  echo "New task registered:$TASK_REVISON_ARN"
}

update_service() {
  if [ $# != 3 ] ; then
      echo "Cluster name, service name, task definition revision arn required"
      exit 1;
  fi

  echo "Fetching service registry in cluster $1 and service $2 with $3"
  SERVICE_INFO=$(aws ecs describe-services --cluster $1 --services $2)
  # Get Service Discovery Registry ARN
  SERVICE_REGISTRY=$(echo $SERVICE_INFO | $JQ '.services[0].serviceRegistries[0]')
  REGISTRY_ARN=$(echo $SERVICE_REGISTRY | $JQ '.registryArn')

  #Get network configuration of existing service
  ECS_SUBNETS=$(echo $SERVICE_INFO | $JQ '.services[0].networkConfiguration.awsvpcConfiguration.subnets')
  ECS_SECURITY_GROUP=$(echo $SERVICE_INFO | $JQ '.services[0].networkConfiguration.awsvpcConfiguration.securityGroups')

  if [ "$SERVICE_REGISTRY" != "null" ]; then
    echo "Updating service in cluster $1 and service $2 with $3 service registry $REGISTRY_ARN container name $CONTAINER_NAME and container port $CONTAINER_PORT"
    if [[ $(aws ecs update-service --cluster $1 \
                --service $2 \
                --service-registries registryArn=$REGISTRY_ARN,port=$CONTAINER_PORT \
                --network-configuration "awsvpcConfiguration={subnets=$ECS_SUBNETS,securityGroups=$ECS_SECURITY_GROUP,assignPublicIp='DISABLED'}" \
                --task-definition $3 | \
        $JQ '.service.taskDefinition') != $3 ]]; then
        return 1
    fi
  else
    if [[ $(aws ecs update-service --cluster $1 \
                --service $2 \
                --task-definition $3 | \
        $JQ '.service.taskDefinition') != $3 ]]; then
        return 1
    fi

  fi

}

main(){

  if [ -z $1 ]; then
    echo "Deploy requires environment name (dev,qa,demo etc)"; exit 1;
  fi
  DOCKER_IMAGE_NAME=$ASSET_DOCKER_IMAGE_NAME

  if [ $1 == "dev" ]; then
    generate_tags

    artifactory_repo_url=$ARTIFACTORY_REPOSITORY_URL/$CIRCLE_PROJECT_REPONAME/$DOCKER_IMAGE_NAME
    echo "Docker artifactory URL:$artifactory_repo_url"
    if [ $BUILD_JFROG_IMAGE == true ]; then
      push_docker_image_to_artifactory $DOCKER_IMAGE_NAME $artifactory_repo_url $SHORT_COMMIT_HASH $VERSION_TAG $RELEASE_BRANCH $RELEASE_TAG $PACKAGE_VERSION
    fi
  else
    CURRENT_IMAGE_REPO_URL=$2
    SHORT_COMMIT_HASH=$3
  fi

  if [ $1 != "dev" ]; then
    if [[ -z "$2" || -z "$3" ]]; then
      echo "Error: For environment $1 image repo Url($2) and tag($3) is required."; exit 1;
    fi
  fi

  if [ $DEPLOY_TO_ECS == true ]; then
    AWS_REGION=$AWS_REGION
    if [ $1 == "dev" ] || [ $1 == "qa" ] || [ $1 == "stage" ] || [ $1 == "prod" ]; then
      CLUSTER=$DEPLOY_CLUSTER
      SERVICE=$ASSET_ECS_SERVICE_NAME
      TASK_FAMILY=$ASSET_ECS_TASK_FAMILY
    else
      echo "Undefined environment:$1"; exit 1;
    fi

    configure_aws_cli  $AWS_REGION

    get_current_task_definition $TASK_FAMILY
    echo  "Current Task ECS Repo:$CURRENT_IMAGE_REPO_URL"


    register_new_task_definition $TASK_FAMILY $CURRENT_IMAGE_REPO_URL $SHORT_COMMIT_HASH "$CURRENT_TASK_DEF"
    if [[ -z "$TASK_REVISON_ARN" ]]; then
      echo "Error: Could not register task definition"; exit 1;
    fi

    update_service $CLUSTER $SERVICE $TASK_REVISON_ARN
    if [ $? -eq 1 ]; then
      echo "Error updating service in cluster $CLUSTER and service $SERVICE with $TASK_REVISON_ARN"; exit 1; 
    fi
  fi

  # if everything is ok, export the REPO to which it was published.
  if [[ ! -e $dir ]]; then
    mkdir -p workspace
    echo "export STUDIO_CI_CURRENT_IMAGE_TAG="$SHORT_COMMIT_HASH"" > workspace/env_exports
    echo "export STUDIO_CI_CURRENT_IMAGE_REPO_URL="$CURRENT_IMAGE_REPO_URL"" >> workspace/env_exports
    echo $(cat workspace/env_exports)
  fi
}
main "$@"
