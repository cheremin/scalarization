package ru.cheremin.scalarization.scenarios.tricky;

import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;
import static ru.cheremin.scalarization.scenarios.collections.FixedSizePrimitiveArrayScenario.fillUnrolled;
import static ru.cheremin.scalarization.scenarios.collections.FixedSizePrimitiveArrayScenario.sumUnrolled;

/**
 * @author ruslan
 *         created 01/04/16 at 00:12
 */
public class DifferentSizeAllocation extends AllocationScenario {

	private static final String TYPE_KEY = "scenario.object-type";
	public static final ObjectType OBJECT_TYPE = ObjectType.valueOf( System.getProperty( TYPE_KEY, ObjectType.OBJECT.name() ) );

	@Override
	public long run() {
		return OBJECT_TYPE.runScenario( this );
	}

	@Override
	public String additionalInfo() {
		return OBJECT_TYPE.name();
	}

	//TODO RC: Vector2D(), Vector50D, Vector64D
	//TODO RC: new int[1], new int[64], new int[65]
	//TODO RC: new long[1], new long[50], long[64], new int[65]

	public static enum ObjectType {
		OBJECT {
			@Override
			public long runScenario( final DifferentSizeAllocation scenario ) {
				switch( SIZE ) {
					case 1: {
						return new Double( Math.random() ).longValue();
					}

				}
				return 0;
			}
		},
		ARRAY_OF_INTS {
			@Override
			public long runScenario( final DifferentSizeAllocation scenario ) {
				final int[] array = new int[SIZE];
				fillUnrolled( array, 2 );
				return sumUnrolled( array );
			}
		},
		ARRAY_OF_LONGS {
			@Override
			public long runScenario( final DifferentSizeAllocation scenario ) {
				final long[] array = new long[SIZE];
				fillUnrolled( array, 2 );
				return sumUnrolled( array );
			}
		};

		public abstract long runScenario( final DifferentSizeAllocation scenario );
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 1, 2, 50, 51, 64, 65 ),
				allOf( TYPE_KEY, ObjectType.values() )
		);
	}
}
