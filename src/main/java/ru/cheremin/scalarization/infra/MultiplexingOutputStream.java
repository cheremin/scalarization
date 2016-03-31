package ru.cheremin.scalarization.infra;

import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author ruslan
 *         created 29/03/16 at 22:04
 */
public class MultiplexingOutputStream extends OutputStream {
	private final OutputStream[] delegates;

	public MultiplexingOutputStream( final OutputStream... delegates ) {
		checkArgument( delegates != null, "delegates can't be null" );
		this.delegates = delegates;
	}

	@Override
	public void write( final int b ) throws IOException {
		for( final OutputStream delegate : delegates ) {
			delegate.write( b );
		}
	}

	@Override
	public void write( final byte[] b ) throws IOException {
		for( final OutputStream delegate : delegates ) {
			delegate.write( b );
		}
	}

	@Override
	public void write( final byte[] b, final int off, final int len ) throws IOException {
		for( final OutputStream delegate : delegates ) {
			delegate.write( b, off, len );
		}
	}

	@Override
	public void flush() throws IOException {
		for( final OutputStream delegate : delegates ) {
			delegate.flush();
		}
	}

	@Override
	public void close() throws IOException {
		for( final OutputStream delegate : delegates ) {
			delegate.close();
		}
	}
}
