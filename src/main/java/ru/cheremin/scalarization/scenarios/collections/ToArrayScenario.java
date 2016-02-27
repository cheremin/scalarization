package ru.cheremin.scalarization.scenarios.collections;

import java.util.*;

import com.google.common.collect.Lists;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.scenarios.Utils.generateStringArray;

/**
 * TODO RC: check array of size 0 (with .getClass())
 * TODO RC: check array of size 1 (with .getClass())
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ToArrayScenario extends AllocationScenario {

	private final ArrayList<String> stringsList = Lists.newArrayList( generateStringArray( SIZE ) );

	@Override
	public long allocate() {
		final String[] stringsArray = new String[SIZE];
		for( int i = 0; i < stringsList.size(); i++ ) {
			stringsArray[i] = stringsList.get( i );
		}
//		stringsList.toArray( stringsArray );
		return stringsArray.length;
	}
}
