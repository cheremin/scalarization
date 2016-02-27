package ru.cheremin.scalarization.scenarios.tricky;

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
 *
 * I.e. it is only system identity hash code which blocks scalarization (most probably
 * because it is native function which is not inlined, and not explicitly marked
 * as none-escaping, as many other intrinsics does).
 *
 * TODO check 1.7
 *
 * @author ruslan
 *         created 18/02/16 at 00:36
 */
public class ObjectyStaffScenario extends AllocationScenario {
	@Override
	public long allocate() {
		final WithoutCustomHashCode o1 = new WithoutCustomHashCode( 1, 4 );
		final WithoutCustomHashCode o2 = new WithoutCustomHashCode( 1, 4 );
		return o1.getClass() ==  o2.getClass() ? 1 : 5;
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
}
