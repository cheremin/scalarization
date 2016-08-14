package ru.cheremin.scalarization.lab.misc;

import ru.cheremin.scalarization.AllocationScenario;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class DummyScenario extends AllocationScenario {
	public int dummy;

	@Override
	public long run() {
		return dummy++;
	}
}
