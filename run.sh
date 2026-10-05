#!/bin/bash
javac -cp "lib/mysql-connector-j-26.7.0/*" -d out src/com/lru/project/*.java
if [ $? -eq 0 ]; then
    java -cp "out:lib/mysql-connector-j-26.7.0/*" com.lru.project.AppMain
fi