
import java.io.IOException;

import icedev.ws.*;
import icedev.ws.api.WebSocketServer;

public class DumbConnectionTest {

	public static void main(String[] args) throws Exception {
		WebSocketServer server = new WebSocketServer(26001);
		
		try {
			while(!server.isClosed()) {
				System.out.println("Listening for connections...");
				
				try {
					WebSocket socket = server.listen();
					
					System.out.println("Got something");

					try {
						WebSocket.PRINT_HEADERS = true;
						socket.readHeaders();
						String path = socket.getPath();
						
						if(!path.matches("/[a-zA-Z0-9]+")) {
							System.err.println("Weird path: " + path);
						}

						System.out.println("Connection from " + socket.getIP() + " to " + path);
						
//						for(String a : socket.headers.keySet()) {
//							System.out.println(a);
//						}
						
						socket.accept("gunwo");
						
						DumbConnection sc = new DumbConnection(socket);
						sc.start();
					} catch(Exception e) {
						e.printStackTrace();
						socket.close();
					}
					
				} catch(IOException e) {
					if(server.isClosed())
						return;
					
					e.printStackTrace();
					server.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			System.out.println("End.");
		}
	}
}
