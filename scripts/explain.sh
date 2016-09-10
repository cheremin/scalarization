
# -XX:CompileCommand="print ru/cheremin/scalarization/lab/$SCENARIO run"
# -XX:BCEATraceLevel=3      (inter-procedural EA)
# Debug JMV options
# -XX:+PrintEscapeAnalysis  (non-product)
# -XX:+Verbose              (non-product)
# -XX:+TraceDeoptimizations (non-product)
# -XX:+ProfileTraps         (non-product)

# JITWatch preset:
# -XX:-PrintInlining
# -XX:+LogCompilation
# -XX:+TraceClassLoading
# -XX:+PrintAssembly
# -XX:LogFile=jit.log



JAVA_OPTS="$JAVA_OPTS -Xmx64m -Xms64m -XX:+UseSerialGC \
-XX:+DoEscapeAnalysis \
-XX:+UnlockDiagnosticVMOptions \
-XX:+LogCompilation -XX:+TraceClassLoading -XX:+PrintAssembly -XX:-PrintInlining \
-XX:LogFile=jit.log"

JAVA_CMD="$JAVA_HOME/bin/java -cp lab/target/benchmarks.jar $JAVA_OPTS"

echo "JAVA_CMD: $JAVA_CMD:"
echo "Explaining $@:"

$JAVA_CMD $@ -XX:CompileCommand="log ru/cheremin/scalarization/lab/misc/PreconditionsScenario run" ru.cheremin.scalarization.infra.AllocationBenchmarkMain
