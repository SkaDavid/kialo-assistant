#!/bin/bash

PROJECT_ROOT_DIR=$(dirname $0)/..

cd $PROJECT_ROOT_DIR

build_safety_viz_image() {
  cd ../safety_viz
  docker build . -t safety_viz
  cd -
}



restart_application() {
  docker pull
  docker-compose down
  docker-compose up -d
}


echo "INFO: Pulling changes LKPR distribution configuration ..."
git pull
echo "INFO: Building safety_viz docker image ..."
build_safety_viz_image
echo "INFO: Starting LKPR deployment ..."
restart_application
#echo "INFO: Send notification about sucessfull deployment ..."
#./bin/notify.sh
cd -
