SCENARIO=$1
SIZE=$2

$JAVA_HOME/bin/java -version

JAVA_OPTS="$JAVA_OPTS -Xmx64m -Xms64m -XX:+UseSerialGC"

JAVA_CMD="$JAVA_HOME/bin/java -cp ../target/benchmarks.jar $JAVA_OPTS -Dscenario=scenarios.$SCENARIO -Dscenario.size=$SIZE"

#echo $JAVA_CMD

echo "Use +EA:"

$JAVA_CMD -XX:+DoEscapeAnalysis ru.cheremin.scalarization.AllocationBenchmark

echo "Use -EA:"

$JAVA_CMD -XX:-DoEscapeAnalysis ru.cheremin.scalarization.AllocationBenchmark