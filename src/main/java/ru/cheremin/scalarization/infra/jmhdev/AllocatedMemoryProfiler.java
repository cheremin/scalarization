package ru.cheremin.scalarization.infra.jmhdev;

import java.util.*;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;

/**
 * @author ruslan
 *         created 23/02/16 at 19:33
 */
public class AllocatedMemoryProfiler implements InternalProfiler {
	@Override
	public void beforeIteration( final BenchmarkParams benchmarkParams,
	                             final IterationParams iterationParams ) {

	}

	@Override
	public Collection<? extends Result> afterIteration( final BenchmarkParams benchmarkParams,
	                                                    final IterationParams iterationParams,
	                                                    final IterationResult result ) {
		return null;
	}

	@Override
	public String getDescription() {
		return "Measures memory allocated by current thread by calling HotSpot MBeans";
	}
}
