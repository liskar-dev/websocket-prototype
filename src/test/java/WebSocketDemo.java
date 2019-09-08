
import java.io.IOException;
import java.nio.ByteBuffer;

import icedev.ws.*;
import icedev.ws.api.WebSocketServer;

public class WebSocketDemo {

	public static void main(String[] args) throws Exception {
		System.setProperty("line.separator", "\r\n");

		WebSocketServer server = new WebSocketServer(8080);
		System.out.println("Listening");
		while(true) {
			WebSocket ws = server.listen();
			
			try {
				ws.readHeaders();
				ws.accept("demo");
				
				while(true) {
					
					Object packet = ws.read();
					
					if(packet == null) {
						System.out.println("Null received, why?");
						break;
					}
					
					if(packet instanceof String) {
						System.out.println("Received String: " + packet);
						ws.send((String) packet);
					}
					
					if(packet instanceof byte[]) {
						System.out.println("Received byte[]: " + packet);
						ws.send((byte[]) packet);
					}
					
					if(packet instanceof ByteBuffer) {
						System.out.println("Received ByteBuffer: " + packet);
					}
					
					if(packet instanceof ControlFrame) {
						System.out.println("Received ControlFrame: " + packet);
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				ws.close();
			}
		}
	}
}
