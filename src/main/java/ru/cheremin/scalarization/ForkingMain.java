package ru.cheremin.scalarization;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;
import ru.cheremin.scalarization.infra.AllocationBenchmarkMain;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedFlag;
import ru.cheremin.scalarization.infra.JvmArg.SystemProperty;
import ru.cheremin.scalarization.infra.JvmProcessBuilder;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * @author ruslan
 *         created 23/02/16 at 19:41
 */
public class ForkingMain {
	private static final Log log = LogFactory.getLog( ForkingMain.class );

	public static final String SCENARIO_CLASS_NAME = AllocationBenchmarkMain.SCENARIO_CLASS_NAME;
	public static final String AUTODISCOVER_ALL_SCENARIOS_IN = System.getProperty( "scenario.auto-discover-in", null );

	public static final File TARGET_DIRECTORY = new File( System.getProperty( "target-directory", "results" ) );


	private static final ScenarioRun[] STATIC_RUN_ARGS = {
			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", true ) ),
			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", false ) )
			//TODO: add -server/-client?
	};

	public static void main( final String[] args ) throws Exception {
		if( !TARGET_DIRECTORY.exists() ) {
			TARGET_DIRECTORY.mkdirs();
		}

		if( AUTODISCOVER_ALL_SCENARIOS_IN != null ) {
			System.out.printf( "Auto-discovering scenarios in '" + AUTODISCOVER_ALL_SCENARIOS_IN + "': \n" );

			final List<Class<? extends AllocationScenario>> allocationScenarioClasses = lookupScenarios( AUTODISCOVER_ALL_SCENARIOS_IN );
			//TODO RC: print ScenarioRuns count right after class names here
			System.out.printf( "Found scenarios: \n" + Joiner.on( "\n" ).join( allocationScenarioClasses ) + " \n\n" );

			for( final Class<? extends AllocationScenario> allocationScenarioClass : allocationScenarioClasses ) {
				if( !Modifier.isAbstract( allocationScenarioClass.getModifiers() ) ) {
					runScenario( allocationScenarioClass );
				}
			}
		} else {
			final Class<?> clazz = Class.forName( SCENARIO_CLASS_NAME );
			runScenario( ( Class<AllocationScenario> ) clazz );
		}

	}

	private static void runScenario( final Class<? extends AllocationScenario> scenarioClass ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, InterruptedException {
		//just to check class have 0-arg ctor and can be cast to AllocationScenario
		final AllocationScenario scenario = scenarioClass.newInstance();


		final List<ScenarioRun> scenarioRuns = extractScenarioSpecificArgs( scenarioClass );

		if( scenarioRuns.isEmpty() ) {
			System.out.printf( "Running %s: x 1 run (@ScenarioRunArgs not found) \n", scenarioClass.getCanonicalName() );
		} else {
			System.out.printf( "Running %s: x %d runs (@ScenarioRunArgs found) \n",
			                   scenarioClass.getCanonicalName(),
			                   scenarioRuns.size()
			);
		}

		final JvmProcessBuilder currentJvm = JvmProcessBuilder
				.copyCurrentJvm()
				.appendArgOverriding( new SystemProperty( AllocationBenchmarkMain.SCENARIO_CLASS_KEY, scenarioClass.getCanonicalName() ) )
				.withMainClass( AllocationBenchmarkMain.class );


		// redirect output into file <ClassName>.result
		final File scenarioOutputFile = new File(
				TARGET_DIRECTORY,
				scenarioClass.getCanonicalName()
		);
		//TODO RC: it's better to duplicate output to console and to file
		//      also it worth to append run params to file, because not all of them
		//      (e.g. extended JVM args) are now available inside
		if( scenarioRuns.isEmpty() ) {
			for( final ScenarioRun staticRun : STATIC_RUN_ARGS ) {
				final JvmProcessBuilder jvmWithStaticArgs = currentJvm.appendArgsOverriding( staticRun.getJvmArgs() );
				System.out.println( "Single run with " + staticRun );
				final List<String> forkedJvmCmd = jvmWithStaticArgs.buildJvmCommandLine();
				final Process process = new ProcessBuilder( forkedJvmCmd )
//						.redirectOutput( appendTo( scenarioOutputFile ) )
						.inheritIO()
						.start();
				process.waitFor();
			}
		} else {
			for( final ScenarioRun scenarioRun : scenarioRuns ) {
				final JvmProcessBuilder scenarioJvm = currentJvm.appendArgsOverriding( scenarioRun.getJvmArgs() );
				for( final ScenarioRun staticRun : STATIC_RUN_ARGS ) {
					System.out.println( "Repeating run with " + staticRun + " x " + scenarioRun );
					final JvmProcessBuilder scenarioStaticJvm = scenarioJvm.appendArgsOverriding( staticRun.getJvmArgs() );

					final List<String> forkedJvmCmd = scenarioStaticJvm.buildJvmCommandLine();
					final Process process = new ProcessBuilder( forkedJvmCmd )
//							.redirectOutput( appendTo( scenarioOutputFile ) )
							.inheritIO()
							.start();
					process.waitFor();
				}
			}
		}
	}

	private static List<Class<? extends AllocationScenario>> lookupScenarios( final String autodiscoverAllScenariosIn ) {
		final Reflections reflections = new Reflections( autodiscoverAllScenariosIn );
		final List<Class<? extends AllocationScenario>> allocationScenarioClasses =
				Lists.newArrayList( reflections.getSubTypesOf( AllocationScenario.class ) );
		//sort to have stable order
		Collections.sort( allocationScenarioClasses, new Ordering<Class<? extends AllocationScenario>>() {
			@Override
			public int compare( final Class<? extends AllocationScenario> left,
			                    final Class<? extends AllocationScenario> right ) {
				return left.getCanonicalName().compareTo( right.getCanonicalName() );
			}
		} );
		return allocationScenarioClasses;
	}

	private static List<ScenarioRun> extractScenarioSpecificArgs( final Class<?> clazz ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		for( final Method method : clazz.getMethods() ) {
			if( method.getAnnotation( ScenarioRunArgs.class ) != null
					&& Modifier.isStatic( method.getModifiers() )
					&& Modifier.isPublic( method.getModifiers() )
					&& method.getParameterTypes().length == 0
					&& List.class.isAssignableFrom( method.getReturnType() ) ) {
				return ( List<ScenarioRun> ) method.invoke( null );
			}

		}
		return Collections.EMPTY_LIST;
	}

}
