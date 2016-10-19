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
import org.reflections.Reflections;
import ru.cheremin.scalarization.infra.AllocationBenchmarkMain;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedFlag;
import ru.cheremin.scalarization.infra.internal.MultiplexingOutputStream;
import ru.cheremin.scalarization.infra.internal.ScenarioRunner;

/**
 * @author ruslan
 *         created 23/02/16 at 19:41
 */
public class ForkingMain {
	public static final String SCENARIO_CLASS_NAME = AllocationBenchmarkMain.SCENARIO_CLASS_NAME;
	public static final String AUTODISCOVER_ALL_SCENARIOS_IN = System.getProperty( "scenario.auto-discover-in", null );

	public static final File TARGET_DIRECTORY = new File( System.getProperty( "target-directory", "results" ) );


	private static final ScenarioRun[] EXTENDED_RUN_PARAMETERS = {
//			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", true ) ),
//			new ScenarioRun( new JvmExtendedFlag( "DoEscapeAnalysis", false ) )

new ScenarioRun( new JvmExtendedFlag( "EliminateAllocations", true ) ),
new ScenarioRun( new JvmExtendedFlag( "EliminateAllocations", false ) )
//TODO: add -server/-client?
	};

	/**
	 * Parameters are passed via system properties:
	 * <p/>
	 * -Dscenario = [className extends AllocationScenario] -- profile single class
	 * <p/>
	 * -Dscenario.auto-discover-in = [class name pattern]  -- profile all lab
	 * matched with pattern
	 * <p/>
	 * -Dtarget-directory = [path]                         -- store results in files
	 * in the directory. Files are created one-per-scenario. (Output also duplicated
	 * to stdout)
	 * <p/>
	 * All parameters passed to JVM will be copied and used to start forked JVM
	 */
	public static void main( final String[] args ) throws Exception {
		if( !TARGET_DIRECTORY.exists() ) {
			TARGET_DIRECTORY.mkdirs();
		}

		if( AUTODISCOVER_ALL_SCENARIOS_IN != null ) {
			System.out.printf( "Auto-discovering lab in '" + AUTODISCOVER_ALL_SCENARIOS_IN + "': \n" );

			final List<Class<? extends Scenario>> allocationScenarioClasses = lookupScenarios( AUTODISCOVER_ALL_SCENARIOS_IN );
			//TODO RC: print ScenarioRuns count right after class names here
			System.out.printf( "Found scenario: \n" + Joiner.on( "\n" ).join( allocationScenarioClasses ) + " \n\n" );

			for( final Class<? extends Scenario> allocationScenarioClass : allocationScenarioClasses ) {
				if( !Modifier.isAbstract( allocationScenarioClass.getModifiers() ) ) {
					runScenario( allocationScenarioClass );
				}
			}
		} else if( SCENARIO_CLASS_NAME != null ) {
			final Class<?> clazz = Class.forName( SCENARIO_CLASS_NAME );
			runScenario( ( Class<? extends Scenario> ) clazz );
		} else {
			System.err.println( "'scenario.auto-discover-in' or 'scenario' must be set" );
			System.exit( -1 );
		}

	}

	private static void runScenario( final Class<? extends Scenario> scenarioClass ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, InterruptedException, ExecutionException {

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
				},
				EXTENDED_RUN_PARAMETERS
		);

		scenarioRunner.run();
	}

	@SuppressWarnings( "unchecked" )
	private static List<Class<? extends Scenario>> lookupScenarios( final String autodiscoverAllScenariosIn ) {
		final Reflections reflections = new Reflections( autodiscoverAllScenariosIn );

		//TODO RC: lookup AllocationScenario instead of plain Scenario because it seems
		//         Reflections lookup don't account for deep inheritance. Dirty fixed now
		final List<Class<? extends AllocationScenario>> allocationScenarioClasses =
				Lists.newArrayList( reflections.getSubTypesOf( AllocationScenario.class ) );

		//sort just to have stable order
		Collections.sort( allocationScenarioClasses, new Ordering<Class<? extends Scenario>>() {
			@Override
			public int compare( final Class<? extends Scenario> left,
			                    final Class<? extends Scenario> right ) {
				return left.getCanonicalName().compareTo( right.getCanonicalName() );
			}
		} );
		return (List)allocationScenarioClasses;
	}
}
