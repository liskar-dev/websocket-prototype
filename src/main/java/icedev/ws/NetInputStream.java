package icedev.ws;

import java.io.*;

public class NetInputStream extends InputStream {
    /**
     * The input stream to be filtered.
     */
    private final InputStream in;

    public NetInputStream(InputStream in) {
        this.in = in;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }
    
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    public int available() throws IOException {
        return in.available();
    }
    public void close() throws IOException {
        in.close();
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    public void reset() throws IOException {
        in.reset();
    }

    public boolean markSupported() {
        return in.markSupported();
    }
	
	@Override
	public int read() throws IOException, EOFException {
		int r = in.read();
		if(r==-1)
			throw new EOFException();
		return r;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException, EOFException {
		int r = in.read(b, off, len);
		if(r==-1)
			throw new EOFException();
		return r;
	}

}