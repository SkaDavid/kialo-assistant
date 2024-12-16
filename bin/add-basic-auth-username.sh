#!/bin/bash

function print_usage() {
          echo "Add USERNAME for basic authorization."
          echo "Usage: "
          echo -e "\t$0 <USERNAME>"
          echo "Examples: "
          echo -e "\t$0 blcham"
}

if [ ! "$#" -eq 1 ]; then
        print_usage
        exit
fi

htpasswd ./nginx/.htpasswd $1

