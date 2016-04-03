package ru.cheremin.scalarization.scenarios.collections;

import java.util.*;

import com.google.common.collect.Lists;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;
import static ru.cheremin.scalarization.scenarios.Utils.generateStringArray;

/**
 * Check may be .toArray(new T[N] ) for small collections (N = 0 or 1) scalarized.
 * <p/>
 * It looks like not, because of too complex logic in .toArray(). But implementing
 * .toArray() by hand, using the simple for-loop, being not so effective from CPU
 * perspective, IS scalarized with N=0,1
 * <p/>
 * TODO RC: check array of size 0 (with .getClass())
 * TODO RC: check array of size 1 (with .getClass())
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ToArrayScenario extends AllocationScenario {

	private final ArrayList<String> stringsList = Lists.newArrayList( generateStringArray( SIZE ) );

	@Override
	public long run() {
		final String[] stringsArray = new String[SIZE];
		toArray( stringsList, stringsArray );
//		stringsList.toArray( stringsArray );
		return stringsArray.length;
	}

	private static <T> void toArray( final ArrayList<T> list,
	                                 final T[] array ) {
		for( int i = 0; i < list.size(); i++ ) {
			array[i] = list.get( i );
		}
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runWithAll( SIZE_KEY, 0, 1, 2 );
	}
}
