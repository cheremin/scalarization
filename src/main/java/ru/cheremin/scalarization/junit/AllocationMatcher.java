package ru.cheremin.scalarization.junit;

import org.hamcrest.*;
import ru.cheremin.scalarization.infra.BenchmarkResult;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static com.google.common.base.Preconditions.checkArgument;
import static ru.cheremin.scalarization.infra.AllocationBenchmarkMain.INFRASTRUCTURE_ALLOCATION_BYTES;
import static ru.cheremin.scalarization.infra.AllocationBenchmarkMain.runBenchmarkSeries;

/**
 * @author ruslan
 *         created 07/08/16 at 16:01
 */
public class AllocationMatcher extends TypeSafeDiagnosingMatcher<AllocationMatcher.Scenario> {

	private final int runs = 12;
	private final long singleRunTimeMs = 3000;


	private final Matcher<BenchmarkResult[]> benchmarkResultsMatcher;

	public AllocationMatcher( final Matcher<BenchmarkResult[]> benchmarkResultsMatcher ) {
		checkArgument( benchmarkResultsMatcher != null, "benchmarkResultsMatcher can't be null" );
		this.benchmarkResultsMatcher = benchmarkResultsMatcher;
	}

	@Override
	protected boolean matchesSafely( final Scenario scenario,
	                                 final Description mismatch ) {
		final BenchmarkResult[] benchmarkResults = runBenchmarkSeries(
				new AllocationScenario() {
					@Override
					public long run() {
						return scenario.run();
					}
				},
				runs,
				singleRunTimeMs
		);

//		final BenchmarkResult lastResult = benchmarkResults[benchmarkResults.length - 1];
//		final double bytesAllocatedPerLastRun = 1.0 * ( lastResult.memoryAllocatedByThreadBytes ) / lastResult.totalIterations;
		if( benchmarkResultsMatcher.matches( benchmarkResults ) ) {
			return true;
		} else {
			mismatch.appendValue( scenario )
					.appendText( " allocations ~ [" );
			benchmarkResultsMatcher.describeMismatch( benchmarkResults, mismatch );
			mismatch.appendText( "]" );
			return false;
		}
	}

	@Override
	public void describeTo( final Description description ) {
		description.appendText( "scenario.run() allocations ~ [" )
				.appendDescriptionOf( benchmarkResultsMatcher )
				.appendText( "]" );
	}

	public static AllocationMatcher lastRun( final Matcher<BenchmarkResult> lastRunResultMatcher ) {
		return new AllocationMatcher( new FeatureMatcher<BenchmarkResult[], BenchmarkResult>(
				lastRunResultMatcher,
				"last benchmark",
				"last benchmark result"
		) {
			@Override
			protected BenchmarkResult featureValueOf( final BenchmarkResult[] actual ) {
				return actual[actual.length - 1];
			}
		} );
	}

	public static AllocationMatcher allocatesPerRun( final Matcher<Number> allocatedBytesPerRunMatcher ) {
		return lastRun(
				new FeatureMatcher<BenchmarkResult, Number>(
						allocatedBytesPerRunMatcher,
						"allocated bytes/run",
						"allocated bytes/run"
				) {
					@Override
					protected Number featureValueOf( final BenchmarkResult actual ) {
						return actual.memoryAllocatedByThreadBytes / actual.totalIterations;
					}
				}
		);
	}

	public static AllocationMatcher allocatesNothing() {
		return lastRun( new TypeSafeDiagnosingMatcher<BenchmarkResult>() {
			@Override
			protected boolean matchesSafely( final BenchmarkResult result,
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
		return lastRun( new TypeSafeDiagnosingMatcher<BenchmarkResult>() {
			@Override
			protected boolean matchesSafely( final BenchmarkResult result,
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

	//TODO RC: AllocationMatcherBuilder.runs(12)
	//                                 .secondsEach(3)
	//                                 .finallyAllocatesNothing();

	public interface Scenario {
		public long run();
	}
}
