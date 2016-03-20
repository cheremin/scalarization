package ru.cheremin.scalarization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.cheremin.scalarization.infra.*;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedFlag;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * TODO automatic discovery of all scenarios implements AllocationScenario
 *
 * @author ruslan
 *         created 23/02/16 at 19:41
 */
public class ForkingMain {
	private static final Log log = LogFactory.getLog( ForkingMain.class );

	public static final String SCENARIO_CLASS_NAME = AllocationBenchmarkMain.SCENARIO_CLASS_NAME;


	private static final ScenarioRun[] STATIC_RUN_ARGS = {
			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", true ) ),
			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", false ) )
			//TODO: add -server/-client
	};

	public static void main( final String[] args ) throws Exception {
		final Class<?> clazz = Class.forName( SCENARIO_CLASS_NAME );

		final List<ScenarioRun> scenarioRuns = extractScenarioSpecificArgs( clazz );

		//just to check class have 0-arg ctor and can be cast to AllocationScenario
		final AllocationScenario scenario = ( AllocationScenario ) clazz.newInstance();


		if( scenarioRuns.isEmpty() ) {
			System.out.println( "No @ScenarioRunArgs -> single run" );
		} else {
			System.out.println( "Find @ScenarioRunArgs(" + scenarioRuns.size() + " runs) -> iterating" );
		}

		final JvmProcessBuilder currentJvm = JvmProcessBuilder
				.copyCurrentJvm()
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
