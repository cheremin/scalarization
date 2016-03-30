package ru.cheremin.scalarization.infra.jmhdev;

import java.io.*;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import ru.cheremin.scalarization.infra.DuplicatingOutputStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author ruslan
 *         created 29/03/16 at 22:05
 */
public class DuplicateStdout implements AutoCloseable, Closeable {

	private final PrintStream storedOut;
	private final OutputStream duplicateToStream;

	public DuplicateStdout( final ByteSink duplicateToSink ) throws IOException {
		checkArgument( duplicateToSink != null, "duplicateToSink can't be null" );

		this.storedOut = System.out;

		duplicateToStream = duplicateToSink.openBufferedStream();

		System.setOut(
				new PrintStream(
						new DuplicatingOutputStream(
								storedOut,
								duplicateToStream
						)
				)
		);
	}

	@Override
	public void close() throws IOException {
		System.out.flush();
		System.setOut( storedOut );
		duplicateToStream.close();
	}

	public static DuplicateStdout duplicateOutToFile( final File file ) throws IOException {
		return new DuplicateStdout(
				Files.asByteSink( file )
		);
	}
}
