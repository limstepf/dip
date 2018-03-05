#!/bin/bash
app=./app/target/dip-app-1.0.0-SNAPSHOT-jar-with-dependencies.jar

java -jar ${app} "$@"
