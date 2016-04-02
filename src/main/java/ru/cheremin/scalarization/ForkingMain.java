package ru.cheremin.scalarization;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;
import ru.cheremin.scalarization.infra.AllocationBenchmarkMain;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedFlag;
import ru.cheremin.scalarization.infra.MultiplexingOutputStream;
import ru.cheremin.scalarization.infra.ScenarioRunner;
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
		} else if( SCENARIO_CLASS_NAME != null ) {
			final Class<?> clazz = Class.forName( SCENARIO_CLASS_NAME );
			runScenario( ( Class<AllocationScenario> ) clazz );
		} else {
			System.err.println( "'scenario.auto-discover-in' or 'scenario' must be set" );
			System.exit( -1 );
		}

	}

	private static void runScenario( final Class<? extends AllocationScenario> scenarioClass ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, InterruptedException, ExecutionException {

		final File scenarioOutputFile = new File(
				TARGET_DIRECTORY,
				scenarioClass.getCanonicalName()
		);

		final ScenarioRunner scenarioRunner = new ScenarioRunner(
				scenarioClass,
				new ByteSink() {
					@Override
					public OutputStream openStream() throws IOException {
						final ByteSink fileSink = Files.asByteSink( scenarioOutputFile );
						return new MultiplexingOutputStream(
								System.out,
								fileSink.openStream()
						);
					}
				}
		);

		scenarioRunner.run();
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
}
