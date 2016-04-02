#!/bin/bash

JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_73.jdk/Contents/Home"

# $JAVA_HOME/bin/java -version

JAVA_OPTS="$JAVA_OPTS -Xmx64m -Xms64m -XX:+UseSerialGC -server -XX:+UseSerialGC \
		-Dscenario.auto-discover-in=ru.cheremin.scalarization.scenarios \
		-Dtarget-directory=results1.8"

JAVA_CMD="$JAVA_HOME/bin/java $JAVA_OPTS -jar target/benchmarks.jar"
echo $JAVA_CMD
$JAVA_CMD





