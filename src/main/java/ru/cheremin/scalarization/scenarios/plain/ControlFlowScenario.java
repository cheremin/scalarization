package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.Lists;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runWith;
import static ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario.Vector2D.randomVector;

/**
 * Check simple Vector2D arithmetic in different scenarios
 * <p/>
 * Results (both 1.7.0_25 and 1.8.0_73):
 * <p/>
 * ALLOCATE_ONCE           : scalarized
 * <p/>
 * ACCUMULATE_IN_LOOP        : scalarized
 * <p/>
 * REPLACE_IN_LOOP         : partially scalarized. For SIZE=1 fully scalarized, for
 * SIZE > 1 one of in-loop allocations is scalarized, other
 * SIZE allocations in loop + 1 out of loop are not scalarized
 * <p/>
 * ASSIGN_REFERENCE_CONDITIONALLY       : not scalarized
 * ASSIGN_REFERENCE_UN_CONDITIONALLY    : scalarized
 * ASSIGN_REFERENCE_UN_CONDITIONALLY2   : scalarized
 *
 * @author ruslan
 *         created 09/02/16 at 20:11
 */
public class ControlFlowScenario extends AllocationScenario {

	private static final Type USE_TYPE = Type.valueOf(
			System.getProperty( "scenario.use-type", Type.ALLOCATE_ONCE.name() )
	);

	@Override
	public long run() {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();

		switch( USE_TYPE ) {
			case ALLOCATE_ONCE:
				return allocateOnce( rnd );
			case ACCUMULATE_IN_LOOP:
				return accumulateInLoop( rnd );
			case REPLACE_REFERENCE_IN_LOOP:
				return replaceReferenceInLoop( rnd );
			case ASSIGN_REFERENCE_CONDITIONALLY:
				return assignReferenceConditionally( rnd );
			case ASSIGN_REFERENCE_UN_CONDITIONALLY:
				return assignReferenceUnConditionally( rnd );
			case ASSIGN_REFERENCE_UN_CONDITIONALLY2:
				return assignReferenceUnConditionally2( rnd );
		}

		throw new IllegalStateException( "Unknown USE_TYPE=" + USE_TYPE );
	}

	private long allocateOnce( final ThreadLocalRandom rnd ) {
		final Vector2D v1 = randomVector( rnd );
		final Vector2D v2 = randomVector( rnd );
		final Vector2D v3 = randomVector( rnd );
		return ( long ) v1.add( v2 ).dot( v3 );
	}

	private long accumulateInLoop( final ThreadLocalRandom rnd ) {
		final Vector2D v = randomVector( rnd );
		for( int i = 0; i < SIZE; i++ ) {
			final Vector2D addition = randomVector( rnd );
			v.addAccumulate( addition );
		}
		return ( long ) v.length();
	}

	private long replaceReferenceInLoop( final ThreadLocalRandom rnd ) {
		Vector2D v = randomVector( rnd );
		//RC: we replace reference, this confuses EA because it is hard to prove
		// which object v refer to
		for( int i = 0; i < SIZE; i++ ) {
//			v = v.add( new Vector2D( 1, 2 ) );
			v = v.mul( i );
		}
		return ( long ) v.length();
	}

	private long assignReferenceConditionally( final ThreadLocalRandom rnd ) {
		//RC: similar to replacement in loop: EA lost track of v's target object
		// and this prevents EA to prove v can be scalarized
		final Vector2D v;
		if( rnd.nextBoolean() ) {
			v = new Vector2D( 1, rnd.nextDouble() );
		} else {
			v = new Vector2D( rnd.nextDouble(), 1 );
		}

		return ( long ) v.length();
	}

	private long assignReferenceUnConditionally( final ThreadLocalRandom rnd ) {
		//RC: and this one IS scalarized
		if( rnd.nextBoolean() ) {
			final Vector2D v = new Vector2D( 1, rnd.nextDouble() );
			return ( long ) v.length();
		} else {
			final Vector2D v = new Vector2D( rnd.nextDouble(), 1 );
			return ( long ) v.length();
		}
	}

	private long assignReferenceUnConditionally2( final ThreadLocalRandom rnd ) {
		//RC: this one IS also scalarized
		final double x, y;
		if( rnd.nextBoolean() ) {
			x = 1;
			y = rnd.nextDouble();
		} else {
			x = rnd.nextDouble();
			y = 1;
		}
		final Vector2D v = new Vector2D( x, y );

		return ( long ) v.length();
	}

	@Override
	public String additionalInfo() {
		return USE_TYPE.name();
	}

	public static final class Vector2D {
		private double x;
		private double y;

		public static Vector2D randomVector( final ThreadLocalRandom rnd ) {
			return new Vector2D(
					rnd.nextDouble(),
					rnd.nextDouble()
			);
		}

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

		public Vector2D mul( final double scale ) {
			return new Vector2D(
					x * scale,
					y * scale
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

	public enum Type {
		ALLOCATE_ONCE,

		ACCUMULATE_IN_LOOP,
		REPLACE_REFERENCE_IN_LOOP,

		ASSIGN_REFERENCE_CONDITIONALLY,
		ASSIGN_REFERENCE_UN_CONDITIONALLY,
		ASSIGN_REFERENCE_UN_CONDITIONALLY2;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		final List<ScenarioRun> runs = Lists.newArrayList();

		runs.add(
				runWith(
						"scenario.size", -1,
						"scenario.use-type", Type.ALLOCATE_ONCE
				)
		);
		runs.add(
				runWith(
						"scenario.size", -1,
						"scenario.use-type", Type.ASSIGN_REFERENCE_CONDITIONALLY
				)
		);
		runs.add(
				runWith(
						"scenario.size", -1,
						"scenario.use-type", Type.ASSIGN_REFERENCE_UN_CONDITIONALLY
				)
		);
		runs.add(
				runWith(
						"scenario.size", -1,
						"scenario.use-type", Type.ASSIGN_REFERENCE_UN_CONDITIONALLY2
				)
		);

		final int[] sizes = { 0, 1, 2, 4, 8, 16, 32 };
		for( final int size : sizes ) {
			runs.add(
					runWith(
							"scenario.size", size,
							"scenario.use-type", Type.ACCUMULATE_IN_LOOP
					)
			);
		}
		for( final int size : sizes ) {
			runs.add(
					runWith(
							"scenario.size", size,
							"scenario.use-type", Type.REPLACE_REFERENCE_IN_LOOP
					)
			);
		}


		return runs;
	}
}
