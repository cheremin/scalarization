package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils.StringKeysGenerator;

import static ru.cheremin.scalarization.ScenarioRun.withoutSpecificParameters;
import static ru.cheremin.scalarization.scenarios.Utils.randomKeysGenerator;

/**
 * Check pattern: method with multiple return values wrapped in composite object.
 * Successfully scalarized with 1.7/1.8
 *
 * @author ruslan
 *         created 09/02/16 at 21:41
 */
public class ReturnTupleScenario extends AllocationScenario {
	private final StringKeysGenerator generator = randomKeysGenerator( 1024 );

	@Override
	public long run() {

		final Tuple2<String> tuple = createTuple2(
				generator.next(),
				generator.next()
		);

		return tuple.getItem1().length()
				+ tuple.getItem2().length();
	}

	//TODO RC: try return null!
	//TODO RC: return different subtypes of common supertype
	private Tuple2<String> createTuple2( final String item1,
	                                     final String item2 ) {
		final String key1 = item1;
		final String key2 = item2;
		return new Tuple2( key1, key2 );
	}


	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return withoutSpecificParameters();
	}

	public static class Tuple2<T> {
		public T item1;
		public T item2;

		public Tuple2( final T item1,
		               final T item2 ) {
			this.item1 = item1;
			this.item2 = item2;
		}

		public T getItem1() {
			return item1;
		}

		public T getItem2() {
			return item2;
		}
	}
}
