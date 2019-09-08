package icedev.ws;

import java.util.HashMap;

@SuppressWarnings("serial")
public class CaseInsensitiveMap extends HashMap<String, String> {

    @Override
    public String put(String key, String value) {
       return super.put(key.toLowerCase(), value);
    }

    @Override
    public String get(Object key) {
       return super.get(((String)key).toLowerCase());
    }
    
    public String get(String key) {
       return super.get(key.toLowerCase());
    }
    
    @Override
    public boolean containsKey(Object key) {
    	if(key instanceof String)
    		super.containsKey(((String) key).toLowerCase());
    	return super.containsKey(key);
    }
    
    @Override
    public String remove(Object key) {
    	if(key instanceof String)
    		super.remove(((String) key).toLowerCase());
    	return super.remove(key);
    }
}
