
import java.nio.ByteBuffer;

import icedev.ws.*;
import icedev.ws.api.WebSocketConnection;
import icedev.ws.util.Hex;

public class DumbConnection extends WebSocketConnection {

	public DumbConnection(WebSocket sock) {
		super(sock);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onReceived(Object message) {
		if(message instanceof String) {
			System.out.println("String: " + message);
		}
		else if(message instanceof ByteBuffer) {
			ByteBuffer buff = (ByteBuffer) message;
			System.out.print("byte[]: ");
			
			byte[] data = buff.array();
			int length = buff.limit();
			Hex.printBytes(data, 0, length);
		} else {
			System.out.println("Other: " + message);
		}
	}

	@Override
	protected void onDisconnected() {
		System.out.println("Disconnected " + sock.getIP());
	}
	
	@Override
	public void start() {
		super.start();
		
		send("Welcome1");
		send("Welcome2");
		send("Welcome3");
		send("Welcome4");
		
//		send("test server message");
//		send("test server message3");
		
		byte[] dupa = new byte[] {0x11, 0x22, 0x33, 0x44};
		
		ByteBuffer bb = ByteBuffer.wrap(dupa);
		send(bb);
		send(bb);
		send(bb);
		send(bb);
	}

}
