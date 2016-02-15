SCENARIO=$1
SIZE=$2

JAVA_CMD="$JAVA_HOME/bin/java -cp ../target/benchmarks.jar -Xmx64m -Xms64m -XX:+UseSerialGC"

echo "Use +EA:"

#-XX:+PrintInlining -XX:CompileCommand="print ru/cheremin/scalarization/scenarios/$SCENARIO allocate"
$JAVA_CMD \
	-XX:+UnlockDiagnosticVMOptions -XX:+DoEscapeAnalysis -XX:BCEATraceLevel=3 \
	-XX:+PrintEscapeAnalysis -XX:+Verbose
	-Dscenario=scenarios.$SCENARIO -Dscenario.size=$SIZE \
	ru.cheremin.scalarization.AllocationBenchmark
