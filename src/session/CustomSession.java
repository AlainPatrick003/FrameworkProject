package session;

import java.util.HashMap;

public class CustomSession {
    public HashMap<String, Object> values;

    public void add(String key, Object value) {
        this.values.put(key, value);
    } 

    public void delete(String key) {
        
    }
}
