package icedev.ws.api;

import java.nio.ByteBuffer;

import icedev.ws.ControlFrame;

public interface WSListener {
	public void onMessage(ByteBuffer message);
	public void onMessage(String message);
	public void onMessage(ControlFrame frame);
}
