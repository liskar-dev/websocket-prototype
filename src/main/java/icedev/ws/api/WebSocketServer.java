package icedev.ws.api;

import java.io.*;
import java.net.*;

import icedev.ws.WebSocket;
  
public class WebSocketServer {
	
	static final char[] heex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	public static final String hex(int dec) {
		return heex[dec/16] + "" + heex[dec%16];
	}
	
    private ServerSocket server;  
  
    public WebSocketServer(int port) throws IOException {
        server = new ServerSocket();
        server.setPerformancePreferences(0, 10, 1);
        server.bind(new InetSocketAddress((InetAddress) null, port));
    }
      
    public WebSocket listen() throws IOException {  
		Socket sock = server.accept();
		sock.setTcpNoDelay(true);
		return new WebSocket(sock);
    }  
    
    public void close() {
        try {
			server.close();
		} catch (IOException e) {
			System.err.println(e);
		}  
    }
    
    public boolean isClosed() {
    	return server.isClosed();
    }

	public int getPort() {
	    return server.getLocalPort();
    }  
}  
