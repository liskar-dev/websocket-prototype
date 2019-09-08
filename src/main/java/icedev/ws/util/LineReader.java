package icedev.ws.util;

import java.io.*;
import java.nio.charset.Charset;

/**
 * This is not overengineering.
 * 
 * StreamDecoder and therefore any Reader consumes all
 * the available bytes from the input. This makes them not usable for
 * reading binary data after reading text, if it comes in one batch.
 * Typically it does over network, so this class is needed.
 * 
 * Other option is to throttle the stream to only return one byte at a time. It wouldn't affect performance that much if its already buffered
 * @author kerai
 */
public class LineReader {

	private static final Charset UTF8 = Charset.forName("UTF8");
	private InputStream in;
	
	private int length;
	private byte[] buffer;
	
	public LineReader(InputStream in) {
		this.in = in;
		buffer = new byte[1024 * 4];
	}
	
	public String readLine() throws IOException {
		while(true) {
			int c = in.read();
			//System.out.print((char)c);
			
			if(c == -1) {
				if(length==0)
					return null;
				return ret();					
			}

			if(c == '\r') {
				in.read();
				return ret();
			}
			
			if(c == '\n') {
				return ret();
			}
			
			buffer[length++] = (byte) c;
		}
	}
	
	private String ret() {
		String ret = new String(buffer,0,length, UTF8);
		length = 0;
		return ret;
	}
}
