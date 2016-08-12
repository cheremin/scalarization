package ru.cheremin.scalarization.infra;

import java.lang.management.ManagementFactory;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.ThreadMXBean;
import org.apache.commons.lang3.StringUtils;
import ru.cheremin.scalarization.scenarios.AllocationScenario;


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

	public static final int ITERATIONS_IN_BATCH = Integer.getInteger( "iterations-in-batch", 1024 );
	public static final int SINGLE_BENCHMARK_TIME_MS = Integer.getInteger( "duration", 3000 );
	public static final int RUNS = Integer.getInteger( "runs", 12 );

	public static final boolean CSV_OUTPUT = Boolean.getBoolean( "output.csv" );

	public static final Formatters FORMATTER = Formatters.valueOf(
			System.getProperty( "formatter", Formatters.FULL.name() )
	);

	/**
	 * Bytes allocated in {@linkplain ThreadMXBean#getThreadAllocatedBytes(long)} for
	 * 2 long[1] arrays. Exact value can be in range [48..64], depends on arch(32/64)
	 * and compressedOops usage, so I use max
	 */
	public static final int INFRASTRUCTURE_ALLOCATION_BYTES = 64;


	public static void main( final String[] args ) throws Exception {
		if( StringUtils.isEmpty( SCENARIO_CLASS_NAME ) ) {
			System.err.println( "-D" + SCENARIO_CLASS_KEY + " must be set to name of scenario class" );
			System.exit( -1 );
		}

		final Class<?> clazz = Class.forName( SCENARIO_CLASS_NAME );
		final AllocationScenario scenario = ( AllocationScenario ) clazz.newInstance();

		System.out.println( "\n>>>>>>>>>>>>> START >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
		//TODO RC: print DoEscapeAnalysis enabled/disabled
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
				RUNS,
				SINGLE_BENCHMARK_TIME_MS
		);

		final BenchmarkResult[] benchmarkResults = runBenchmarkSeries( scenario, RUNS, SINGLE_BENCHMARK_TIME_MS );

		//print results
		printResults( benchmarkResults );
		System.out.println( "\n<<<<<<<<<<<<<<< END <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" );
	}

	public static BenchmarkResult[] runBenchmarkSeries( final AllocationScenario scenario,
	                                                    final int runs,
	                                                    final long singleSeriesTimeMs ) {
		final BenchmarkResult[] benchmarkResults = new BenchmarkResult[runs];
		for( int i = 0; i < benchmarkResults.length; i++ ) {
			benchmarkResults[i] = new BenchmarkResult();
		}
		System.gc();

		final AllocationMonitor allocationMonitor = new AllocationMonitor( Thread.currentThread() );
		allocationMonitor.storeCurrentStamps();
		for( int i = 0; i < runs; i++ ) {
			final BenchmarkResult benchmarkResult = benchmarkResults[i];

			runBenchmark( scenario, benchmarkResult, singleSeriesTimeMs );

			allocationMonitor.updateStampsAndStoreDifference( benchmarkResult );
		}
		return benchmarkResults;
	}

	private static void runBenchmark( final AllocationScenario scenario,
	                                  /*out*/
	                                  final BenchmarkResult benchmarkResult,
	                                  final long singleBenchmarkTimeMs ) {
		runLoop( scenario, singleBenchmarkTimeMs, benchmarkResult );
	}

	private static void runLoop( final AllocationScenario scenario,
	                             final long maxTimeMs,
	                             /*out*/
	                             final BenchmarkResult benchmarkResult ) {
		long sum = 0;
		long totalIterations = 0;
		for( final long startedAt = System.currentTimeMillis();
		     System.currentTimeMillis() - startedAt < maxTimeMs; ) {
			for( int i = 0; i < ITERATIONS_IN_BATCH; i++ ) {
				sum += scenario.run();
			}
			totalIterations += ITERATIONS_IN_BATCH;
		}
		benchmarkResult.setTotalIterations( totalIterations );
		benchmarkResult.setBenchmarkResult( sum );
	}

	private static void printResults( final BenchmarkResult[] results ) {
		System.out.print( FORMATTER.header( results.length ) );

		for( int i = 0; i < results.length; i++ ) {
			final BenchmarkResult result = results[i];
			System.out.print( FORMATTER.format( i, result ) );
		}
	}

	public static class AllocationMonitor {
		private final GarbageCollectorMXBean[] gcMXBeans;
		private final ThreadMXBean threadMXBean;

		private final long benchmarkThreadId;

		private long gcCollectionTime;
		private long gcCollectionCount;

		private long memoryAllocatedByThreadBytes;

		public AllocationMonitor( final Thread benchmarkThread ) {
			gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans().toArray( new GarbageCollectorMXBean[0] );
			threadMXBean = ( ThreadMXBean ) ManagementFactory.getThreadMXBean();

			this.benchmarkThreadId = benchmarkThread.getId();
		}

		public void storeCurrentStamps() {
			long collectionCounts = 0;
			long collectionTimes = 0;
			for( final GarbageCollectorMXBean gcBean : gcMXBeans ) {
				collectionCounts += gcBean.getCollectionCount();
				collectionTimes += gcBean.getCollectionTime();
			}
			this.gcCollectionCount = collectionCounts;
			this.gcCollectionTime = collectionTimes;

			this.memoryAllocatedByThreadBytes = threadMXBean.getThreadAllocatedBytes( benchmarkThreadId );
		}

		public void updateStampsAndStoreDifference( /*out*/ final BenchmarkResult benchmarkResult ) {
			long totalGcCollectionCount = 0;
			long totalGcCollectionTime = 0;
			for( final GarbageCollectorMXBean gcBean : gcMXBeans ) {
				totalGcCollectionCount += gcBean.getCollectionCount();
				totalGcCollectionTime += gcBean.getCollectionTime();
			}

			final long bytesAllocatedByThread = threadMXBean.getThreadAllocatedBytes( benchmarkThreadId );

			benchmarkResult.setup(
					totalGcCollectionTime - gcCollectionTime,
					totalGcCollectionCount - gcCollectionCount,
					bytesAllocatedByThread - memoryAllocatedByThreadBytes
			);


			this.gcCollectionCount = totalGcCollectionCount;
			this.gcCollectionTime = totalGcCollectionTime;
			this.memoryAllocatedByThreadBytes = bytesAllocatedByThread;
		}
	}

}
