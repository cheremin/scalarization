package ru.cheremin.scalarization.junit;

import java.io.IOException;
import java.util.*;

import org.hamcrest.*;
import ru.cheremin.scalarization.Scenario;
import ru.cheremin.scalarization.infra.BenchmarkResults;
import ru.cheremin.scalarization.infra.BenchmarkResults.IterationResult;
import ru.cheremin.scalarization.infra.Formatters;

import static com.google.common.base.Preconditions.checkArgument;
import static ru.cheremin.scalarization.infra.AllocationBenchmarkBuilder.AllocationMonitor.INFRASTRUCTURE_ALLOCATION_BYTES;
import static ru.cheremin.scalarization.infra.AllocationBenchmarkBuilder.forScenario;

/**
 * @author ruslan
 *         created 07/08/16 at 16:01
 */
public class AllocationMatcher extends TypeSafeDiagnosingMatcher<Scenario> {

	private static final int DEFAULT_ITERATIONS = Integer.getInteger( "allocation-matcher.iterations", 12 );
	private static final int DEFAULT_ITERATION_DURATION_MS = Integer.getInteger( "allocation-matcher.iteration-duration-ms", 3000 );

	private final int iterations = DEFAULT_ITERATIONS;
	private final long iterationDurationMs = DEFAULT_ITERATION_DURATION_MS;
	private final BenchmarkResults.Formatter formatter = Formatters.FULL;


	private final Matcher<BenchmarkResults> benchmarkResultsMatcher;

	public AllocationMatcher( final Matcher<BenchmarkResults> benchmarkResultsMatcher ) {
		checkArgument( benchmarkResultsMatcher != null, "benchmarkResultsMatcher can't be null" );
		this.benchmarkResultsMatcher = benchmarkResultsMatcher;
	}

	@Override
	protected boolean matchesSafely( final Scenario scenario,
	                                 final Description mismatch ) {
		final BenchmarkResults benchmarkResults = forScenario( scenario )
				.withIterationDurationMs( iterationDurationMs )
				.withIterations( iterations )
				.run();


//		final BenchmarkResult lastResult = benchmarkResults[benchmarkResults.length - 1];
//		final double bytesAllocatedPerLastRun = 1.0 * ( lastResult.memoryAllocatedByThreadBytes ) / lastResult.totalIterations;
		if( benchmarkResultsMatcher.matches( benchmarkResults ) ) {
			return true;
		} else {
			mismatch.appendValue( scenario )
					.appendText( " allocations ~ [" );
			benchmarkResultsMatcher.describeMismatch( benchmarkResults, mismatch );
			mismatch.appendText( "]" );

			if( formatter != null ) {
				try {
					final StringBuilder sb = new StringBuilder();
					formatter.format( benchmarkResults, sb );

					mismatch.appendText( "\nDetailed log: \n" );
					mismatch.appendText( sb.toString() );
				} catch( IOException e ) {
					throw new AssertionError( "IO exception should not happen with StringBuilder", e );
				}
			}
			return false;
		}
	}

	@Override
	public void describeTo( final Description description ) {
		description.appendText( "scenario.run() allocations ~ [" )
				.appendDescriptionOf( benchmarkResultsMatcher )
				.appendText( "]" );
	}

	public static AllocationMatcher lastIteration( final Matcher<IterationResult> lastRunResultMatcher ) {
		return new AllocationMatcher( new FeatureMatcher<BenchmarkResults, IterationResult>(
				lastRunResultMatcher,
				"last benchmark",
				"last benchmark result"
		) {
			@Override
			protected IterationResult featureValueOf( final BenchmarkResults results ) {
				final List<IterationResult> iterationResults = results.iterationResults();
				return iterationResults.get( iterationResults.size() - 1 );
			}
		} );
	}

	public static AllocationMatcher allocatesPerTurn( final Matcher<Number> allocatedBytesPerRunMatcher ) {
		return lastIteration(
				new FeatureMatcher<IterationResult, Number>(
						allocatedBytesPerRunMatcher,
						"allocated bytes/run",
						"allocated bytes/run"
				) {
					@Override
					protected Number featureValueOf( final IterationResult actual ) {
						return actual.memoryAllocatedByThreadBytes / actual.totalIterations;
					}
				}
		);
	}

	public static AllocationMatcher allocatesNothing() {
		return lastIteration( new TypeSafeDiagnosingMatcher<IterationResult>() {
			@Override
			protected boolean matchesSafely( final IterationResult result,
			                                 final Description mismatch ) {
				if( result.gcCollectionCount == 0
						&& result.gcCollectionTime == 0
						&& ( result.memoryAllocatedByThreadBytes <= INFRASTRUCTURE_ALLOCATION_BYTES ) ) {
					return true;
				} else {
					mismatch.appendText( "allocates [" )
							.appendValue( result.memoryAllocatedByThreadBytes )
							.appendText( "] bytes (~" )
							.appendValue( result.memoryAllocatedByThreadBytes * 1.0 / result.totalIterations )
							.appendText( " bytes/run) in final" );
					return false;
				}
			}

			@Override
			public void describeTo( final Description description ) {
				description.appendText( "allocates no more than " )
						.appendValue( INFRASTRUCTURE_ALLOCATION_BYTES )
						.appendText( " bytes in final turns" );
			}
		} );
	}

	public static AllocationMatcher allocatesSomething() {
		return lastIteration( new TypeSafeDiagnosingMatcher<IterationResult>() {
			@Override
			protected boolean matchesSafely( final IterationResult result,
			                                 final Description mismatch ) {
				if( ( result.memoryAllocatedByThreadBytes > INFRASTRUCTURE_ALLOCATION_BYTES ) ) {
					return true;
				} else {
					final double bytesPerIteration = result.memoryAllocatedByThreadBytes * 1.0 / result.totalIterations;
					mismatch.appendText( "allocates [" )
							.appendValue( result.memoryAllocatedByThreadBytes )
							.appendText( "] bytes (~" )
							.appendValue( bytesPerIteration )
							.appendText( " bytes/run) in final" );
					return false;
				}
			}

			@Override
			public void describeTo( final Description description ) {
				description.appendText( "allocates more than " )
						.appendValue( INFRASTRUCTURE_ALLOCATION_BYTES )
						.appendText( " bytes in final turns" );
			}
		} );
	}

	//TODO RC: AllocationMatcherBuilder.iterations(12)
	//                                 .secondsEach(3)
	//                                 .finallyAllocatesNothing();

}
