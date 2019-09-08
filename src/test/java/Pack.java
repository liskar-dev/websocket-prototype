
public class Pack {

	
	
	public static String pack(String format, Number...values) {
		
		StringBuilder packed = new StringBuilder(format.length() * 5);
		
		for(int i=0; i<format.length(); i++) {
			char c = format.charAt(i);
			
			switch(c) {
			case 's':{
					short val = values[i].shortValue();
					packed.append((char) (val & 0xF));
					packed.append((char) (val >> 4 & 0xF));
					packed.append((char) (val >> 8 & 0xF));
					packed.append((char) (val >> 12 & 0xF));
				} break;

			case 'i':{
					int val = values[i].intValue();
					packed.append((char) (val & 0xF));
					packed.append((char) (val >> 4 & 0xF));
					packed.append((char) (val >> 8 & 0xF));
					packed.append((char) (val >> 12 & 0xF));
					packed.append((char) (val >> 16 & 0xF));
					packed.append((char) (val >> 20 & 0xF));
					packed.append((char) (val >> 24 & 0xF));
					packed.append((char) (val >> 28 & 0xF));
				} break;

			case 'f':{
					int val = (int) (values[i].floatValue() * (65536>>1));
					packed.append((char) (val & 0xF));
					packed.append((char) (val >> 4 & 0xF));
					packed.append((char) (val >> 8 & 0xF));
					packed.append((char) (val >> 12 & 0xF));
					packed.append((char) (val >> 16 & 0xF));
					packed.append((char) (val >> 20 & 0xF));
					packed.append((char) (val >> 24 & 0xF));
					packed.append((char) (val >> 28 & 0xF));
				} break;
			default:
				throw new IllegalArgumentException("Unknown format: " + c);
			}
		}
		
		return packed.toString();
	}
	
	private int index;
	private final String packed;
	
	Pack(String data) {
		if(data == null)
			throw new NullPointerException("data is null");
		packed = data;
	}
	
	public int nextInt() {
		int val = packed.charAt(index++) & 0xF;
		val |= (packed.charAt(index++) & 0xF) << 4;
		val |= (packed.charAt(index++) & 0xF) << 8;
		val |= (packed.charAt(index++) & 0xF) << 12;
		val |= (packed.charAt(index++) & 0xF) << 16;
		val |= (packed.charAt(index++) & 0xF) << 20;
		val |= (packed.charAt(index++) & 0xF) << 24;
		val |= (packed.charAt(index++) & 0xF) << 28;
		return val;
	}
	
	public float nextFloat() {
		float val = nextInt();
		return val / (65536>>1);
	}
	
	public short nextShort() {
		short val = (short) (packed.charAt(index++) & 0xFF);
		val |= (packed.charAt(index++) & 0xFF) << 4;
		val |= (packed.charAt(index++) & 0xFF) << 8;
		val |= (packed.charAt(index++) & 0xFF) << 12;
		return val;
	}
	
	public static Pack unpack(String packed) {
		return new Pack(packed);
	}
}
