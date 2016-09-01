package ru.cheremin.scalarization.lab.plain;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.junit.Test;
import ru.cheremin.scalarization.Scenario;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesSomething;

/**
 * @author ruslan
 *         created 16/08/16 at 23:40
 */
public class CompareToBuilderTest {

	@Test
	public void compareToBuilderIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return new CompareToBuilder()
								.append( "34543", "34543" )
								.append( true, true )
								.append( 'A', 'A' )
								.append( 1.45f, 1.5f )
								.toComparison();
					}
				},
				finallyAllocatesSomething()
		);
	}
}