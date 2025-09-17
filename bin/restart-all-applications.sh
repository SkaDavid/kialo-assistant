#!/bin/bash

PROJECT_ROOT_DIR=$(dirname $0)/..

cd $PROJECT_ROOT_DIR

restart_application() {
  docker compose pull
  docker compose down
  docker compose up -d
}


echo "INFO: Pulling changes LKPR distribution configuration ..."
git pull
echo "INFO: Starting LKPR deployment ..."
restart_application
#echo "INFO: Send notification about sucessfull deployment ..."
#./bin/notify.sh
cd -
