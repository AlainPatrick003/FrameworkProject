package mg.itu.prom16.util;

import java.util.HashMap;
import java.util.Map;

public class Redirect {

    Map<String, Object> data = new HashMap<>();
    String lien = null;

    public Redirect() {
    }

    public Redirect(String lien) {
        // this.data.put("lien", lien);
        this.lien = lien;

    }

    public void setAction(String lien) {
        this.data.put("lien", lien);
    }

    public void add(String key, Object v) {
        this.data.put(key, v);
    }

    public Object get(String key) {
        return this.data.get(key);
    }

    public String getLien() {
        // return ((String) data.get("lien"));
        return this.lien;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
