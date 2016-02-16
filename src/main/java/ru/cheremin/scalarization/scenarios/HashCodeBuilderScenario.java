package ru.cheremin.scalarization.scenarios;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Looks like HashCodeBuilder stable scalarized by 1.7-1.8 with SIZE up to 128
 * @author ruslan
 *         created 16/02/16 at 23:45
 */
public class HashCodeBuilderScenario extends AllocationScenario {
	private final String[] keys = Utils.generateStringArray( SIZE );


	@Override
	public long allocate() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		for( final String key : keys ) {
			builder.append( key );
		}
		return builder
				.append( true )
				.append( 'A' )
				.append( "Abc" )
				.toHashCode();
	}


}
