package ru.cheremin.scalarization.infra;

import java.io.IOException;
import java.util.*;

import ru.cheremin.scalarization.infra.BenchmarkResults.IterationResult;

import static ru.cheremin.scalarization.infra.AllocationBenchmarkBuilder.AllocationMonitor.INFRASTRUCTURE_ALLOCATION_BYTES;

/**
 * @author ruslan
 *         created 12/08/16 at 23:22
 */
public enum Formatters implements BenchmarkResults.Formatter {
	FULL {
		@Override
		public void format( final BenchmarkResults results,
		                    final Appendable appendable ) throws IOException {
			final List<IterationResult> iterationResults = results.iterationResults();
			final Formatter formatter = new Formatter( appendable );
			for( int no = 0; no < iterationResults.size(); no++ ) {
				final IterationResult result = iterationResults.get( no );
				final double bytesAllocatedPerRun = 1.0 * ( result.memoryAllocatedByThreadBytes ) / result.totalIterations;
				final String shortSummary = shortSummary( result );
				formatter.format(
						"run[#%d]: %s (~= %.2f bytes/iteration: allocated %d bytes/%d iterations, %d GCs/%dms 'result' = %d) \n",
						no,
						shortSummary,
						bytesAllocatedPerRun,
						result.memoryAllocatedByThreadBytes,
						result.totalIterations,
						result.gcCollectionCount,
						result.gcCollectionTime,
						result.benchmarkOutcome
				);
			}
			formatter.flush();
		}
	},
	SHORT {
		@Override
		public void format( final BenchmarkResults results,
		                    final Appendable appendable ) throws IOException {
			final List<IterationResult> iterationResults = results.iterationResults();
			final Formatter formatter = new Formatter( appendable );
			for( int no = 0; no < iterationResults.size(); no++ ) {
				final IterationResult result = iterationResults.get( no );
				final double bytesAllocatedPerRun = 1.0 * ( result.memoryAllocatedByThreadBytes ) / result.totalIterations;
				formatter.format(
						"#%d: ~= %.2f b/call (%d bytes/%d calls) %d\n",
						no,
						bytesAllocatedPerRun,
						result.memoryAllocatedByThreadBytes,
						result.totalIterations,
						result.benchmarkOutcome
				);
			}
			formatter.flush();
		}
	},
	CSV {
		@Override
		public void format( final BenchmarkResults results,
		                    final Appendable appendable ) throws IOException {
			appendable.append( "#, alloc?, bytes/iter, alloc bytes, iter-s, , GCs, 'result'\n" );

			final List<IterationResult> iterationResults = results.iterationResults();
			final Formatter formatter = new Formatter( appendable );
			for( int no = 0; no < iterationResults.size(); no++ ) {
				final IterationResult result = iterationResults.get( no );
				final double bytesAllocatedPerRun = 1.0 * ( result.memoryAllocatedByThreadBytes ) / result.totalIterations;
				final String shortSummary = shortSummary( result );
				formatter.format(
						"%d, %s, %.2f, %d, %d, %d, %d, %d\n",
						no,
						shortSummary,
						bytesAllocatedPerRun,
						result.memoryAllocatedByThreadBytes,
						result.totalIterations,
						result.gcCollectionCount,
						result.gcCollectionTime,
						result.benchmarkOutcome
				);
			}
			formatter.flush();
		}
	};

	private static String shortSummary( final IterationResult result ) {
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
