
import java.io.IOException;
import java.util.zip.Deflater;

import icedev.ws.*;
import icedev.ws.api.WebSocketServer;

public class TestingCompression {

	public TestingCompression() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		WebSocketServer server = new WebSocketServer(8080);
		System.out.println("Listening");
		
		
		byte[] bts = "hello world".getBytes("UTF-8");
		byte[] buf = new byte[2048];
		
		Deflater def = new Deflater(9, true);
		
		def.setInput(bts);
		def.finish();
		
		int bytes = def.deflate(buf);
		

		System.out.println("Hex: " + hex(bts, bts.length));
		System.out.println("Hex: " + hex(buf, bytes));
		
		
		
		
		WebSocket ws = server.listen();
		
		server.close();
		
		try {
			ws.readHeaders();
			
			String extensions = ws.headers.get("sec-websocket-extensions");
			
			if(extensions != null && extensions.contains("permessage-deflate")) {
//				ws.deflateEnabled = true;
			}
			//if(ws.headers.)
			
			ws.headers.forEach((k,v)->{
				System.out.printf("%s: %s\n", k, v);
			});
			
			ws.accept("dupa");
			
			
			Object message = ws.read();
			
			System.out.println("Received message: " + message);
			
			ws.send("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ws.close();
		}
	}
	
	
	static final char[] hex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	private static String hex(int val) {
		return hex[val/16] + "" + hex[val%16];
	}
	
	private static String hex(byte[] bytes, int len) {
		String hax = "";
		boolean first = true;
		for(int i=0; i< len; i++) {
			if(first)
				first=false;
			else
				hax += ":";
			int val = bytes[i] & 0xFF;
			hax += hex[val/16];
			hax += hex[val%16];
		}
		return hax;
	}

}
