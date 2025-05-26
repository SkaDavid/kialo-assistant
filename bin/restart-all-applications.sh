#!/bin/bash

PROJECT_ROOT_DIR=$(dirname $0)/..

cd $PROJECT_ROOT_DIR


build_safety_viz_image() {
  cd ../safety_viz
  docker build . -t safety_viz
  cd -
}



restart_application() {
  # workaround to docker-compose pull, it does not work due to safety-performance service
  docker-compose pull nginx auth-server-db auth-server termit-server db-server-proxy annotace-server termit ontographer safety-performance-server s-pipes-engine db-server scipy auth-server-db auth-server termit-server annotace-server db-server
  docker-compose down
  docker-compose up -d
}


echo "INFO: Pulling changes LKPR distribution configuration ..."
git pull
echo "INFO: Building safety_viz docker image ..."
build_safety_viz_image
echo "INFO: Starting LKPR deployment ..."
restart_application
cd -
