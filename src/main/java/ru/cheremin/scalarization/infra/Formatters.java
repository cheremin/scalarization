package ru.cheremin.scalarization.infra;

import static ru.cheremin.scalarization.infra.AllocationBenchmarkMain.INFRASTRUCTURE_ALLOCATION_BYTES;

/**
 * @author ruslan
 *         created 12/08/16 at 23:22
 */
public enum Formatters implements BenchmarkResultFormatter {
	FULL {
		@Override
		public String header( final int resultsCount ) {
			return "";
		}

		@Override
		public String format( final int resultNo,
		                      final BenchmarkResult result ) {
			final double bytesAllocatedPerRun = 1.0 * ( result.memoryAllocatedByThreadBytes ) / result.totalIterations;
			final String shortDescription = shortDescription( result );
			return String.format(
					"run[#%d]: %s (~= %.2f bytes/iteration: allocated %d bytes/%d iterations, %d GCs/%dms 'result' = %d) \n",
					resultNo,
					shortDescription,
					bytesAllocatedPerRun,
					result.memoryAllocatedByThreadBytes,
					result.totalIterations,
					result.gcCollectionCount,
					result.gcCollectionTime,
					result.benchmarkResult
			);
		}
	},
	SHORT {
		@Override
		public String header( final int resultsCount ) {
			return "";
		}

		@Override
		public String format( final int resultNo,
		                      final BenchmarkResult result ) {
			final double bytesAllocatedPerRun = 1.0 * ( result.memoryAllocatedByThreadBytes ) / result.totalIterations;
			return String.format(
					"#%d: ~= %.2f b/call (%d bytes/%d calls) %d\n",
					resultNo,
					bytesAllocatedPerRun,
					result.memoryAllocatedByThreadBytes,
					result.totalIterations,
					result.benchmarkResult
			);
		}
	},
	CSV {
		@Override
		public String header( final int resultsCount ) {
			return "#, alloc?, bytes/iter, alloc bytes, iter-s, , GCs, 'result'\n";
		}

		@Override
		public String format( final int resultNo,
		                      final BenchmarkResult result ) {
			final double bytesAllocatedPerRun = 1.0 * ( result.memoryAllocatedByThreadBytes ) / result.totalIterations;
			final String shortDescription = shortDescription( result );

			return String.format(
					"%d, %s, %.2f, %d, %d, %d, %d, %d\n",
					resultNo,
					shortDescription,
					bytesAllocatedPerRun,
					result.memoryAllocatedByThreadBytes,
					result.totalIterations,
					result.gcCollectionCount,
					result.gcCollectionTime,
					result.benchmarkResult
			);
		}
	};

	private static String shortDescription( final BenchmarkResult result ) {
		final double bytesAllocatedPerRun = 1.0 * ( result.memoryAllocatedByThreadBytes ) / result.totalIterations;
		if( result.gcCollectionCount == 0
				&& result.gcCollectionTime == 0
				&& ( result.memoryAllocatedByThreadBytes <= INFRASTRUCTURE_ALLOCATION_BYTES ) ) {
			return "NO_ALLOCATIONS";
		} else if( bytesAllocatedPerRun < 1 ) {
			return "likely NO_ALLOCATIONS";
		} else {
			return "ARE_ALLOCATIONS";
		}
	}
}
