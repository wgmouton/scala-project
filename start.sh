#!/usr/bin/env bash

echo "How do you want to run this project? "
IFS=","
OPTIONS=("Run it Docker","Run in local with sbt")
select option in $OPTIONS; do
  case $REPLY in
    1)
      IFS=" "
      docker compose up --build
      break;;
    2)
      IFS=" "
      [ ! -f .env ] || export $(grep -v '^#' .env | xargs)
      sbt run
      break;;
  esac
done