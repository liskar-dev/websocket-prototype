package icedev.ws;

import java.io.UnsupportedEncodingException;

public class ControlFrame {
	public static final int OPCODE_CLOSE = 0x08;
	
	public final int opcode;
	public final byte[] message;
	public final int offset;
	public final int length;
	
	ControlFrame(int opcode, byte[] message, int offset, int length) {
		this.opcode = opcode;
		this.message = message;
		this.offset = offset;
		this.length = length;
	}
	
	public String getMessage() {
		try {
			if(opcode == 0x08)
				return new String(message, offset+2, length-4, "UTF8");
			else
				return new String(message, offset, length, "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public int getCloseCode() {
		if(opcode!=0x08) return -1;
		if(message.length<2) return -1;
		return ((message[0] & 0xFF) << 8) | (message[1] & 0xFF);
	}
	
	
	@Override
	public String toString() {
		return "ControlFrame 0x" + (Integer.toHexString(opcode));
	}
}
