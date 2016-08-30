package ru.cheremin.scalarization.lab.misc;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.lab.Utils.Pool;

import static com.google.common.base.Preconditions.checkArgument;
import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;
import static ru.cheremin.scalarization.lab.Utils.randomStringsPool;

/**
 * java1.7: It seems like varargs-array scalarized perfectly fine for sizes 1..4, but
 * only if check almost never fails. I.e. it looks like even 1-2 check fails (and
 * exceptions thrown and catch) lead to re-compilation(?) and after it allocations are
 * not scalarized anymore (even if there is no more fails happens for quite a long time).
 * I'm not sure about recompilation, though.
 * <p/>
 * Java 1.8: Seems like scalarization is not so stable even then check is not failing
 * at all. Still do not understand backgrounds
 *
 * @author ruslan
 *         created 03/04/16 at 00:25
 */
public class PreconditionsScenario extends AllocationScenario {
	public static final String CHECK_FAILED_PROBABILITY_KEY = "scenario.check-failed-probability";

	private static final double CHECK_FAILED_PROBABILITY = Double.valueOf(
			System.getProperty( CHECK_FAILED_PROBABILITY_KEY, "1e-5" )
	);

	private final Pool<String> pool = randomStringsPool( 1024 );

	private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

	@Override
	public long run() {
		try {
			switch( SIZE ) {
				//no reason to check 0-args since there is checkArgument 0-args version
				case 1: {
					checkArgument1Arg();
					return 0;
				}
				case 2: {
					checkArgument2Args();
					return 0;
				}
				case 3: {
					checkArgument3Args();
					return 0;
				}
				case 4: {
					checkArgument4Args();
					return 0;
				}
				default: {
					throw new IllegalStateException( "SIZE=" + SIZE + " is not supported" );
				}
			}
		} catch( IllegalArgumentException e ) {
			return 1;
		}
	}

	private void checkArgument1Arg() {
		final boolean expression = falseWithProbability( CHECK_FAILED_PROBABILITY );
		final String arg1 = pool.next();
		checkArgument( expression, "1-arg(%s) message", arg1 );
	}

	private void checkArgument2Args() {
		final boolean expression = falseWithProbability( CHECK_FAILED_PROBABILITY );
		final String arg1 = pool.next();
		final String arg2 = pool.next();
		checkArgument( expression, "2-arg(%s, %s) message", arg1, arg2 );
	}

	private void checkArgument3Args() {
		final boolean expression = falseWithProbability( CHECK_FAILED_PROBABILITY );
		final String arg1 = pool.next();
		final String arg2 = pool.next();
		final String arg3 = pool.next();
		checkArgument( expression, "3-arg(%s, %s, %s) message", arg1, arg2, arg3 );
	}

	private void checkArgument4Args() {
		final boolean expression = falseWithProbability( CHECK_FAILED_PROBABILITY );
		final String arg1 = pool.next();
		final String arg2 = pool.next();
		final String arg3 = pool.next();
		final String arg4 = pool.next();
		checkArgument( expression, "4-arg(%s, %s, %s, %s) message", arg1, arg2, arg3, arg4 );
	}

	@Override
	public String additionalInfo() {
		return "failed probability: " + CHECK_FAILED_PROBABILITY;
	}

	private boolean falseWithProbability( final double falseProbability ) {
		//RC: 'false' must be possible (so JIT can't prove it impossible and DCE code
		// altogether), but it should be very unlikely, since I don't want exception
		// to be really thrown
		return rnd.nextDouble() >= falseProbability;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( CHECK_FAILED_PROBABILITY_KEY, "1e-7", "1e-9", "1e-11" ),
				allOf( SIZE_KEY, 1, 2, 3, 4 )
		);
	}
}