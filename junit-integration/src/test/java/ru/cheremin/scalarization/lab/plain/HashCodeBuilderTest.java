package ru.cheremin.scalarization.lab.plain;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;
import ru.cheremin.scalarization.Scenario;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.allocatesNothing;

/**
 * @author ruslan
 *         created 16/08/16 at 23:40
 */
public class HashCodeBuilderTest {

	@Test
	public void hasCodeBuilderIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return new HashCodeBuilder()
								.append( "34543" )
								.append( true )
								.append( 'A' )
								.append( "Abc" )
								.append( 1.45f )
								.toHashCode();
					}
				},
				allocatesNothing()
		);
	}
}