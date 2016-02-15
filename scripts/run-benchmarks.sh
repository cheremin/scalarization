SCENARIO=$1
SIZE=$2

JAVA_CMD="$JAVA_HOME/bin/java -cp ../target/benchmarks.jar -Xmx64m -Xms64m -XX:+UseSerialGC"

echo "Use +EA:"

$JAVA_CMD -XX:+DoEscapeAnalysis -Dscenario=scenarios.$SCENARIO -Dscenario.size=$SIZE \
	ru.cheremin.scalarization.AllocationBenchmark

echo "Use -EA:"

$JAVA_CMD -XX:-DoEscapeAnalysis -Dscenario=scenarios.$SCENARIO -Dscenario.size=$SIZE \
	ru.cheremin.scalarization.AllocationBenchmark