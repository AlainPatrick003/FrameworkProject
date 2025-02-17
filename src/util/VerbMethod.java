package mg.itu.prom16.util;

import java.lang.reflect.Method;

public class VerbMethod {
    Method methode;
    String verb;
    public Method getMethode() {
        return methode;
    }
    public void setMethode(Method methode) {
        this.methode = methode;
    }
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }
    
    public VerbMethod(Method methode, String verb) {
        this.setMethode(methode);
        this.setVerb(verb);
    }

    public VerbMethod() {

    }
}
