package ru.cheremin.scalarization.lab.plain;

import java.util.*;

import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;

/**
 * It looks like nested objects are _not_ scalarized (same as inner classes).
 * I'm sad here
 *
 * @author ruslan
 *         created 16/02/16 at 23:45
 */
public class NestedObjectsScenario extends AllocationScenario {

	public static final String TYPE_KEY = "scenario.type";

	public static final Type TYPE = Type.valueOf(
			System.getProperty( TYPE_KEY, Type.NESTED_0.name() )
	);

	@Override
	public long run() {
		switch( TYPE ) {
			case NESTED_0:
				return nested0();
			case NESTED_1:
				return nested1();
			case NESTED_2:
				return nested2();
			case INNER:
				return inner();
			default: {
				throw new IllegalStateException( "Unknown type " + TYPE );
			}
		}

	}

	private long inner() {
		final Node root = new Node( null, 10 );
		final Node.Inner inner = root.new Inner();
		return inner.owner().getPayload();
	}

	private long nested0() {
		final Node root = new Node( null, 10 );
		return root.getPayload();
	}

	private long nested1() {
		final Node nested = new Node( null, 10 );
		final Node root = new Node( nested, 20 );
		return root.getNested().getPayload();
	}

	private long nested2() {
		final Node nested2 = new Node( null, 10 );
		final Node nested = new Node( nested2, 10 );
		final Node root = new Node( nested, 20 );
		return root.getNested().getNested().getPayload();
	}

	public static enum Type {
		NESTED_0,
		NESTED_1,
		NESTED_2,
		INNER;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runWithAll( TYPE_KEY, Type.values() );
	}

	public static class Node {

		private final Node nested;
		private final long payload;

		public Node( final Node nested,
		             final long payload ) {
			this.nested = nested;
			this.payload = payload;
		}

		public Node getNested() {
			return nested;
		}

		public long getPayload() {
			return payload;
		}

		public class Inner {
			public Node owner() {
				return Node.this;
			}
		}
	}

}
