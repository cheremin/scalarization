package ru.cheremin.scalarization.scenarios.collections;


import java.util.concurrent.ThreadLocalRandom;

import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ArrayUnpredictableSizeScenario extends AllocationScenario {

	@Override
	public long allocate() {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();

		final String[] array = new String[rnd.nextInt( SIZE )];

		return array.length;
	}
}
