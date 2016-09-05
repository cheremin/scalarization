package ru.cheremin.scalarization.lab.misc;

import java.util.concurrent.ThreadLocalRandom;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import ru.cheremin.scalarization.Scenario;
import ru.cheremin.scalarization.infra.BenchmarkResults;
import ru.cheremin.scalarization.junit.AllocationMatcher;
import ru.cheremin.scalarization.lab.Utils.Pool;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.lab.Utils.randomStringsPool;

/**
 * @author ruslan
 *         created 04/09/16 at 14:53
 */
public class PreconditionsTest {

	private final Pool<String> pool = randomStringsPool( 1024 );


	@Test
	public void checkArgumentWith1ArgIsNotAllocated1e_20() throws Exception {
		final double probabilityOfFailedCheck = 1e-20;
		assertThat(
				new CheckArgument1ArgScenario( probabilityOfFailedCheck ),
				allocatesNothingIfOnlyFewThrows()
		);
	}

	@Test
	public void checkArgumentWith1ArgIsNotAllocated1e_10() throws Exception {
		final double probabilityOfFailedCheck = 1e-10;
		assertThat(
				new CheckArgument1ArgScenario( probabilityOfFailedCheck ),
				allocatesNothingIfOnlyFewThrows()
		);
	}

	@Test
	public void checkArgumentWith1ArgIsNotAllocated1e_08() throws Exception {
		final double probabilityOfFailedCheck = 1e-08;
		assertThat(
				new CheckArgument1ArgScenario( probabilityOfFailedCheck ),
				allocatesNothingIfOnlyFewThrows()
		);
	}

	@Test
	public void checkArgumentWith2ArgIsNotAllocated1E_20() throws Exception {
		final double probabilityOfFailedCheck = 1e-20;
		assertThat(
				new CheckArguments2ArgScenario( probabilityOfFailedCheck ),
				allocatesNothingIfOnlyFewThrows()
		);
	}

	@Test
	public void checkArgumentWith2ArgIsNotAllocated1E_10() throws Exception {
		final double probabilityOfFailedCheck = 1e-10;
		assertThat(
				new CheckArguments2ArgScenario( probabilityOfFailedCheck ),
				allocatesNothingIfOnlyFewThrows()
		);
	}

	@Test
	public void checkArgumentWith2ArgIsNotAllocated1E_08() throws Exception {
		final double probabilityOfFailedCheck = 1e-08;
		assertThat(
				new CheckArguments2ArgScenario( probabilityOfFailedCheck ),
				allocatesNothingIfOnlyFewThrows()
		);
	}

	@Test
	public void checkArgumentWith3ArgIsNotAllocated1E_20() throws Exception {
		final double probabilityOfFailedCheck = 1e-20;
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						try {
							checkArgument3Args( probabilityOfFailedCheck );
							return 0;
						} catch( IllegalArgumentException e ) {
							return 1;
						}
					}
				},
				allocatesNothingIfOnlyFewThrows()
		);
	}

	@Test
	public void checkArgumentWith4ArgIsNotAllocated1E_20() throws Exception {
		final double probabilityOfFailedCheck = 20;
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						try {
							checkArgument4Args( probabilityOfFailedCheck );
							return 0;
						} catch( IllegalArgumentException e ) {
							return 1;
						}
					}
				},
				allocatesNothingIfOnlyFewThrows()
		);
	}

	/* ================ infra ==================================================== */

	private void checkArgument1Arg( final double probabilityOfFailedCheck ) {
		final boolean expression = falseWithProbability( probabilityOfFailedCheck );
		final String arg1 = pool.next();
		checkArgument( expression, "1-arg(%s) message", arg1 );
	}

	private void checkArgument2Args( final double probabilityOfFailedCheck ) {
		final boolean expression = falseWithProbability( probabilityOfFailedCheck );
		final String arg1 = pool.next();
		final String arg2 = pool.next();
		checkArgument( expression, "2-arg(%s, %s) message", arg1, arg2 );
	}

	private void checkArgument3Args( final double probabilityOfFailedCheck ) {
		final boolean expression = falseWithProbability( probabilityOfFailedCheck );
		final String arg1 = pool.next();
		final String arg2 = pool.next();
		final String arg3 = pool.next();
		checkArgument( expression, "3-arg(%s, %s, %s) message", arg1, arg2, arg3 );
	}

	private void checkArgument4Args( final double probabilityOfFailedCheck ) {
		final boolean expression = falseWithProbability( probabilityOfFailedCheck );
		final String arg1 = pool.next();
		final String arg2 = pool.next();
		final String arg3 = pool.next();
		final String arg4 = pool.next();
		checkArgument( expression, "4-arg(%s, %s, %s, %s) message", arg1, arg2, arg3, arg4 );
	}

	private static boolean falseWithProbability( final double falseProbability ) {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();
		//RC: 'false' must be possible (so JIT can't prove it impossible and DCE code
		// altogether), but it should be very unlikely
		return rnd.nextDouble() >= falseProbability;
	}

	private static AllocationMatcher allocatesNothingIfOnlyFewThrows() {
		return new AllocationMatcher(
				new AllocatesNothingIfNotThrowTooMuchMatcher()
		);
	}

	public static class AllocatesNothingIfNotThrowTooMuchMatcher extends TypeSafeDiagnosingMatcher<BenchmarkResults> {

		private final double maxBytesAllocationPerIteration = 1.0;
		private final long maxThrows = 2;

		@Override
		protected boolean matchesSafely( final BenchmarkResults results,
		                                 final Description mismatch ) {
			final long throwsCount = totalThrowsCount( results.iterationResults() );
			if( throwsCount <= maxThrows ) {
				final BenchmarkResults.IterationResult finalResult = results.iterationResults().get( results.iterationResults().size() - 1 );
				final double bytesPerIteration = finalResult.memoryAllocatedByThreadBytes * 1.0 / finalResult.totalIterations;
				if( bytesPerIteration < maxBytesAllocationPerIteration ) {
					return true;
				} else {
					mismatch.appendText( "allocates (~" )
							.appendValue( bytesPerIteration )
							.appendText( " bytes/run > " + maxBytesAllocationPerIteration + ") in final, with " + throwsCount + " throws [" )
							.appendValue( finalResult.memoryAllocatedByThreadBytes )
							.appendText( "bytes]" );
					return false;
				}
			} else {
				return true;
			}
		}

		@Override
		public void describeTo( final Description description ) {
			description.appendText( "allocates no more than " )
					.appendValue( maxBytesAllocationPerIteration )
					.appendText( " bytes, if thrown <= " + maxThrows + " in final turns" );
		}

		private static long totalThrowsCount( final Iterable<BenchmarkResults.IterationResult> results ) {
			long throwsCount = 0;
			for( final BenchmarkResults.IterationResult result : results ) {
				throwsCount += result.benchmarkOutcome;
			}
			return throwsCount;
		}
	}

	private class CheckArgument1ArgScenario implements Scenario {
		private final double probabilityOfFailedCheck;

		public CheckArgument1ArgScenario( final double probabilityOfFailedCheck ) {
			this.probabilityOfFailedCheck = probabilityOfFailedCheck;
		}

		@Override
		public long run() {
			try {
				checkArgument1Arg( probabilityOfFailedCheck );
				return 0;
			} catch( IllegalArgumentException e ) {
				return 1;
			}
		}
	}

	private class CheckArguments2ArgScenario implements Scenario {
		private final double probabilityOfFailedCheck;

		public CheckArguments2ArgScenario( final double probabilityOfFailedCheck ) {
			this.probabilityOfFailedCheck = probabilityOfFailedCheck;
		}

		@Override
		public long run() {
			try {
				checkArgument2Args( probabilityOfFailedCheck );
				return 0;
			} catch( IllegalArgumentException e ) {
				return 1;
			}
		}
	}
}