#!/bin/bash

PROJECT_ROOT_DIR=$(dirname $0)/..

cd $PROJECT_ROOT_DIR


restart_application() {
docker-compose pull
docker-compose down
docker-compose up -d
}


echo "INFO: Starting LKPR deployment ..."
restart_application
cd -
