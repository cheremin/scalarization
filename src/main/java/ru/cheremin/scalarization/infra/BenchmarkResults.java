package ru.cheremin.scalarization.infra;

import java.io.IOException;
import java.util.*;

import com.google.common.collect.ImmutableList;
import net.jcip.annotations.Immutable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author ruslan
 *         created 13/08/16 at 19:35
 */
public class BenchmarkResults {
	private final String scenarioName;
	private final ImmutableList<IterationResult> results;

	public BenchmarkResults( final String scenarioName,
	                         final IterationResult[] results ) {
		checkArgument( scenarioName != null, "scenarioName can't be null" );
		checkArgument( results != null && results.length > 0, "results can't be null nor empty" );
		this.scenarioName = scenarioName;
		this.results = ImmutableList.copyOf( results );
	}

	public String scenarioName() {
		return scenarioName;
	}

	public List<IterationResult> iterationResults() {
		return results;

	}

	@Immutable
	public static class IterationResult {
		public final long gcCollectionTime;
		public final long gcCollectionCount;

		public final long memoryAllocatedByThreadBytes;

		public final long totalIterations;

		/** Fake "result" to prevent dead code elimination */
		public final long benchmarkOutcome;

		public IterationResult( final long totalIterations,
		                        final long memoryAllocatedByThreadBytes,
		                        final long gcCollectionTime,
		                        final long gcCollectionCount,
		                        final long benchmarkOutcome ) {
			this.totalIterations = totalIterations;
			this.memoryAllocatedByThreadBytes = memoryAllocatedByThreadBytes;
			this.gcCollectionTime = gcCollectionTime;
			this.gcCollectionCount = gcCollectionCount;
			this.benchmarkOutcome = benchmarkOutcome;
		}
	}


	public interface Formatter {
		public void format( final BenchmarkResults results,
		                    final Appendable appendable ) throws IOException;
	}
}
