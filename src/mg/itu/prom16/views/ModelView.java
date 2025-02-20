package mg.itu.prom16.views;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String url;
    private HashMap<String, Object> data = new HashMap<>();
    private Map<String, String> validationErrors = new HashMap<>();
    private Map<String, Object> validationValues = new HashMap<>();


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

    public void addError(String key, String errorMessage) {
        this.validationErrors.put(key, errorMessage);
    }

    public Map<String, String> getValidationErrors() {
        return this.validationErrors;
    }

    public void addValidationValue(String key, Object value) {
        this.validationValues.put(key, value);
    }

    public Map<String, Object> getValidationValues() {
        return this.validationValues;
    }

    public void mergeValidationErrors(Map<String, String> errors) {
        if (errors != null) {
            this.validationErrors=errors;
        }
    }

    public void mergeValidationValues(Map<String, Object> values) {
        if (values != null) {
            this.validationValues=values;
        }
    }

    
}
