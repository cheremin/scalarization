package ru.cheremin.scalarization.infra;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.base.Predicate;
import com.google.common.io.ByteSink;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.JvmArg.SystemProperty;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static com.google.common.base.Preconditions.checkArgument;
import static ru.cheremin.scalarization.infra.AllocationBenchmarkMain.SCENARIO_CLASS_KEY;

/**
 * @author ruslan
 *         created 31/03/16 at 00:50
 */
public class ScenarioRunner {
	private static final Predicate<JvmArg> AGENT_LIB = new Predicate<JvmArg>() {
		@Override
		public boolean apply( final JvmArg arg ) {
			return arg.name().contains( "agentlib" );
		}
	};

	private final ScenarioRun[] extendedRunArguments;

	private final Class<? extends AllocationScenario> scenarioClass;
	private final ByteSink reportTo;

	public ScenarioRunner( final Class<? extends AllocationScenario> scenarioClass,
	                       final ByteSink reportTo,
	                       final ScenarioRun[] extendedRunArguments ) throws IllegalAccessException, InstantiationException {
		checkArgument( scenarioClass != null, "scenarioClass can't be null" );
		checkArgument( reportTo != null, "reportTo can't be null" );
		//just to check class have 0-arg ctor and can be cast to AllocationScenario
		final AllocationScenario scenario = scenarioClass.newInstance();


		this.scenarioClass = scenarioClass;
		this.reportTo = reportTo;
		this.extendedRunArguments = extendedRunArguments;
	}

	public void run() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException, InterruptedException, ExecutionException {
		final ExecutorService executor = Executors.newFixedThreadPool( 1 /*stdout+stderr*/ );

		try (PrintStream out = new PrintStream( reportTo.openBufferedStream() )) {

			final List<ScenarioRun> scenarioRuns = extractScenarioSpecificArgs( scenarioClass );

			if( scenarioRuns.isEmpty() ) {
				out.printf( "Running %s: x 1 run (@ScenarioRunArgs not found) \n", scenarioClass.getCanonicalName() );
			} else {
				out.printf( "Running %s: x %d runs (@ScenarioRunArgs found) \n",
				            scenarioClass.getCanonicalName(),
				            scenarioRuns.size()
				);
			}

			final JvmProcessBuilder currentJvm = JvmProcessBuilder
					.copyCurrentJvm()
					.appendArgOverriding( new SystemProperty( SCENARIO_CLASS_KEY, scenarioClass.getCanonicalName() ) )
					.removeArg( AGENT_LIB /*can't debug 2 JVMs with same settings */ )
					.withMainClass( AllocationBenchmarkMain.class );


			if( scenarioRuns.isEmpty() ) {
				for( final ScenarioRun extendedRunArgs : extendedRunArguments ) {
					final JvmProcessBuilder jvmWithExtendedArgs = currentJvm.appendArgsOverriding( extendedRunArgs.getJvmArgs() );
					out.println( "Single run with " + extendedRunArgs );
					out.flush();

					final List<String> forkedJvmCmd = jvmWithExtendedArgs.buildJvmCommandLine();
					runProcess( executor, out, forkedJvmCmd );
				}
			} else {
				for( final ScenarioRun extendedRunArgs : extendedRunArguments ) {
					final JvmProcessBuilder currentWithExtendedArgs = currentJvm.appendArgsOverriding( extendedRunArgs.getJvmArgs() );
					for( final ScenarioRun scenarioRun : scenarioRuns ) {
						final JvmProcessBuilder scenarioJvm = currentWithExtendedArgs.appendArgsOverriding( scenarioRun.getJvmArgs() );
						out.println( "Repeating run with " + extendedRunArgs + " x " + scenarioRun );
						out.flush();


						final List<String> forkedJvmCmd = scenarioJvm.buildJvmCommandLine();
						runProcess( executor, out, forkedJvmCmd );
					}
				}
			}
		} finally {
			executor.shutdown();
		}
	}

	private void runProcess( final ExecutorService executor,
	                         final PrintStream out,
	                         final List<String> forkedJvmCmd ) throws IOException, InterruptedException, ExecutionException {
		final Process process = new ProcessBuilder( forkedJvmCmd )
				.redirectErrorStream( true )
				.start();

		final Future<?> future = executor.submit(
				new StreamPumper(
						process.getInputStream(),
						out
				)
		);
		process.waitFor();
		future.get();
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

	public static class StreamPumper implements Runnable {
		private final InputStream source;
		private final OutputStream target;
		private final byte[] buffer = new byte[1024];

		public StreamPumper( final InputStream source,
		                     final OutputStream target ) {
			this.source = source;
			this.target = target;
		}

		@Override
		public void run() {
			try {
				while( true ) {
					final int bytesRead = source.read( buffer );
					if( bytesRead == -1 ) {
						return;
					}
					target.write( buffer, 0, bytesRead );
				}
			} catch( IOException e ) {
				e.printStackTrace();
			}
		}
	}
}
