package websocket;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.security.*;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

import websocket.util.LineReader;


public final class WebSocket {
	
	private static final Charset UTF8 = Charset.forName("UTF8");
	
	public final Map<String, String> headers = new CaseInsensitiveMap();
	
	private static String digest(String str) throws IOException {
		try {
			// sha1
			byte[] digest = MessageDigest.getInstance("SHA-1").digest(str.getBytes("UTF8"));
			// and base64
			return DatatypeConverter.printBase64Binary(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
	
	
	private static byte[] digest(byte[] str) throws IOException {
		try {
			return MessageDigest.getInstance("MD5").digest(str);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
	
	private boolean old;

	private String ip;
	private String path;
	private String wsKey;
	private byte[] oldKey;
	
	private boolean accepted;

	public final Socket sock;
	private final InputStream bin;
	private final NetInputStream in;
	private final OutputStream out;
	
	
	private byte[] readBuffer = new byte[1024*1024 + 4];
	private byte[] decompBuffer = new byte[1024*1024*4];
	private byte[] compBuffer = new byte[1024*1024*4];
	
	public boolean deflateEnabled = false;
	Inflater inflater = new Inflater(true);
	Deflater deflater = new Deflater(9, true);

	public WebSocket(Socket socket) throws IOException {
		sock = socket;
		ip = ((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString();
		bin = sock.getInputStream();
		in = new NetInputStream(bin);
		
		out = new BufferedOutputStream(sock.getOutputStream());
		
		
		sock.setSoTimeout(1000);
	}
	
	public String getIP() {
		return ip;
	}

	public String getPath() {
		return path;
	}
	
	private long getDigits(String gunwo) {
		String digits = "";
		for(int i=0; i< gunwo.length(); i++) {
			char c = gunwo.charAt(i);
			if(Character.isDigit(c))
				digits += c;
		}
		return Long.parseLong(digits);
	}
	
	private int getSpaces(String gunwo) {
		int spaces = 0;
		for(int i=0; i< gunwo.length(); i++) {
			char c = gunwo.charAt(i);
			if(c == ' ')
				spaces += 1;
		}
		return spaces;
	}
	
	public void readHeaders() throws IOException {
		try {
			LineReader lr = new LineReader(bin);
			
			// the first line of HTTP headers
			String line = lr.readLine();
			
			//System.out.println(line);
			if (line ==null || !line.startsWith("GET")) {
				throw new IOException("Wrong header: " + line);
			}
			
			path = line.split(" ")[1];
	
			// we read header fields
			String key = null;
	
			// read line by line until we get empty line
			while (!(line = lr.readLine()).isEmpty()) {
				
				if(line.contains(": ")) {
					String[] idx = line.split("\\: ",2);
					headers.put(idx[0].toLowerCase(), idx[1]);
				}
			}
//			while (!(line = br.readLine()).isEmpty()) {
//				System.out.println(line);
//			}
			
			if(headers.containsKey("sec-websocket-key")) {
				key = headers.get("sec-websocket-key");
				wsKey = key;
			}
			else if(headers.containsKey("sec-websocket-key1")) {
				System.out.println("Using old websocket");
				//System.out.println("New websocket");
				String key1 = headers.get("sec-websocket-key1");
				String key2 = headers.get("sec-websocket-key2");
				
				int spaces1 = getSpaces(key1);
				int spaces2 = getSpaces(key2);
				long digits1 = getDigits(key1);
				long digits2 = getDigits(key2);
				
				
				int k1 = (int) (digits1 / spaces1);
				int k2 = (int) (digits2 / spaces2);
				
				//System.out.println(digits1 + " / " + spaces1 + " = " + k1);
				//System.out.println(digits2 + " / " + spaces2 + " = " + k2);
				
				byte[] bytes = new byte[16];

				bytes[0] = (byte) ((k1>>24)& 0xFF);
				bytes[1] = (byte) ((k1>>16)& 0xFF);
				bytes[2] = (byte) ((k1>>8)& 0xFF);
				bytes[3] = (byte) (k1 & 0xFF);

				bytes[4] = (byte) ((k2>>24)& 0xFF);
				bytes[5] = (byte) ((k2>>16)& 0xFF);
				bytes[6] = (byte) ((k2>>8)& 0xFF);
				bytes[7] = (byte) (k2 & 0xFF);

				int r = in.read(bytes, 8, 8);
				if(r != 8)
					throw new EOFException("not 8");
				
				//System.out.println(hex(bytes));
				//System.out.println(in.available());
				oldKey = bytes;
				old = true;
			}
			else {
				throw new IOException("No Websocket key specified");
			}
			
			String extensions = headers.get("sec-websocket-extensions");
			if(extensions != null && extensions.contains("permessage-deflate")) {
				// TODO compression is broken
				//deflateEnabled = true;
			}
			
		} catch(IOException | RuntimeException e) {
			close();
			throw e;
		}
	}
	static final char[] hex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	private String hex(int val) {
		return hex[val/16] + "" + hex[val%16];
	}
	private String hex(byte[] bytes) {
		String hax = "";
		boolean first = true;
		for(byte b : bytes) {
			if(first)
				first=false;
			else
				hax += ":";
			int val = b & 0xFF;
			hax += hex[val/16];
			hax += hex[val%16];
		}
		return hax;
	}


	public void accept() throws IOException {
		try {
			sock.setSoTimeout(0);
			synchronized(out) {
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF8"));
		
				
				
				// send http headers
				pw.println("HTTP/1.1 101 WebSocket Protocol Handshake");
				//pw.println("Server: Java/" + System.getProperty("java.version") + " (" + System.getProperty("os.name") + ")");
				pw.println("Upgrade: WebSocket");
				pw.println("Connection: Upgrade");
				if(wsKey != null) {
					// add key and magic value
					String accept = wsKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			
					//sha1 and base64
					accept = digest(accept);
					pw.println("Sec-WebSocket-Accept: " + accept);
				} else {
					pw.println("Sec-WebSocket-Origin: " + headers.get("origin"));
					String location = "ws://" + headers.get("host") + path;
					pw.println("Sec-WebSocket-Location: " + location );
					//System.out.println(location);
					
				}
				
				if(deflateEnabled)
					pw.println("Sec-WebSocket-Extensions: permessage-deflate");
				pw.println();
				pw.flush();
				
				if(oldKey != null) {
					out.write(digest(oldKey));
					out.flush();
				}

				accepted = true;
			}
		} catch(IOException | RuntimeException e) {
			close();
			throw e;
		}

	}

	
	private void reject(String message) throws IOException {
		synchronized(out) {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF8"));
			pw.println("HTTP/1.1 403 Forbidden");
			
			String server = "Server: Java/" + System.getProperty("java.version") + " (" + System.getProperty("os.name") + ")"; 
			pw.println(server);
			pw.println("Connection: close");
			pw.println();
			pw.println(message); // this can only be seen in packet sniffer, sadly
			pw.flush();
		}
	}

	private void readFully(byte[] b, int length) throws IOException {

		int readen = 0;
		while (readen < length) {
			int r = in.read(b, readen, length - readen);
			if (r == -1)
				break;
			readen += r;
		}
	}
	
	/**
	 * Keeps reading until received message is string. Discards all other messages.
	 */
	public String readString() throws IOException, EOFException {
		while(true) {
			Object obj = read();
			if(obj instanceof String || obj == null)
				return (String) obj;
		}
	}
	
	byte[] buffer = new byte[1024*4]; // for old
	
	/**
	 * Tries to read frame from socket and return corresponding object
	 * This closes stream before rethrowing exceptions
	 * @return String if normal frame, byte[] if binary frame, ControlFrame if control frame, null if socket closed
	 * @throws EOFException on end of stream
	 * @throws IOException only if socket is not closed
	 * @throws DataFormatException 
	 */
	public Object read() throws IOException, EOFException {
		try {
			
			if(old) {
				sock.setSoTimeout(0); // no timeout
				int opcode = in.read();
				sock.setSoTimeout(1000); // we dont want to get stuck reading packet

				if(opcode == 0xFF) {
					return new ControlFrame(ControlFrame.OPCODE_CLOSE, null);
				}
				if(opcode != 0x00)
					throw new IOException("Expected 0x00, got 0x" + hex(opcode));
				
				int i = 0;
				while(true) {
					int read = in.read();
					if(read==0xFF) {
						return new String(buffer, 0, i, UTF8);
					} else {
						buffer[i++] = (byte) read;
					}
				}
				
				//throw new IOException("unimplemented");
			} else {
				sock.setSoTimeout(0); // no timeout
				int opcode = in.read();
				sock.setSoTimeout(1000); // we dont want to get stuck reading packet
				boolean whole = (opcode & 0b10000000) != 0;
				boolean rsv1 = (opcode & 0b01000000) != 0;
				
				System.out.println("RSV1: " + rsv1);
				
				if(!whole) {
					close(1003, "Fragmented messages not supported");
					throw new IOException("Fragmented messages not supported");
				}
				
				opcode = opcode & 0xF;
		
				int len = in.read();
				boolean encoded = (len >= 128);
		
				if (encoded)
					len -= 128;
		
				if (len == 127) {
					len = (in.read() << 56) | (in.read() << 48) | (in.read() << 40) | (in.read() << 32) | (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | in.read();
				} else if (len == 126) {
					len = (in.read() << 8) | in.read();
				}
				
				//System.out.println("Length got: " + len);
				
				if(len>1024*1024*4) {
					close(1008, "Message too long");
					throw new IOException("Message too long: " + len);
				}
		
				byte[] key = new byte[4];
		
				if (encoded) {
					readFully(key, key.length);
				}

				byte[] frame;// = new byte[len];
				
				
				
				if(rsv1) {
					System.out.println("decompressing! ?? " + encoded);
					
					//System.out.println("hex data: " + hex(frame));

					readFully(readBuffer, len);
					
					
					if (encoded) {
						for (int i = 0; i < len; i++) {
							readBuffer[i] = (byte) (readBuffer[i] ^ key[i % 4]);
						}
					}
					
//					data[frame.length+0] = (byte) 0x00;
//					data[frame.length+1] = (byte) 0x00;
//					data[frame.length+2] = (byte) 0xFF;
//					data[frame.length+3] = (byte) 0xFF;
					
					
					inflater.setInput(readBuffer, 0, len);
					
					
					System.out.println("remaining: " + inflater.getRemaining());
					System.out.println("wtf: " + inflater.getAdler());
					
					
					int bytes = inflater.inflate(decompBuffer);
					inflater.reset();
					
					System.out.println("Bytes deflated: " + bytes);
					
					
					frame = new byte[bytes];
					System.arraycopy(decompBuffer, 0, frame, 0, bytes);
					
					
				} else {
					frame = new byte[len];
					readFully(frame, len);
					

					if (encoded) {
						for (int i = 0; i < frame.length; i++) {
							frame[i] = (byte) (frame[i] ^ key[i % 4]);
						}
					}
				}
		
		
				
				
				
				switch(opcode) {
				case 0x01: // utf8 message
					return new String(frame, "UTF8");
				case 0x02: // binary message
					return frame;
				case 0x08: // close
					close(1000, null); // will send closing frame also
					return new ControlFrame(opcode, frame);
				case 0x09: //ping
					pong();
					return new ControlFrame(opcode, frame);
				case 0x0A: //pong
					return new ControlFrame(opcode, frame);
				}
				
				throw new IOException("Unknown opcode " + Integer.toHexString(opcode) + " / " + new String(frame, "UTF8"));
			}
		} catch(SocketException| DataFormatException | EOFException e) {
			System.out.println(e);
			close();
			return null;
		} catch (IOException | RuntimeException e) {
			close();
			throw e; // rethrow
		}
	}
	
	public void ping() throws IOException{
		send(null, 0x09);
	}
	
	public void pong() throws IOException{
		send(null, 0x0A);
	}
	

	public final void write(String data) throws IOException {
		if(data==null) {
			out.write(old? 0xFF : 0); // length 0
			return;
		}
		write(data.getBytes("UTF8"));
	}

	private final void write(byte[] data) throws IOException {
		write(data, data.length);
	}
	private final void write(byte[] data, int length) throws IOException {
		if (length > 65535) {
			out.write(127);

			out.write(length >>> 56);
			out.write(length >>> 48);
			out.write(length >>> 40);
			out.write(length >>> 32);
			
			out.write(length >>> 24);
			out.write(length >>> 16);
			out.write(length >>> 8);
			out.write(length);
		} else if (length > 125) {
			out.write(126);
			out.write(length >>> 8);
			out.write(length);
		} else {
			out.write(length);
		}
		out.write(data, 0, length);
	}
	
	public final void send(byte[] data) throws IOException {
		synchronized(out) {
			
			if(deflateEnabled) {
				out.write(0x02 | 0b10000000 | 0b01000000); // opcode 0x02, WHOLE, RSV1

				deflater.setInput(data);
				deflater.finish();
				int len = deflater.deflate(compBuffer);
				write(compBuffer, len);
				deflater.reset();
			} else {

				out.write(0x02 | 0b10000000); // opcode 0x02, WHOLE bit
				write(data);
			}
			out.flush();
		}
	}

	public final void send(String message) throws IOException {
		send(message, 0x01);
	}

	private final void send(String message, int opcode) throws IOException {
		synchronized(out) {
			if(sock.isClosed())
				return;
			if(old) {
				out.write(0x00);
				out.write(message.getBytes(UTF8));
				out.write(0xFF);
			} else {

				if(deflateEnabled) {
					out.write(opcode | 0b10000000 | 0b01000000); // opcode 0x02, WHOLE, RSV1
					byte[] data = message.getBytes("UTF8");
					deflater.setInput(data);
					deflater.finish();
					int len = deflater.deflate(compBuffer);
					System.out.println("Writing compressed data: " + len);
					write(compBuffer, len);
					deflater.reset();
				} else {
					out.write(opcode | 0b10000000); // opcode and WHOLE bit
					write(message);
				}
			}
			out.flush();
		}
	}
	
	/**
	 * Will send message and close a connection.
	 */
	public final void close(int code, String message) {
		if(sock.isClosed())
			return;
		try {
			
			if(accepted) {
				synchronized(out) {
					if(old) {
						
					} else {
						byte[] utf = message == null ? new byte[]{} : message.getBytes("UTF8");
						if(code<1000) code = 1000;
	
						out.write(0x08 | 0b10000000); // opcode and WHOLE bit
						out.write(2 + utf.length);
						out.write((code >>>8) & 0xFF);
						out.write(code  & 0xFF);
						out.write(utf);
						out.flush();
					}
				}
			}
			else
				reject(message);
		} catch (IOException e) {
			System.err.println("Error closing: " + e);
		} finally {
			close();
		}
	}

	public final void close() {
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	public boolean isClosed() {
		return sock.isClosed();
	}
}
