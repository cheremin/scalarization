# scalarization #

Project contains my experiments with Escape Analysis and Scalar Replacement optimization
in JVM. It contains set of scenarios and a small framework to run them.

## How to build ##
```
mvn clean package
```

## How to run ##
(Assuming you have `$JAVA_HOME` pointing to java installation you want to test)

```
chmod u+x scripts/run-all.sh
scripts/run-all.sh [results folder]
```
This will run _all_ scenarios under `ru.cheremin.scalarization.scenarios` package, for
all parameters space. Results will be stored in `results` folder, file per scenario.
But this will take quite long: *~10 hours* right now on my laptop.

More targeted run will look like this:
```
$JAVA_HOME/bin/java -Xmx64m -Xms64m -XX:+UseSerialGC -server -XX:+UseSerialGC ...\
		-Dscenario=ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario \
		-Dtarget-directory=results \
		 -jar target/benchmarks.jar
```
This will run specific scenario (`ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario`)
for all parameters space, which is not too big for this scenario, so it takes only
10-15 minutes. If you want to run from IDE, you should use `ru.cheremin.scalarization.ForkingMain`
as main class, with all parameters above

And most targeted run will look like this:
```
$JAVA_HOME/bin/java -Xmx64m -Xms64m -XX:+UseSerialGC -server -XX:+UseSerialGC ...\
		-Dscenario=ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario \
		-Dscenario.use-type=ACCUMULATE_IN_LOOP \
		-Dscenario.size=0 \
 		 -cp target/benchmarks.jar \
 		 ru.cheremin.scalarization.infra.AllocationBenchmarkMain
```
Here I run specific scenario with specific parameters.

Scenario parameters (`-Dscenario.use-type=ACCUMULATE_IN_LOOP -Dscenario.size=0`) are
different for each scenario (`scenario.size` is more or less universal, since used by
many, but not all, scenarios)
