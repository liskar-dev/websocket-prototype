package websocket;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public abstract class WebSocketConnection {
	
	
	Thread receiveThread;
	Thread sendThread;
	
	protected int maxQueueSize = 128;
	
	public final WebSocket sock;

	public WebSocketConnection(WebSocket sock) {
		this.sock = sock;
	}
	
	protected Throwable cause;
	
	
	private synchronized void setCause(Throwable c) {
		if(cause == null)
			cause = c;
	}
	
	public void start() {
		receiveThread = new Thread("Receive Thread " + sock.getIP()) {
			@Override
			public void run() {
				runReceiving();
			}
		};
		sendThread = new Thread("Send Thread " + sock.getIP()) {
			@Override
			public void run() {
				runSending();
			}
		};
		receiveThread.start();
		sendThread.start();
	}
	
	
	protected void runReceiving() {
		try {
			while(!sock.isClosed()) {
				Object object = sock.read();
				onReceived(object);
			}
		} catch (Exception e) {
			if(!sock.isClosed())
				setCause(e);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close();
			onDisconnected();
		}
	}
	
	protected abstract void onReceived(Object message);
	protected abstract void onDisconnected();


	Queue<String> sendQueue = new LinkedList<>();
	
	public void send(String message) {
		synchronized(sendQueue) {
			if(sendQueue.size() > maxQueueSize) {
				close();
				return;
			}
			sendQueue.add(message);
			sendQueue.notify();
		}
	}
	
	
	protected void runSending() {
		try {
			while(!sock.isClosed()) {
				String message;
				synchronized(sendQueue) {
					while(sendQueue.isEmpty())
							sendQueue.wait();
					message = sendQueue.poll();
				}
				sock.send(message);
			}
		} catch (InterruptedException e) {
			//System.out.println("Sending thread interrupted " + sock.getIP());
		} catch (Exception e) {
			//e.printStackTrace();
			setCause(e);
		} finally {
			close();
		}
	}
	
	
	public void close() {
		sock.close();
		receiveThread.interrupt();
		sendThread.interrupt();
	}
	
	public boolean isClosed() {
		return sock.isClosed();
	}

}
