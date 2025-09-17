#!/bin/sh

# This script prepares the environment and then runs the Java application.

# 1. Transform the DATABASE_URL from postgres:// to jdbc:postgresql://
export JDBC_DATABASE_URL=$(echo $DATABASE_URL | sed 's/postgres:/postgresql:/')

# 2. Launch the Java application, forcefully passing the correct properties
java -Dspring.datasource.url=$JDBC_DATABASE_URL \
     -Dspring.redis.host=$SPRING_REDIS_HOST \
     -Dspring.redis.port=$SPRING_REDIS_PORT \
     -jar app.jar