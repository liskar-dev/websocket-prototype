package icedev.ws.util;

/**
 * Holds a byte[] buffer for reuse later.
 * Creates new buffer if higher capacity is needed.
 * @author Keray
 */
public class BufferProvider {
	public static int minimumCapacity = 1024; // 1kb
	public static int maximumCapacity = 1024 * 1024 * 4; // 4mb
	byte[] buffer;
	
	
	
	public byte[] getSafe(int capacity) {
		return get(capacity > maximumCapacity ? maximumCapacity : capacity);
	}
	
	public byte[] get(int capacity) {
		if(buffer == null || buffer.length < capacity) {
			if(capacity > maximumCapacity)
				throw new UnsupportedOperationException("Requested buffer capacity " + capacity + " is too high");
			capacity = Math.max(capacity, minimumCapacity);
//			System.out.println("Creating new buffer of capacity " + capacity);
			buffer = new byte[capacity];
		}
		return buffer;
	}
}
