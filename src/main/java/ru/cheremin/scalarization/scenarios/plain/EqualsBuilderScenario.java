package ru.cheremin.scalarization.scenarios.plain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils;

/**
 * With using at least one .append(Object,Object) method (even without looping) -> EA
 * fails with "Skipping method because: code size (327) exceeds MaxBCEAEstimateSize (150)"
 * on both 1.7 and 1.8 JVMs. BTW .append(Object,Object) cost 327 bytecodes alone...
 * <p/>
 * But even with -XX:MaxBCEAEstimateSize=1400 allocations still happen. -XX:+PrintInlining
 * produces "callee is too large" and "hot method too big" for EqualsBuilder.append(),
 * so it looks like inlining threshold is breached. Default FreqInlineSize is 325,
 * which is less than 327 bc/append. Increasing -XX:FreqInlineSize 325->500 indeed
 * removes all allocations with 1.8 and 1.7 JVMs.
 * <p/>
 * Without long .append(Object,Object) method scalarization do happens successfully
 * with default settings: e.g. Extending EqualsBuilder to EqualsBuilderEx with
 * specialized short .append(String,String) method also cause scalarization to happen.
 *
 * @author ruslan
 *         created 16/02/16 at 23:45
 */
public class EqualsBuilderScenario extends AllocationScenario {
	private final String[] keys = Utils.generateStringArray( SIZE );


	@Override
	public long allocate() {
		final EqualsBuilder builder = new EqualsBuilder();
		for( final String key : keys ) {
			builder.append( key, key );
		}
		return builder
				.append( true, true )
				.append( 1.1, 1.2 )
				.append( 1, 1 )
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
