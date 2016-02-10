package ru.cheremin.scalarization.scenarios;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class DummyAllocator extends AllocationScenario {
	public int dummy;

	@Override
	public long allocate() {
		return dummy++;
	}
}
