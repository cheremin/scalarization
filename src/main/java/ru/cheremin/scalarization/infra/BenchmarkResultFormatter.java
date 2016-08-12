package ru.cheremin.scalarization.infra;

/**
 * @author ruslan
 *         created 12/08/16 at 23:21
 */
public interface BenchmarkResultFormatter {
	public String header( final int resultsCount );

	public String format( final int resultNo,
	                      final BenchmarkResult result );
}
