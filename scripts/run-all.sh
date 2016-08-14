#!/bin/bash

echo "Using jdk from JAVA_HOME=$JAVA_HOME"
echo "Output dir $1"
# $JAVA_HOME/bin/java -version

JAVA_OPTS="$JAVA_OPTS -Xmx64m -Xms64m -XX:+UseSerialGC -server -XX:+UseSerialGC \
		-Dscenario.auto-discover-in=ru.cheremin.scalarization.lab \
		-Dtarget-directory=$1"

JAVA_CMD="$JAVA_HOME/bin/java $JAVA_OPTS -jar target/benchmarks.jar"
#echo $JAVA_CMD
$JAVA_CMD





