#!/usr/bin/env bash

# Install Docker
set -x
VER="17.12.0-ce"
curl -L -o /tmp/docker-$VER.tgz https://download.docker.com/linux/static/stable/x86_64/docker-$VER.tgz
tar -xz -C /tmp -f /tmp/docker-$VER.tgz
sudo mv -f /tmp/docker/* /usr/bin

configure_aws_cli() {

  aws --version
  if [ $1 == "dev" ] || [ $1 == "qa" ]; then
    aws configure set default.aws_access_key_id $QUICKSTART_PROTOTYPE_SECRETS_AWS_ACCESS_KEY_ID_DEV_QA
    aws configure set default.aws_secret_access_key $QUICKSTART_PROTOTYPE_SECRETS_AWS_SECRET_ACCESS_KEY_DEV_QA
  elif [ $1 == "stage" ] || [ $1 == "prod" ]; then
    aws configure set default.aws_access_key_id $QUICKSTART_PROTOTYPE_SECRETS_AWS_ACCESS_KEY_ID_STAGE_PROD
    aws configure set default.aws_secret_access_key $QUICKSTART_PROTOTYPE_SECRETS_AWS_SECRET_ACCESS_KEY_STAGE_PROD
  fi

}

configure_aws_cli "$@"
