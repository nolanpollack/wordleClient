#!/bin/bash

MY_PATH=$(dirname "$0")

FILE=${MY_PATH}/Other/Client.jar

java -jar $FILE $1 $2