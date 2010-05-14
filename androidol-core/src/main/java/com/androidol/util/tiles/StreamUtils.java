package com.androidol.util.tiles;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.androidol.constants.UtilConstants;

public class StreamUtils implements UtilConstants {
	
	/**
	 * Copy the content of the input stream into the output stream, using a temporary
	 * byte array buffer whose size is defined by {@link #IO_BUFFER_SIZE}.
	 *
	 * @param in The input stream to copy from.
	 * @param out The output stream to copy to.
	 *
	 * @throws IOException If any error occurs during the copy.
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	/**
	 * Closes the specified stream.
	 *
	 * @param stream The stream to close.
	 */
	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				android.util.Log.e("IO", "Could not close stream", e);
			}
		}
	} 
}
