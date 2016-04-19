package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedProperty;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static java.util.Arrays.asList;
import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;
import static ru.cheremin.scalarization.scenarios.Utils.generateStringArray;

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
	public static final String BUILDER_TYPE_KEY = "scenario.builder-type";

	private static final BuilderType BUILDER_TYPE = BuilderType.valueOf(
			System.getProperty( BUILDER_TYPE_KEY, BuilderType.NORMAL.name() )
	);

	private final String[] keys = generateStringArray( SIZE );


	@Override
	public long run() {
		return BUILDER_TYPE.equalsWithKeys( keys );
	}

	@Override
	public String additionalInfo() {
		return BUILDER_TYPE.name();
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

	public enum BuilderType {
		NORMAL {
			@Override
			public int equalsWithKeys( final String[] keys ) {
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
		},
		EXTENDED {
			@Override
			public int equalsWithKeys( final String[] keys ) {
				final EqualsBuilderEx builder = new EqualsBuilderEx();
				for( final String key : keys ) {
					builder.append( key, key );
				}
				return builder
						.append( true, true )
						.append( 1.1, 1.2 )
						.append( 1, 1 )
						.isEquals() ? 1 : 0;
			}
		};

		public abstract int equalsWithKeys( final String[] keys );
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 0, 1, 4, 128 ),

				allOf( BUILDER_TYPE_KEY, BuilderType.values() ),

				asList(
						new JvmExtendedProperty( "FreqInlineSize", "325" ),
						new JvmExtendedProperty( "FreqInlineSize", "500" )
				)
		);
	}
}
