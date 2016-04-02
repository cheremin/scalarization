SCENARIO=$1
SIZE=$2

#-XX:+PrintInlining -XX:CompileCommand="print ru/cheremin/scalarization/scenarios/$SCENARIO run"
JAVA_OPTS="$JAVA_OPTS \
	-Xmx64m -Xms64m -XX:+UseSerialGC -XX:+UnlockDiagnosticVMOptions \
	-XX:+DoEscapeAnalysis -XX:BCEATraceLevel=3 -XX:+PrintEscapeAnalysis -XX:+Verbose"

JAVA_CMD="$JAVA_HOME/bin/java -cp ../target/benchmarks.jar $JAVA_OPTS"

echo "JAVA_CMD: $JAVA_CMD:"
echo "Explaining $SCENARIO:"


$JAVA_CMD \
	-Dscenario=scenarios.$SCENARIO -Dscenario.size=$SIZE \
	ru.cheremin.scalarization.infra.AllocationBenchmarkMain
