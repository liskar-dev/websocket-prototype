package websocket;

import java.io.UnsupportedEncodingException;

public class ControlFrame {
	public static final int OPCODE_CLOSE = 0x08;
	
	public final int opcode;
	public final byte[] message;
	
	ControlFrame(int opcode, byte[] message) {
		this.opcode = opcode;
		this.message = message;
	}
	
	public String getMessage() {
		try {
			if(opcode == 0x08)
				return new String(message, 2, message.length-2, "UTF8");
			else
				return new String(message, "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public int getCloseCode() {
		if(opcode!=0x08) return -1;
		if(message.length<2) return -1;
		return ((message[0] & 0xFF) << 8) | (message[1] & 0xFF);
	}
}
