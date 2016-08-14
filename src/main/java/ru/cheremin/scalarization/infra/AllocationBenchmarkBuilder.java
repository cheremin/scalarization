package ru.cheremin.scalarization.infra;

import java.lang.management.ManagementFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.ThreadMXBean;
import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.infra.BenchmarkResults.IterationResult;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author ruslan
 *         created 13/08/16 at 19:29
 */
public class AllocationBenchmarkBuilder {

	private static final int ITERATIONS_IN_BATCH = Integer.getInteger( "iterations-in-batch", 1024 );

	private final Supplier<AllocationScenario> scenarioSupplier;

	private long iterationDurationMs = 3000;
	private int iterations = 12;

	private AllocationBenchmarkBuilder( final Supplier<AllocationScenario> scenarioSupplier ) {
		checkArgument( scenarioSupplier != null, "scenarioSupplier can't be null" );
		this.scenarioSupplier = scenarioSupplier;
	}

	public static AllocationBenchmarkBuilder forScenario( final Class<? extends AllocationScenario> scenarioClass ) {
		return new AllocationBenchmarkBuilder( new Supplier<AllocationScenario>() {
			@Override
			public AllocationScenario get() {
				try {
					return scenarioClass.newInstance();
				} catch( Exception e ) {
					throw Throwables.propagate( e );
				}
			}
		} );
	}

	public static AllocationBenchmarkBuilder forScenario( final AllocationScenario scenario ) {
		return new AllocationBenchmarkBuilder( Suppliers.ofInstance( scenario ) );
	}

	public AllocationBenchmarkBuilder withIterationDurationMs( final long iterationTimeMs ) {
		this.iterationDurationMs = iterationTimeMs;
		return this;
	}

	public AllocationBenchmarkBuilder withIterations( final int iterations ) {
		this.iterations = iterations;
		return this;
	}

	public BenchmarkResults run() {
		checkState( iterationDurationMs > 0, "iterationDurationMs(%s) must be >0", iterationDurationMs );
		checkState( iterations > 0, "iterations(%s) must be >0", iterations );

		final AllocationScenario scenario = scenarioSupplier.get();
		checkState( scenario !=null, "scenarioSupplier.get() must not be null" );

		final String scenarioName = scenario.toString();
		final SingleIterationResult[] iterationResults = runIterations(
				scenario,
				iterations,
				iterationDurationMs
		);

		final IterationResult[] results = new IterationResult[iterationResults.length];
		for( int i = 0; i < iterationResults.length; i++ ) {
			results[i] = iterationResults[i].toImmutable();
		}
		return new BenchmarkResults( scenarioName, results );
	}


	/* ================================= infra =================================== */

	private static SingleIterationResult[] runIterations( final AllocationScenario scenario,
	                                                      final int iterations,
	                                                      final long singleIterationDurationMs ) {
		final SingleIterationResult[] iterationResults = new SingleIterationResult[iterations];
		for( int i = 0; i < iterationResults.length; i++ ) {
			iterationResults[i] = new SingleIterationResult();
		}
		System.gc();

		final AllocationMonitor allocationMonitor = new AllocationMonitor( Thread.currentThread() );
		allocationMonitor.storeCurrentStamps();
		for( int i = 0; i < iterations; i++ ) {
			final SingleIterationResult iterationResult = iterationResults[i];

			runIteration( scenario, singleIterationDurationMs, iterationResult );

			allocationMonitor.updateStampsAndStoreDifference( iterationResult );
		}
		return iterationResults;
	}

	private static void runIteration( final AllocationScenario scenario,
	                                  final long singleIterationDurationMs,
	                                  /*out*/
	                                  final SingleIterationResult iterationResult ) {
		long outcome = 0;//to prevent DCE
		long totalIterations = 0;
		final long startedAt = System.currentTimeMillis();
		while( true ) {

			for( int i = 0; i < ITERATIONS_IN_BATCH; i++ ) {
				outcome += scenario.run();
			}
			totalIterations += ITERATIONS_IN_BATCH;

			final long elapsedMs = System.currentTimeMillis() - startedAt;
			if( elapsedMs >= singleIterationDurationMs ) {
				break;
			}
		}

		iterationResult.setTotalIterations( totalIterations );
		iterationResult.storeBenchmarkOutcome( outcome );
	}


	public static class AllocationMonitor {
		/**
		 * Bytes allocated in {@linkplain ThreadMXBean#getThreadAllocatedBytes(long)} for
		 * 2 long[1] arrays. Exact value can be in range [48..64], depends on arch(32/64)
		 * and compressedOops usage, so I use max
		 */
		public static final int INFRASTRUCTURE_ALLOCATION_BYTES = 64;

		private final GarbageCollectorMXBean[] gcMXBeans;
		private final ThreadMXBean threadMXBean;

		private final long benchmarkThreadId;

		private long gcCollectionTime;
		private long gcCollectionCount;

		private long memoryAllocatedByThreadBytes;

		public AllocationMonitor( final Thread benchmarkThread ) {
			//noinspection SuspiciousToArrayCall
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

		public void updateStampsAndStoreDifference( /*out*/ final SingleIterationResult iterationResult ) {
			long totalGcCollectionCount = 0;
			long totalGcCollectionTime = 0;
			for( final GarbageCollectorMXBean gcBean : gcMXBeans ) {
				totalGcCollectionCount += gcBean.getCollectionCount();
				totalGcCollectionTime += gcBean.getCollectionTime();
			}

			final long bytesAllocatedByThread = threadMXBean.getThreadAllocatedBytes( benchmarkThreadId );

			iterationResult.fill(
					totalGcCollectionTime - gcCollectionTime,
					totalGcCollectionCount - gcCollectionCount,
					bytesAllocatedByThread - memoryAllocatedByThreadBytes
			);


			this.gcCollectionCount = totalGcCollectionCount;
			this.gcCollectionTime = totalGcCollectionTime;
			this.memoryAllocatedByThreadBytes = bytesAllocatedByThread;
		}
	}

	private static class SingleIterationResult {
		public long gcCollectionTime;
		public long gcCollectionCount;

		public long memoryAllocatedByThreadBytes;

		/** Fake "result" to prevent dead code elimination */
		public long benchmarkOutcome;
		public long totalIterations;

		public void fill( final long gcCollectionTime,
		                  final long gcCollectionCount,
		                  final long memoryAllocatedByThreadBytes ) {
			this.gcCollectionTime = gcCollectionTime;
			this.gcCollectionCount = gcCollectionCount;
			this.memoryAllocatedByThreadBytes = memoryAllocatedByThreadBytes;
		}

		public void storeBenchmarkOutcome( final long benchmarkResult ) {
			this.benchmarkOutcome = benchmarkResult;
		}

		public void setTotalIterations( final long totalIterations ) {
			this.totalIterations = totalIterations;
		}

		public IterationResult toImmutable() {
			return new IterationResult(
					totalIterations,
					memoryAllocatedByThreadBytes,
					gcCollectionTime,
					gcCollectionCount,
					benchmarkOutcome
			);
		}
	}
}
