package ru.cheremin.scalarization.scenarios;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple Vector2D arithmetic always scalarized (seems like)
 * TODO But in-loop replacement is not scalarized. Why?
 * @author ruslan
 *         created 09/02/16 at 20:11
 */
public class FixedSizeObjectsAllocator extends AllocationScenario {

	@Override
	public long allocate() {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();
//		return ( long ) new Vector2D( rnd.nextDouble(), 3.8 )
//				.add( new Vector2D( 1.5, 3.4 ) )
//				.dot( new Vector2D( 1.9, 14.3 ) );
		Vector2D v = new Vector2D( rnd.nextDouble(), rnd.nextDouble() );
		for( int i = 0; i < SIZE; i++ ) {
			v.addAccumulate(
					new Vector2D( 1, 2 )
			);
		}
		return ( long ) v.length();
//		return v.hashCode();
	}

	public static final class Vector2D {
		private double x;
		private double y;

		public Vector2D( final double x,
		                 final double y ) {
			this.x = x;
			this.y = y;
		}

		public Vector2D add( final Vector2D other ) {
			return new Vector2D(
					x + other.x,
					y + other.y
			);
		}

		public void addAccumulate( final Vector2D other ) {
			x = x + other.x;
			y = y + other.y;
		}

		public double dot( final Vector2D other ) {
			return x * other.x + y * other.y;
		}

		public double length() {
			return Math.sqrt( this.dot( this ) );
		}
	}
}
