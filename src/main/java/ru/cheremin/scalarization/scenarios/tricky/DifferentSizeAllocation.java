package ru.cheremin.scalarization.scenarios.tricky;

import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runForAll;

/**
 * @author ruslan
 *         created 01/04/16 at 00:12
 */
public class DifferentSizeAllocation extends AllocationScenario {

	private static final String TYPE_KEY = "scenario.object-type";
	public static final ObjectType OBJECT_TYPE = ObjectType.valueOf( System.getProperty( TYPE_KEY, ObjectType.SMALL.name() ) );

	@Override
	public long allocate() {
		final Object object = OBJECT_TYPE.apply( this );
		return 0;
	}

	//TODO RC: Vector2D(), Vector50D, Vector64D
	//TODO RC: new int[1], new int[64], new int[65]
	//TODO RC: new long[1], new long[50], long[64], new int[65]

	public static enum ObjectType {
		SMALL {
			@Override
			public Object apply( final DifferentSizeAllocation scenario ) {
				return null;
			}
		},
		BIG{
			@Override
			public Object apply( final DifferentSizeAllocation scenario ) {
				return null;
			}
		};

		public abstract Object apply( final DifferentSizeAllocation scenario );
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runForAll( TYPE_KEY, ObjectType.values() );
	}
}
