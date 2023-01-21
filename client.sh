#!/bin/bash

MY_PATH=$(dirname "$0")

FILE=${MY_PATH}/target/wordleClient-1.0-SNAPSHOT-jar-with-dependencies.jar

java -jar $FILE $1 $2