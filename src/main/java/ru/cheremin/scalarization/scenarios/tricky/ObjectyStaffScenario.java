package ru.cheremin.scalarization.scenarios.tricky;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * 1.8.0_77:
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
	public long run() {
		switch( USE_TYPE ) {
			case OBJECT_HASH_CODE: {
				return objectHashCode();
			}
			case OBJECT_EQUALS: {
				return objectEquals();
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
			case EQUALS_NOT_OVERRIDEN: {
				return equalsNotOverriden();
			}
			case GET_CLASS: {
				return getClazz();
			}
		}
		throw new IllegalStateException( "Unknown type " + USE_TYPE );
	}

	private int objectHashCode() {
		return new Object().hashCode();
	}

	private int objectEquals() {
		final Object o1 = new Object();
		final Object o2 = new Object();
		return o1.equals( o2 ) ? 1 : 0;
	}

	private int hashCodeOverridden() {
		final WithCustomHashCode o = new WithCustomHashCode(
				nextLong(),
				nextLong()
		);
		return o.hashCode();
	}

	private int hashCodeNotOverridden() {
		final WithoutCustomHashCode o = new WithoutCustomHashCode(
				nextLong(),
				nextLong()
		);
		return o.hashCode();
	}

	private int identityHashCode() {
		final WithCustomHashCode o = new WithCustomHashCode(
				nextLong(),
				nextLong()
		);
		return System.identityHashCode( o );
	}

	private int equalsNotOverriden() {
		final WithoutCustomHashCode o1 = new WithoutCustomHashCode(
				nextLong(),
				nextLong()
		);
		final WithoutCustomHashCode o2 = new WithoutCustomHashCode(
				nextLong(),
				nextLong()
		);
		return o1.equals( o2 ) ? 1 : 8;
	}

	private int referenceEquality() {
		final WithCustomHashCode o1 = new WithCustomHashCode(
				nextLong(),
				nextLong()
		);
		final WithCustomHashCode o2 = new WithCustomHashCode(
				nextLong(),
				nextLong()
		);
		return o1 == o2 ? 1 : 8;
	}

	private int getClazz() {
		final WithoutCustomHashCode o1 = new WithoutCustomHashCode(
				nextLong(),
				nextLong()
		);
		final WithoutCustomHashCode o2 = new WithoutCustomHashCode(
				nextLong(),
				nextLong()
		);
		return o1.getClass() == o2.getClass() ? 1 : 5;
	}

	@Override
	public String additionalInfo() {
		return USE_TYPE.name();
	}

	private static long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}

	public static final class WithCustomHashCode {
		private final long x;
		private final long y;

		public WithCustomHashCode( final long x,
		                           final long y ) {
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

			if( Long.compare( that.x, x ) != 0 ) {
				return false;
			}
			return Long.compare( that.y, y ) == 0;

		}

		@Override
		public int hashCode() {
			int result;
			result = ( int ) ( x ^ ( x >>> 32 ) );
			result = 31 * result + ( int ) ( y ^ ( y >>> 32 ) );
			return result;
		}
	}

	public static final class WithoutCustomHashCode {
		public final long x;
		public final long y;

		public WithoutCustomHashCode( final long x,
		                              final long y ) {
			this.x = x;
			this.y = y;
		}

	}

	public static enum Type {
		OBJECT_HASH_CODE,
		OBJECT_EQUALS,

		HASH_CODE_OVERRIDDEN,
		HASH_CODE_NOT_OVERRIDDEN,

		IDENTITY_HASH_CODE,

		REFERENCE_EQUALITY,
		EQUALS_NOT_OVERRIDEN,

		GET_CLASS;

	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, -1 ),
				allOf( USE_TYPE_KEY, Type.values() )
		);
	}
}
