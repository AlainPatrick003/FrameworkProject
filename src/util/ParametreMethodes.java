package mg.itu.prom16.util;

import java.util.HashMap;
import java.util.Map;

public class ParametreMethodes {
    private Object[] methodParams;
    private Map<String, String> errorMap = new HashMap<>();
    private Map<String, Object> valueMap = new HashMap<>();

    public ParametreMethodes(Object[] methodParams, Map<String, String> errorMap, Map<String, Object> valueMap) {
        this.methodParams = methodParams;
        this.errorMap = errorMap;
        this.valueMap = valueMap;
    }

    public Object[] getMethodParams() {
        return methodParams;
    }

    public Map<String, String> getErrorMap() {
        return errorMap;
    }

    public Map<String, Object> getValueMap() {
        return valueMap;
    }
}
