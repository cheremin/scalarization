package ru.cheremin.scalarization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;
import ru.cheremin.scalarization.infra.*;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedFlag;
import ru.cheremin.scalarization.infra.JvmArg.SystemProperty;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * @author ruslan
 *         created 23/02/16 at 19:41
 */
public class ForkingMain {
	private static final Log log = LogFactory.getLog( ForkingMain.class );

	public static final String SCENARIO_CLASS_NAME = AllocationBenchmarkMain.SCENARIO_CLASS_NAME;
	public static final String AUTODISCOVER_ALL_SCENARIOS_IN = System.getProperty( "scenario.auto-discover-in", null );


	private static final ScenarioRun[] STATIC_RUN_ARGS = {
			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", true ) ),
			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", false ) )
			//TODO: add -server/-client
	};

	public static void main( final String[] args ) throws Exception {
		if( AUTODISCOVER_ALL_SCENARIOS_IN != null ) {
			System.out.printf( "Auto-discovering scenarios in '" + AUTODISCOVER_ALL_SCENARIOS_IN + "': \n" );

			final Reflections reflections = new Reflections( AUTODISCOVER_ALL_SCENARIOS_IN );
			final Set<Class<? extends AllocationScenario>> allocationScenarioClasses = reflections.getSubTypesOf( AllocationScenario.class );
			System.out.printf( "Found: " + Joiner.on( "\n" ).join( allocationScenarioClasses ) + " \n\n" );

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

		System.out.printf( "Running " + scenarioClass.getCanonicalName() + " \n" );

		final List<ScenarioRun> scenarioRuns = extractScenarioSpecificArgs( scenarioClass );

		if( scenarioRuns.isEmpty() ) {
			System.out.println( "No @ScenarioRunArgs -> single run" );
		} else {
			System.out.println( "Find @ScenarioRunArgs(" + scenarioRuns.size() + " runs) -> iterating" );
		}

		final JvmProcessBuilder currentJvm = JvmProcessBuilder
				.copyCurrentJvm()
				.appendArgOverriding( new SystemProperty( AllocationBenchmarkMain.SCENARIO_CLASS_KEY, scenarioClass.getCanonicalName() ) )
				.withMainClass( AllocationBenchmarkMain.class );


		if( scenarioRuns.isEmpty() ) {
			for( final ScenarioRun staticRun : STATIC_RUN_ARGS ) {
				final JvmProcessBuilder jvmWithStaticArgs = currentJvm.appendArgsOverriding( staticRun.getJvmArgs() );
				System.out.println( "Single run with " + staticRun );
				final List<String> forkedJvmCmd = jvmWithStaticArgs.buildJvmCommandLine();
				final Process process = new ProcessBuilder( forkedJvmCmd )
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
							.inheritIO()
							.start();
					process.waitFor();
				}
			}
		}
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

	/**
	 * List of additional jvm args to run specific scenario with.
	 */
	public static class ScenarioRun {
		private final List<JvmArg> jvmArgs;

		public ScenarioRun( final List<JvmArg> jvmArgs ) {
			this.jvmArgs = Lists.newArrayList( jvmArgs );
		}

		public ScenarioRun( final JvmArg... jvmArgs ) {
			this.jvmArgs = Lists.newArrayList( jvmArgs );
		}

		public List<JvmArg> getJvmArgs() {
			return jvmArgs;
		}

		@Override
		public String toString() {
			return jvmArgs.toString();
		}
	}
}
