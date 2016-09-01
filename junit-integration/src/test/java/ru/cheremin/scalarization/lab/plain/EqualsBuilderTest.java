package ru.cheremin.scalarization.lab.plain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;
import ru.cheremin.scalarization.Scenario;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesNothing;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesSomething;

/**
 * @author ruslan
 *         created 16/08/16 at 23:40
 */
public class EqualsBuilderTest {

	@Test
	public void equalsBuilderWithPrimitivesIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return new EqualsBuilder()
								.append( true, true )
								.append( 1.1, 1.2 )
								.append( 1, 1 )
								.isEquals() ? 1 : 0;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void equalsBuilderWithObjectsIsNotScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return new EqualsBuilder()
								.append( "1233", "54654" )
								.isEquals() ? 1 : 0;
					}
				},
				finallyAllocatesSomething()
		);
	}

	@Test
	public void equalsBuilderRefactoredWithObjectsIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return new EqualsBuilderScenario.EqualsBuilderEx()
								.append( "1233", "54654" )
								.isEquals() ? 1 : 0;
					}
				},
				finallyAllocatesNothing()
		);
	}
}