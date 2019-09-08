
import java.util.zip.*;

import icedev.ws.WebSocket;
import icedev.ws.util.Hex;

public class ExperimentDeflaterInflater {

	public static void main(String[] args) throws DataFormatException {
		byte[] original = "Hello".getBytes();
		byte[] original2 = "Helle".getBytes();
		byte[] compressed = new byte[512];
		Deflater deflater = new Deflater(9, true);
		
		System.out.println("Original " + original.length);
		System.out.println(Hex.hexBytes(original, 0, original.length));
		
		deflater.setInput(original);
		int deflated = deflater.deflate(compressed, 0, 512, Deflater.SYNC_FLUSH);

		deflater.setInput(original2);
		int deflated2 = deflater.deflate(compressed, deflated, 512-deflated, Deflater.SYNC_FLUSH);
		
		System.out.println("Bytes writtern: " + deflated);
		String hex = Hex.hexBytes(compressed, 0, deflated);
		System.out.println(hex);
		System.out.println("Bytes writtern: " + deflated2);
		String hex2 = Hex.hexBytes(compressed, deflated, deflated2);
		System.out.println(hex2);
		
		
		
		byte[] decompressed = new byte[512];
		Inflater inf = new Inflater(true);
		
		
		inf.setInput(compressed, 0, deflated);
		int inflated = inf.inflate(decompressed);
		
		inf.setInput(compressed, deflated, deflated2);
		int inflated2 = inf.inflate(decompressed, inflated, compressed.length-inflated);

		System.out.println("Bytes read: " + inflated);
		String hec = Hex.hexBytes(decompressed, 0, inflated);
		System.out.println(hec);
		System.out.println("Bytes read: " + inflated2);
		String hec2 = Hex.hexBytes(decompressed, inflated, inflated2);
		System.out.println(hec2);
	}

}
