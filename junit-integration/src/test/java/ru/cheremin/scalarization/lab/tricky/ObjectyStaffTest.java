package ru.cheremin.scalarization.lab.tricky;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import ru.cheremin.scalarization.Scenario;
import ru.cheremin.scalarization.lab.tricky.NewFinalizableScenario.OverridesFinalize;
import ru.cheremin.scalarization.lab.tricky.ObjectyStaffScenario.WithCustomHashCode;
import ru.cheremin.scalarization.lab.tricky.ObjectyStaffScenario.WithoutCustomHashCode;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesNothing;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesSomething;

/**
 * @author ruslan
 *         created 28/08/16 at 16:34
 */
public class ObjectyStaffTest {

	@Test
	public void objectHashCodeIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return new Object().hashCode();
					}
				},
				finallyAllocatesSomething()
		);

	}

	@Test
	public void nonOverriddenHashCodeIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithoutCustomHashCode o = new WithoutCustomHashCode(
								nextLong(),
								nextLong()
						);
						return o.hashCode();
					}
				},
				finallyAllocatesSomething()
		);

	}

	@Test
	public void systemIdentityHashCodeIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithCustomHashCode o = new WithCustomHashCode(
								nextLong(),
								nextLong()
						);
						return System.identityHashCode( o );
					}
				},
				finallyAllocatesSomething()
		);
	}

	@Test
	public void overriddenHashCodeIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithCustomHashCode o = new WithCustomHashCode(
								nextLong(),
								nextLong()
						);
						return o.hashCode();
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void objectEqualsIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final Object o1 = new Object();
						final Object o2 = new Object();
						return o1.equals( o2 ) ? 1 : 0;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void notOverriddenEqualsIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithoutCustomHashCode o1 = new WithoutCustomHashCode(
								nextLong(),
								nextLong()
						);
						final WithoutCustomHashCode o2 = new WithoutCustomHashCode(
								nextLong(),
								nextLong()
						);
						return o1.equals( o2 ) ? 1 : 8;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void referenceEqualityCheckIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithCustomHashCode o1 = new WithCustomHashCode(
								nextLong(),
								nextLong()
						);
						final WithCustomHashCode o2 = new WithCustomHashCode(
								nextLong(),
								nextLong()
						);
						return o1 == o2 ? 1 : 8;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void getClassIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithoutCustomHashCode o1 = new WithoutCustomHashCode(
								nextLong(),
								nextLong()
						);
						return o1.getClass().hashCode();
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void synchronizedIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithCustomHashCode o1 = new WithCustomHashCode(
								nextLong(),
								nextLong()
						);
						synchronized( o1 ) {
							return o1.hashCode();
						}
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void waitIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						try {
							final WithCustomHashCode o1 = new WithCustomHashCode(
									nextLong(),
									nextLong()
							);
							synchronized( o1 ) {
								o1.wait( 1 );
								return 2;
							}
						} catch( InterruptedException e ) {
							return -1;
						}
					}
				},
				finallyAllocatesSomething()
		);
	}

	@Test
	public void notifyIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final WithCustomHashCode o1 = new WithCustomHashCode(
								nextLong(),
								nextLong()
						);
						synchronized( o1 ) {
							o1.notify();
							return 2;
						}
					}
				},
				finallyAllocatesSomething()
		);
	}

	@Test
	public void objectWithFinalizerIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final OverridesFinalize o = new OverridesFinalize( "a" );
						return o.name.length();
					}
				},
				finallyAllocatesSomething()
		);
	}

	private static long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}
}