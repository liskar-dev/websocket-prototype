package icedev.ws.util;

public class Hex {
	static final char[] hex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static String hexByte(int val) {
		return hex[val/16] + "" + hex[val%16];
	}
	
	public static String hexBytes(byte[] bytes, int start, int length) {
		String hax = "";
		boolean first = true;
		for(int i=0; i<length; i++) {
			byte b = bytes[start+i];
			if(first)
				first=false;
			else
				hax += ":";
			int val = b & 0xFF;
			hax += hex[val/16];
			hax += hex[val%16];
		}
		return hax;
	}
	
	public static void printBytes(byte[] bytes, int start, int length) {
		System.out.println(hexBytes(bytes, start, length));
	}
}
