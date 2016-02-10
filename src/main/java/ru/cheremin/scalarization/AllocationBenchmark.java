package ru.cheremin.scalarization;

import java.lang.management.ManagementFactory;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.ThreadMXBean;
import ru.cheremin.scalarization.scenarios.AllocationScenario;


/**
 * @author ruslan
 *         created 13.11.12 at 22:42
 */
public class AllocationBenchmark {

	private static final String SCENARIO_CLASS_NAME = System.getProperty( "scenario" );

	private static final int ITERATIONS_IN_BATCH = Integer.getInteger( "iterations-in-batch", 1024 );
	private static final int SINGLE_BENCHMARK_TIME_MS = Integer.getInteger( "duration", 1000 );
	private static final int RUNS = 12;

	/**
	 * Bytes allocated in {@linkplain ThreadMXBean#getThreadAllocatedBytes(long)}for
	 * 2 long[1] arrays
	 */
	private static final int INFRASTRUCTURE_ALLOCATION_BYTES = 48;


	public static void main( final String[] args ) throws Exception {
		final Class<?> clazz = Class.forName( AllocationBenchmark.class.getPackage().getName() + '.' + SCENARIO_CLASS_NAME );
		final AllocationScenario scenario = ( AllocationScenario ) clazz.newInstance();

		System.out.printf(
				"%s: %d runs, %d ms each\n",
				scenario,
				RUNS,
				SINGLE_BENCHMARK_TIME_MS
		);

		final BenchmarkResult[] benchmarkResults = new BenchmarkResult[RUNS];
		for( int i = 0; i < benchmarkResults.length; i++ ) {
			benchmarkResults[i] = new BenchmarkResult();
		}
		System.gc();

		final AllocationMonitor allocationMonitor = new AllocationMonitor( Thread.currentThread() );
		allocationMonitor.storeCurrentStamps();
		for( int i = 0; i < RUNS; i++ ) {
			final BenchmarkResult benchmarkResult = benchmarkResults[i];

			runBenchmark( scenario, benchmarkResult );

			allocationMonitor.updateStampsAndStoreDifference( benchmarkResult );
		}

		//print results
		printResults( benchmarkResults );
	}

	private static void runBenchmark( final AllocationScenario scenario,
	                                  /*out*/
	                                  final BenchmarkResult benchmarkResult ) {
		runLoop( scenario, SINGLE_BENCHMARK_TIME_MS, benchmarkResult );
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
				sum += scenario.allocate();
			}
			totalIterations += ITERATIONS_IN_BATCH;
		}
		benchmarkResult.setTotalIterations( totalIterations );
		benchmarkResult.setBenchmarkResult( sum );
	}

	private static void printResults( final BenchmarkResult[] results ) {
		for( int i = 0; i < results.length; i++ ) {
			final BenchmarkResult result = results[i];
			final boolean noAllocations =
					result.gcCollectionCount == 0
							&& result.gcCollectionTime == 0
							&& ( result.memoryAllocatedByThreadBytes <= INFRASTRUCTURE_ALLOCATION_BYTES );
			System.out.printf(
					"run[#%d]: %s. (Details: allocated %d bytes/%d iterations ~= %.2f bytes/iteration, %d GCs %d ms in total, 'result' = %d) \n",
					i,
					( noAllocations ? "likely NO_ALLOCATIONS" : "likely ARE_ALLOCATIONS" ),
					result.memoryAllocatedByThreadBytes,
					result.totalIterations,
					1.0 * ( result.memoryAllocatedByThreadBytes - INFRASTRUCTURE_ALLOCATION_BYTES ) / result.totalIterations,
					result.gcCollectionCount,
					result.gcCollectionTime,
					result.benchmarkResult
			);
		}
	}

	public static class BenchmarkResult {
		public long gcCollectionTime;
		public long gcCollectionCount;

		public long memoryAllocatedByThreadBytes;

		/** Fake "result" to prevent dead code elimination */
		public long benchmarkResult;
		private long totalIterations;

		public void setBenchmarkResult( final long benchmarkResult ) {
			this.benchmarkResult = benchmarkResult;
		}

		public void setup( final long gcCollectionTime,
		                   final long gcCollectionCount,
		                   final long memoryAllocatedByThreadBytes ) {
			this.gcCollectionTime = gcCollectionTime;
			this.gcCollectionCount = gcCollectionCount;
			this.memoryAllocatedByThreadBytes = memoryAllocatedByThreadBytes;
		}

		public void setTotalIterations( final long totalIterations ) {
			this.totalIterations = totalIterations;
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