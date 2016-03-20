package ru.cheremin.scalarization.scenarios.tricky;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import ru.cheremin.scalarization.ForkingMain;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * 1.8.0_73:
 * new Object().hashCode() -> is not scalarized
 * new WithCustomHashCode().hashCode() -> is scalarized
 * new WithoutCustomHashCode().hashCode() -> is NOT scalarized
 * System.identityHashCode( new WithCustomHashCode(..) ) -> NOT scalarized
 * new WithCustomHashCode() == new WithCustomHashCode() -> scalarized
 * WithoutCustomHashCode.equals(WithoutCustomHashCode) -> scalarized
 * WithoutCustomHashCode.getClass() == WithoutCustomHashCode.getClass() -> scalarized
 * <p/>
 * I.e. it is only system identity hash code which blocks scalarization (most probably
 * because it is native function which is not inlined, and not explicitly marked
 * as none-escaping, as many other intrinsics does).
 * <p/>
 * TODO check 1.7
 *
 * @author ruslan
 *         created 18/02/16 at 00:36
 */
public class ObjectyStaffScenario extends AllocationScenario {
	public static final String USE_TYPE_KEY = "scenario.use-type";
	private static final Type USE_TYPE = Type.valueOf(
			System.getProperty( USE_TYPE_KEY, Type.OBJECT_HASH_CODE.name() )
	);


	@Override
	public long allocate() {
		switch( USE_TYPE ) {
			case OBJECT_HASH_CODE: {
				return objectHashCode();
			}
			case HASH_CODE_OVERRIDDEN: {
				return hashCodeOverridden();
			}
			case HASH_CODE_NOT_OVERRIDDEN: {
				return hashCodeNotOverridden();
			}

			case IDENTITY_HASH_CODE: {
				return identityHashCode();
			}

			case REFERENCE_EQUALITY: {
				return referenceEquality();
			}
			case EQUALS_INHERITED_FROM_OBJECT: {
				return equalsInheritedFromObject();
			}
			case GET_CLASS: {
				return getClazz();
			}
		}
		throw new IllegalStateException( "Unknown type " + USE_TYPE );
	}

	private int equalsInheritedFromObject() {
		final WithoutCustomHashCode o1 = new WithoutCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		final WithoutCustomHashCode o2 = new WithoutCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		return o1.equals( o2 ) ? 1 : 8;
	}

	private int referenceEquality() {
		final WithCustomHashCode o1 = new WithCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		final WithCustomHashCode o2 = new WithCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		return o1 == o2 ? 1 : 8;
	}

	private int getClazz() {
		final WithoutCustomHashCode o1 = new WithoutCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		final WithoutCustomHashCode o2 = new WithoutCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		return o1.getClass() == o2.getClass() ? 1 : 5;
	}

	private int identityHashCode() {
		final WithCustomHashCode o = new WithCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		return System.identityHashCode( o );
	}

	private int hashCodeNotOverridden() {
		final WithoutCustomHashCode o = new WithoutCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		return o.hashCode();
	}

	private int hashCodeOverridden() {
		final WithCustomHashCode o = new WithCustomHashCode(
				nextDouble(),
				nextDouble()
		);
		return o.hashCode();
	}

	private int objectHashCode() {
		return new Object().hashCode();
	}

	@Override
	public String additionalInfo() {
		return USE_TYPE.name();
	}

	private static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}

	public static final class WithCustomHashCode {
		private double x;
		private double y;

		public WithCustomHashCode( final double x,
		                           final double y ) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals( final Object o ) {
			if( this == o ) {
				return true;
			}
			if( o == null || getClass() != o.getClass() ) {
				return false;
			}

			final WithCustomHashCode that = ( WithCustomHashCode ) o;

			if( Double.compare( that.x, x ) != 0 ) {
				return false;
			}
			return Double.compare( that.y, y ) == 0;

		}

		@Override
		public int hashCode() {
			int result;
			long temp;
			temp = Double.doubleToLongBits( x );
			result = ( int ) ( temp ^ ( temp >>> 32 ) );
			temp = Double.doubleToLongBits( y );
			result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
			return result;
		}
	}

	public static final class WithoutCustomHashCode {
		private double x;
		private double y;

		public WithoutCustomHashCode( final double x,
		                              final double y ) {
			this.x = x;
			this.y = y;
		}
	}

	public static enum Type {
		OBJECT_HASH_CODE,
		HASH_CODE_OVERRIDDEN,
		HASH_CODE_NOT_OVERRIDDEN,

		IDENTITY_HASH_CODE,

		REFERENCE_EQUALITY,
		EQUALS_INHERITED_FROM_OBJECT,

		GET_CLASS;

	}

	@ScenarioRunArgs
	public static List<ForkingMain.ScenarioRun> parametersToRunWith() {
		return Lists.transform(
				ImmutableList.copyOf( Type.values() ),
				new Function<Type, ForkingMain.ScenarioRun>() {
					@Override
					public ForkingMain.ScenarioRun apply( final Type type ) {
						return runWith(
								USE_TYPE_KEY, type.name(),
								SCENARIO_SIZE_KEY, -1
						);
					}
				}
		);
	}
}
