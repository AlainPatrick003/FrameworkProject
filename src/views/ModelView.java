package mg.itu.prom16.views;

import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data = new HashMap<>();

    public ModelView() {

    }

    public ModelView(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }

    
}
