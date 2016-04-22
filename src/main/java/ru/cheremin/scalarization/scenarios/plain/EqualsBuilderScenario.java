package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;

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
 * fails. -XX:+PrintInlining produces "callee is too large" and "hot method too big"
 * for EqualsBuilder.append(), so it looks like inlining threshold is breached. Default
 * FreqInlineSize is 325, which is less than 327 bc/append. Increasing -XX:FreqInlineSize
 * 325->500 indeed removes all allocations with 1.8 and 1.7 JVMs.
 * <p/>
 * With long .append(Object,Object) method split into 2 shorter methods scalarization
 * do happens successfully with default settings.
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

		@Override
		public EqualsBuilder append( final Object lhs, final Object rhs ) {
			if( !isEquals() ) {
				return this;
			}
			if( lhs == rhs ) {
				return this;
			}
			if( lhs == null || rhs == null ) {
				this.setEquals( false );
				return this;
			}
			final Class<?> lhsClass = lhs.getClass();
			if( !lhsClass.isArray() ) {
				// The simple case, not an array, just test the element
				setEquals( lhs.equals( rhs ) );
				return this;
			} else if( lhs.getClass() != rhs.getClass() ) {
				// Here when we compare different dimensions, for example: a boolean[][] to a boolean[]
				this.setEquals( false );
				return this;
			}

			//RC: I copy .append(Object,Object) from superclass, and just extract arrays
			//   comparison into separate method:
			return appendArrays( lhs, rhs );
		}

		private EqualsBuilder appendArrays( final Object lhs,
		                                    final Object rhs ) {
			// 'Switch' on type of array, to dispatch to the correct handler
			// This handles multi dimensional arrays of the same depth
			if( lhs instanceof long[] ) {
				append( ( long[] ) lhs, ( long[] ) rhs );
			} else if( lhs instanceof int[] ) {
				append( ( int[] ) lhs, ( int[] ) rhs );
			} else if( lhs instanceof short[] ) {
				append( ( short[] ) lhs, ( short[] ) rhs );
			} else if( lhs instanceof char[] ) {
				append( ( char[] ) lhs, ( char[] ) rhs );
			} else if( lhs instanceof byte[] ) {
				append( ( byte[] ) lhs, ( byte[] ) rhs );
			} else if( lhs instanceof double[] ) {
				append( ( double[] ) lhs, ( double[] ) rhs );
			} else if( lhs instanceof float[] ) {
				append( ( float[] ) lhs, ( float[] ) rhs );
			} else if( lhs instanceof boolean[] ) {
				append( ( boolean[] ) lhs, ( boolean[] ) rhs );
			} else {
				// Not an array of primitives
				append( ( Object[] ) lhs, ( Object[] ) rhs );
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
				allOf( BUILDER_TYPE_KEY, BuilderType.values() ),
				allOf( SIZE_KEY, 0, 1, 4, 128 ),

				asList(
						new JvmExtendedProperty( "FreqInlineSize", "325" ),
						new JvmExtendedProperty( "FreqInlineSize", "500" )
				)
		);
	}
}
