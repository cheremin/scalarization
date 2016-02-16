package ru.cheremin.scalarization.scenarios;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * With using at least one .append(Object,Object) method (even without looping) -> EA
 * fails with "Skipping method because: code size (327) exceeds MaxBCEAEstimateSize (150)"
 * on both 1.7 and 1.8 jdks. It is .append(Object,Object) which cost 327 bytecodes alone.
 * <p/>
 * Even with -XX:MaxBCEAEstimateSize=1400 allocations still happen. Probably, because
 * of .getClass() intrinsic function used in .append(Object,Object), but I'm not sure
 * TODO verify with debug jvm
 * <p/>
 * Without .append(Object,Object) scalarization do happens
 * Extending EqualsBuilder to EqualsBuilderEx with specialized .append(String,String)
 * also cause scalarization to happen.
 *
 * @author ruslan
 *         created 16/02/16 at 23:45
 */
public class EqualsBuilderScenario extends AllocationScenario {
	private final String[] keys = Utils.generateStringArray( SIZE );


	@Override
	public long allocate() {
		final EqualsBuilderEx builder = new EqualsBuilderEx();
//		for( final String key : keys ) {
//			builder.append( key, key );
//		}
		return builder
				.append( "AbcA", "AbcA" )
				.isEquals() ? 1 : 0;
	}


	public static final class EqualsBuilderEx extends EqualsBuilder {
		public EqualsBuilderEx() {
		}

		public EqualsBuilderEx append( final String l,
		                               final String r ) {
			if( !StringUtils.equals( l, r ) ) {
				setEquals( false );
			}
			return this;
		}
	}
}
