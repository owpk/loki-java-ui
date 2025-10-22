#!/bin/bash

rm -rf data
rm -rf logs

cd ./mock-service
./mvnw clean package
cd ..

docker compose down
docker container prune
docker volume prune
docker compose up --build
