package ru.cheremin.scalarization.lab.plain;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import ru.cheremin.scalarization.Scenario;
import ru.cheremin.scalarization.lab.Utils.Pool;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.allocatesNothing;
import static ru.cheremin.scalarization.junit.AllocationMatcher.allocatesSomething;
import static ru.cheremin.scalarization.lab.Utils.randomStringsPool;
import static ru.cheremin.scalarization.lab.plain.ReturnTupleScenario.TupleType.*;

/**
 * @author ruslan
 *         created 29/08/16 at 09:16
 */
public class ReturnTupleTest {

	private final Pool<String> pool = randomStringsPool( 1024 );

	@Test
	public void plainImmutablePairReturnIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final Pair<String, String> tuple = IMMUTABLE_PAIR.tuple( pool );
						return hash( tuple );
					}
				},
				allocatesNothing()
		);
	}

	@Test
	public void plainMutablePairReturnIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final Pair<String, String> tuple = MUTABLE_PAIR.tuple( pool );
						return hash( tuple );
					}
				},
				allocatesNothing()
		);
	}

	@Test
	public void pairOrNullReturnIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final Pair<String, String> tuple = PAIR_OR_NULL.tuple( pool );
						return hash( tuple );
					}
				},
				allocatesSomething()
		);
	}

	@Test
	public void mixedPairTypesReturnIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final Pair<String, String> tuple = MIXED_PAIR.tuple( pool );
						return hash( tuple );
					}
				},
				allocatesSomething()
		);
	}

	@Test
	public void samePairReturnInBranchesIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final Pair<String, String> tuple = SAME_PAIR_IN_BRANCHES.tuple( pool );
						return hash( tuple );
					}
				},
				allocatesSomething()
		);
	}

	@Test
	public void mixedPairOrNullReturnIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final Pair<String, String> tuple = MIXED_PAIR_OR_NULL.tuple( pool );
						return hash( tuple );
					}
				},
				allocatesSomething()
		);
	}

	private static long hash( final Pair<String, String> tuple ) {
		if( tuple != null ) {
			return tuple.getLeft().length()
					+ tuple.getRight().length();
		} else {
			return 0;
		}
	}
}