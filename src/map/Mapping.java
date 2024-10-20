package mg.itu.prom16.map;

import java.lang.reflect.Method;

public class Mapping {
    String className;
    Method methodeName;
    String verb;

    public Mapping(String className, Method methodeName) {
        this.className = className;
        this.methodeName = methodeName;
    }

    public Mapping() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Method getMethodeName() {
        return methodeName;
    }

    public void setMethodeName(Method methodeName) {
        this.methodeName = methodeName;
    }

    
}
