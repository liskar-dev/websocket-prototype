package icedev.ws.api;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import icedev.ws.WebSocket;

public abstract class WebSocketConnection {
	public static final long THREAD_STACK_IN = 1024;
	public static final long THREAD_STACK_OUT = 1024;
	
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
		receiveThread = new Thread(null, null, "Receive Thread " + sock.getIP(), THREAD_STACK_IN) {
			@Override
			public void run() {
				runReceiving();
			}
		};
		sendThread = new Thread(null, null, "Send Thread " + sock.getIP(), THREAD_STACK_OUT) {
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

	Queue<Object> sendQueue = new LinkedList<>();
	
	public void send(Object message) {
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
				Object message;
				
				synchronized(sendQueue) {
					while(sendQueue.isEmpty())
							sendQueue.wait();
					message = sendQueue.poll();
				}
				
				if(message instanceof String)
					sock.send((String)message);
				if(message instanceof ByteBuffer) {
					ByteBuffer buf = (ByteBuffer) message;
					if(buf.isDirect())
						throw new RuntimeException("no direct byte buffers please");
					byte[] data = buf.array();
					int offset = buf.arrayOffset();
					int length = buf.remaining();
					sock.send(data, offset, length);
				}
				if(message instanceof byte[]) {
					byte[] data = (byte[]) message;
					sock.send(data, 0, data.length);
				}
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
