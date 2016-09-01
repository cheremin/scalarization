package ru.cheremin.scalarization.lab.collections;

import java.util.*;

import org.junit.Test;
import ru.cheremin.scalarization.Scenario;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesNothing;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesSomething;

/**
 * @author ruslan
 *         created 31/08/16 at 23:24
 */
public class IterateSpecialCollectionsTest {
	public static final Integer VALUE = 1256547;

	@Test
	public void singletonListIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final Integer n : Collections.singletonList( VALUE ) ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void singletonSetIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final Integer n : Collections.singleton( VALUE ) ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void singletonMapValuesIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final Integer n : Collections.singletonMap( "KEY", VALUE ).values() ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesSomething()
		);
	}

	@Test
	public void emptyListIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final Integer n : Collections.<Integer>emptyList() ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void emptySetIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final Integer n : Collections.<Integer>emptySet() ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void emptyMapValuesIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final Integer n : Collections.<String, Integer>emptyMap().values() ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesNothing()
		);
	}
}