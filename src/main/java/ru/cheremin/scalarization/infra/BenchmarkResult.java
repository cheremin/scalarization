package ru.cheremin.scalarization.infra;

/**
 * @author ruslan
 *         created 12/08/16 at 23:20
 */
public class BenchmarkResult {
	public long gcCollectionTime;
	public long gcCollectionCount;

	public long memoryAllocatedByThreadBytes;

	/** Fake "result" to prevent dead code elimination */
	public long benchmarkResult;
	public long totalIterations;

	public void setBenchmarkResult( final long benchmarkResult ) {
		this.benchmarkResult = benchmarkResult;
	}

	public void setup( final long gcCollectionTime,
	                   final long gcCollectionCount,
	                   final long memoryAllocatedByThreadBytes ) {
		this.gcCollectionTime = gcCollectionTime;
		this.gcCollectionCount = gcCollectionCount;
		this.memoryAllocatedByThreadBytes = memoryAllocatedByThreadBytes;
	}

	public void setTotalIterations( final long totalIterations ) {
		this.totalIterations = totalIterations;
	}
}
