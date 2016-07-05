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
```bash
$JAVA_HOME/bin/java -Xmx64m -Xms64m -XX:+UseSerialGC -server  ...\
		-Dscenario=ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario \
		-Dtarget-directory=results \
		 -jar target/benchmarks.jar
```
This will run specific scenario (`ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario`)
for all parameters space, which is not too big for this scenario, so it takes only
10-15 minutes. If you want to run from IDE, you should use `ru.cheremin.scalarization.ForkingMain`
as main class, with all parameters above

And most targeted run will look like this:
```bash
$JAVA_HOME/bin/java -Xmx64m -Xms64m -XX:+UseSerialGC -server ...\
		-Dscenario=ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario \
		-Dscenario.use-type=ACCUMULATE_IN_LOOP \
		-Dscenario.size=0 \
 		 -cp target/benchmarks.jar \
 		 ru.cheremin.scalarization.infra.AllocationBenchmarkMain
```
Here I run specific scenario with specific parameters.

Scenario parameters (`-Dscenario.use-type=ACCUMULATE_IN_LOOP -Dscenario.size=0`) are
different for each scenario (`scenario.size` is more or less universal, it is used by
many, but not all, scenarios)

## How to create scenario ##
Scenario is a class, which extends `ru.cheremin.scalarization.scenarios.AllocationScenario`.
You can pause at this point in the beginning, and try. Without parameters, there is
little difference between running scenario with `ForkingMain` or `AllocationBenchmarkMain`
runners, so better to run with `AllocationBenchmarkMain`, since it allows you to debug
(`ForkingMain` forks dedicated jvm process for each scenario run, making debugging harder)

Next option is to specify parameters space for scenario. In you scenario class you should
specify
```java
@ScenarioRunArgs
public static List<ScenarioRun> parametersToRunWith() {
	...
}
```
(method name is not important, `@ScenarioRunArgs` annotation is a key). `ScenarioRun`
class contains list of additional JVM options to append/override before run the scenario.
JVM options could be something like `-Dname=value` (`JvmArg.SystemProperty`), or any
other JVM flag/option -- e.g. `-XX:InlineSmallCode=2000` (`JvmArg.JvmExtendedProperty`).
There are bunch of helpers for making list of parameters:
```java
@ScenarioRunArgs
public static List<ScenarioRun> parametersToRunWith() {
	return crossJoin(
			allOf( BUILDER_TYPE_KEY, BuilderType.values() ),
			allOf( SIZE_KEY, 0, 1, 4, 128 ),

			asList(
					new JvmExtendedProperty( "FreqInlineSize", "325" ),
					new JvmExtendedProperty( "FreqInlineSize", "500" )
			)
	);
}
```
(from `EqualsBuilderScenario`). Here I build scenario space as "cube" with builderType
side going though all `BuilderType.values()`, size side going through [0, 1, 4, 128],
and `-XX:FreqInlineSize` going through [325, 500]: `2 * 4 * 2 = 16` runs in total.
If you run your scenario with `ForkingMain`, it will append additional "dimension" to
your parameters space: `-XX:+EliminateAllocations / -XX:-EliminateAllocations` (this
is default "control" to verify is scalar replacement really in charge of cleaned
allocations), so in this case it will be 32 runs in total

As you can see, most parameters are passed in scenario code with system properties,
as this way you could read them into `public static final` fields, which, in turn,
allows JIT to aggressively inline them. Again, look at `EqualsBuilderScenario` for
an example.



