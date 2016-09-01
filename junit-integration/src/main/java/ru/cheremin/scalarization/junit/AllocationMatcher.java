package ru.cheremin.scalarization.junit;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;
import org.hamcrest.*;
import ru.cheremin.scalarization.Scenario;
import ru.cheremin.scalarization.infra.BenchmarkResults;
import ru.cheremin.scalarization.infra.BenchmarkResults.IterationResult;
import ru.cheremin.scalarization.infra.Formatters;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.cheremin.scalarization.infra.AllocationBenchmarkBuilder.AllocationMonitor.INFRASTRUCTURE_ALLOCATION_BYTES;
import static ru.cheremin.scalarization.infra.AllocationBenchmarkBuilder.forScenario;

/**
 * @author ruslan
 *         created 07/08/16 at 16:01
 */
public class AllocationMatcher extends TypeSafeDiagnosingMatcher<Scenario> {

	private static final int DEFAULT_ITERATIONS = Integer.getInteger( "allocation-matcher.iterations", 12 );
	private static final long DEFAULT_ITERATION_DURATION_MS = Long.getLong( "allocation-matcher.iteration-duration-ms", 3000 );

	private final int iterations;
	private final long iterationDurationMs;
	private final BenchmarkResults.Formatter formatter;


	private final Matcher<BenchmarkResults> benchmarkResultsMatcher;

	public AllocationMatcher( final Matcher<BenchmarkResults> benchmarkResultsMatcher ) {
		this( DEFAULT_ITERATIONS, DEFAULT_ITERATION_DURATION_MS, Formatters.FULL, benchmarkResultsMatcher );
	}

	public AllocationMatcher( final int iterations,
	                          final long iterationDurationMs,
	                          final BenchmarkResults.Formatter formatter,
	                          final Matcher<BenchmarkResults> benchmarkResultsMatcher ) {
		super( Scenario.class );

		checkArgument( benchmarkResultsMatcher != null, "benchmarkResultsMatcher can't be null" );
		checkArgument( formatter != null, "formatter can't be null" );
		checkArgument( iterations > 0, "iterations(%s) must be positive", iterations );
		checkArgument( iterationDurationMs > 0, "iterationDurationMs(%s) must be positive", iterationDurationMs );

		this.iterations = iterations;
		this.iterationDurationMs = iterationDurationMs;
		this.formatter = formatter;
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

	public static AllocationMatcher finalIteration( final Matcher<IterationResult> lastRunResultMatcher ) {
		return new AllocationMatcher(
				DEFAULT_ITERATIONS,
				DEFAULT_ITERATION_DURATION_MS,
				Formatters.FULL,
				new LastIterationMatcher( lastRunResultMatcher )
		);
	}

	public static AllocationMatcher finallyAllocatesPerRun( final Matcher<Number> allocatedBytesPerRunMatcher ) {
		return finalIteration(
				new IterationAllocatesMatcher( allocatedBytesPerRunMatcher )
		);
	}

	public static AllocationMatcher finallyAllocatesNothing() {
		return finalIteration( new IterationAllocatesNothingMatcher() );
	}

	public static AllocationMatcher finallyAllocatesSomething() {
		return finalIteration( new IterationAllocatesSomethingMatcher() );
	}


	@Immutable
	public static class Builder {
		private final int iterations;
		private final long iterationDurationMs;
		private final BenchmarkResults.Formatter formatter;

		public Builder() {
			this( DEFAULT_ITERATIONS, DEFAULT_ITERATION_DURATION_MS, Formatters.FULL );
		}

		private Builder( final int iterations,
		                 final long iterationDurationMs,
		                 final BenchmarkResults.Formatter formatter ) {
			this.iterations = iterations;
			this.iterationDurationMs = iterationDurationMs;
			this.formatter = formatter;
		}


		public Builder iterations( final int iterations ) {
			return new Builder( iterations, iterationDurationMs, formatter );
		}

		public Builder secondsEach( final long iterationDurationSeconds ) {
			return eachIterationOf( iterationDurationSeconds, SECONDS );
		}

		public Builder eachIterationOf( final long iterationDuration,
		                                final TimeUnit unit ) {
			final long iterationDurationMs = unit.toMillis( iterationDuration );
			return new Builder( iterations, iterationDurationMs, formatter );
		}

		public Builder printMismatchWith( final BenchmarkResults.Formatter formatter ) {
			return new Builder( iterations, iterationDurationMs, formatter );
		}

		public AllocationMatcher finallyAllocatesPerRun( final Matcher<Number> allocatedBytesPerRunMatcher ) {
			return new AllocationMatcher(
					iterations,
					iterationDurationMs,
					formatter,
					new LastIterationMatcher(
							new IterationAllocatesMatcher(
									allocatedBytesPerRunMatcher
							)
					)
			);
		}


		public AllocationMatcher finallyAllocatesNothing() {
			return new AllocationMatcher(
					iterations,
					iterationDurationMs,
					formatter,
					new LastIterationMatcher(
							new IterationAllocatesNothingMatcher()
					)
			);
		}

		public AllocationMatcher finallyAllocatesSomething() {
			return new AllocationMatcher(
					iterations,
					iterationDurationMs,
					formatter,
					new LastIterationMatcher(
							new IterationAllocatesSomethingMatcher()
					)
			);
		}
	}

	private static class IterationAllocatesNothingMatcher extends TypeSafeDiagnosingMatcher<IterationResult> {
		@Override
		protected boolean matchesSafely( final IterationResult result,
		                                 final Description mismatch ) {
			if( result.memoryAllocatedByThreadBytes <= INFRASTRUCTURE_ALLOCATION_BYTES ) {
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
	}

	private static class IterationAllocatesSomethingMatcher extends TypeSafeDiagnosingMatcher<IterationResult> {
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
	}

	private static class LastIterationMatcher extends FeatureMatcher<BenchmarkResults, IterationResult> {
		public LastIterationMatcher( final Matcher<IterationResult> lastRunResultMatcher ) {
			super( lastRunResultMatcher, "last iteration", "last iteration result" );
		}

		@Override
		protected IterationResult featureValueOf( final BenchmarkResults results ) {
			final List<IterationResult> iterationResults = results.iterationResults();
			return iterationResults.get( iterationResults.size() - 1 );
		}
	}

	private static class IterationAllocatesMatcher extends FeatureMatcher<IterationResult, Number> {
		public IterationAllocatesMatcher( final Matcher<Number> allocatedBytesPerRunMatcher ) {
			super( allocatedBytesPerRunMatcher, "allocated bytes/run", "allocated bytes/run" );
		}

		@Override
		protected Number featureValueOf( final IterationResult actual ) {
			return actual.memoryAllocatedByThreadBytes / actual.totalIterations;
		}
	}
}
