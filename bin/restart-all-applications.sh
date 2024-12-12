#!/bin/bash

PROJECT_ROOT_DIR=$(dirname $0)/..

cd $PROJECT_ROOT_DIR


build_safety_viz_image() {
  cd ../safety_viz
  docker build . -t safety_viz
  cd -
}



restart_application() {
  docker-compose pull
  docker-compose down
  docker-compose up -d
}


echo "INFO: Building safety_viz image ..."
build_safety_viz_image
echo "INFO: Starting LKPR deployment ..."
restart_application
cd -
