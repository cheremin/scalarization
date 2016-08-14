package ru.cheremin.scalarization.infra;

import org.apache.commons.lang3.StringUtils;
import ru.cheremin.scalarization.AllocationScenario;


/**
 * No args, "scenario" class passed with system property (the only required, others
 * optional with reasonable defaults).
 * <p/>
 * Usually called from {@linkplain ru.cheremin.scalarization.ForkingMain}
 *
 * @author ruslan
 *         created 13.11.12 at 22:42
 */
public class AllocationBenchmarkMain {

	public static final String SCENARIO_CLASS_KEY = "scenario";
	public static final String SCENARIO_CLASS_NAME = System.getProperty( SCENARIO_CLASS_KEY );

	public static final int SINGLE_ITERATION_TIME_MS = Integer.getInteger( "duration", 3000 );
	public static final int ITERATIONS = Integer.getInteger( "runs", 12 );

	public static final Formatters FORMATTER = Formatters.valueOf(
			System.getProperty( "formatter", Formatters.FULL.name() )
	);


	public static void main( final String[] args ) throws Exception {
		if( StringUtils.isEmpty( SCENARIO_CLASS_NAME ) ) {
			System.err.println( "-D" + SCENARIO_CLASS_KEY + " must be set to name of scenario class" );
			System.exit( -1 );
		}

		final Class<?> clazz = Class.forName( SCENARIO_CLASS_NAME );
		final AllocationScenario scenario = ( AllocationScenario ) clazz.newInstance();

		System.out.println( "\n>>>>>>>>>>>>> START >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
		//TODO RC: print DoEscapeAnalysis/EliminateAllocations enabled/disabled
		System.out.printf(
				"JDK: %s (%s), OS: '%s' %s arch: %s\n",
				System.getProperty( "java.version" ),
				System.getProperty( "java.vm.version" ),

				System.getProperty( "os.name" ),
				System.getProperty( "os.version" ),
				System.getProperty( "os.arch" )
		);
		System.out.printf(
				"%s: %d runs, %d ms each\n",
				scenario,
				ITERATIONS,
				SINGLE_ITERATION_TIME_MS
		);

		final BenchmarkResults benchmarkResults = AllocationBenchmarkBuilder.forScenario( scenario )
				.withIterations( ITERATIONS )
				.withIterationDurationMs( SINGLE_ITERATION_TIME_MS )
				.run();

		FORMATTER.format( benchmarkResults, System.out );
		System.out.println( "\n<<<<<<<<<<<<<<< END <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" );
	}
}
