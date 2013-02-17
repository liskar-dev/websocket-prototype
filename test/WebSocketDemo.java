
import java.io.IOException;

import websocket.*;

public class WebSocketDemo {

	
	public static void main(String[] args) throws Exception {
		System.setProperty("line.separator", "\r\n");

		WebSocketServer server = new WebSocketServer(8080);
		System.out.println("Listening on " + server.getLocalAddress());
		while(true) {
			WebSocket ws = server.listen();
			
			try {
				ws.readHeaders();
				ws.accept();
				
				while(true) {
					String packet = ws.readString();
					
					if(packet == null) {
						System.out.println("Null received, why?");
						break;
					}
					
					System.out.println("Received value: " + packet);
					
					ws.send("hellou");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				ws.close();
			}
		}
	}
}
