package ru.cheremin.scalarization;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openjdk.jmh.util.Utils;
import ru.cheremin.scalarization.infra.AllocationBenchmarkMain;
import ru.cheremin.scalarization.infra.JvmArg;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedFlag;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
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

		final List<JvmArg> systemPropertiesJvmArgs = convertSystemProperties( System.getProperties() );

		if( scenarioRuns.isEmpty() ) {
			System.out.println( "No @ScenarioRunArgs -> single run" );
		} else {
			System.out.println( "Find @ScenarioRunArgs(" + scenarioRuns.size() + " runs) -> iterating" );
		}

		//TODO RC: make JvmProcessBuilder()

		//TODO RC: move STATIC_RUN_ARGS iteration inside per-scenario params iteration
		// 'cos it's more convenient to have +EA/-EA for same params close to each other
		for( final ScenarioRun staticRun : STATIC_RUN_ARGS ) {
			final List<JvmArg> jvmArgs = appendArgsOverriding(
					systemPropertiesJvmArgs,
					staticRun.getJvmArgs()
			);
			if( scenarioRuns.isEmpty() ) {
				System.out.println( "Single run with " + staticRun );
				final List<String> forkedJvmCmd = buildJvmCommandLine( jvmArgs );
				final Process process = new ProcessBuilder( forkedJvmCmd )
						.inheritIO()
						.start();
				process.waitFor();
			} else {
				for( final ScenarioRun scenarioRun : scenarioRuns ) {
					System.out.println( "Repeating run with " + staticRun + " x " + scenarioRun );
					final List<JvmArg> scenarioJvmArgs = appendArgsOverriding(
							jvmArgs,
							scenarioRun.getJvmArgs()
					);
					final List<String> forkedJvmCmd = buildJvmCommandLine(
							scenarioJvmArgs
					);
					final Process process = new ProcessBuilder( forkedJvmCmd )
							.inheritIO()
							.start();
					process.waitFor();
				}
			}
		}
	}

	private static List<JvmArg> parseJvmArgs( final RuntimeMXBean runtimeMXBean ) {
		final List<String> jvmArgsStrings = runtimeMXBean.getInputArguments();
		return Lists.newArrayList(
				Lists.transform(
						jvmArgsStrings,
						new Function<String, JvmArg>() {
							@Override
							public JvmArg apply( final String jvmArgString ) {
								final JvmArg jvmArg = JvmArg.parse( jvmArgString );
//								System.out.println( jvmArgString + " -> " + jvmArg );
								return jvmArg;
							}
						}
				)
		);
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

	private static List<String> buildJvmCommandLine( final List<JvmArg> jvmArgsToOverride ) {
		final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		final String classPath = runtimeMXBean.getClassPath();

		final List<JvmArg> jvmArgs = parseJvmArgs( runtimeMXBean );

		final List<JvmArg> runWithArgs = appendArgsOverriding( jvmArgs, jvmArgsToOverride );


		final String jvmPath = Utils.getCurrentJvm();

		final ImmutableList.Builder<String> jvmCmdLineBuilder = ImmutableList.<String>builder()
				.add( jvmPath )
				.add( "-cp" )
				.add( classPath );

		for( final JvmArg jvmArg : runWithArgs ) {
			jvmCmdLineBuilder.add( jvmArg.asCommandLineString() );
		}

		final ImmutableList<String> jvmCommandLine = jvmCmdLineBuilder
				.add( AllocationBenchmarkMain.class.getName() )
				.build();
		System.out.println( jvmCommandLine );
		return jvmCommandLine;
	}

	private static List<JvmArg> convertSystemProperties( final Properties systemProperties ) {
		final List<JvmArg> jvmArgs = Lists.newArrayList();
		for( final Map.Entry<Object, Object> entry : systemProperties.entrySet() ) {
			final String propertyName = ( String ) entry.getKey();
			final String propertyValue = ( String ) entry.getValue();
			jvmArgs.add( new JvmArg.SystemProperty( propertyName, propertyValue ) );
		}
		return jvmArgs;
	}

	private static List<JvmArg> appendArgsOverriding(
			final List<JvmArg> sourceArgs,
			final List<JvmArg> argsToAppend ) {
		final ArrayList<JvmArg> modifiableArgsList = Lists.newArrayList( sourceArgs );

		for( final JvmArg argToAppend : argsToAppend ) {
			final String argName = argToAppend.name();
			final JvmArg.Kind argKind = argToAppend.kind();

			for( final Iterator<JvmArg> i = modifiableArgsList.iterator(); i.hasNext(); ) {
				final JvmArg arg = i.next();

				if( argKind == arg.kind()
						&& argName.equals( arg.name() ) ) {
					i.remove();
					break;
				}
			}
			modifiableArgsList.add( argToAppend );
		}
		return modifiableArgsList;
	}


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
