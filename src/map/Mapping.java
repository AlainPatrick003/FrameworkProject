package mg.itu.prom16.map;

import java.util.ArrayList;
import java.util.List;

import mg.itu.prom16.util.VerbMethod;

public class Mapping {
    String className;
    List<VerbMethod> listeVerbMethode = new ArrayList<>();
    private boolean needAuth=false;
    private String profil;



    public Mapping(String className, List<VerbMethod> verbMethodes) {
        this.className = className;
        this.listeVerbMethode = verbMethodes;
    }

    public Mapping() {
    }
    public Mapping(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<VerbMethod> getListeVerbMethode() {
        return listeVerbMethode;
    }

    public void setListeVerbMethode(List<VerbMethod> listeVerbMethode) {
        this.listeVerbMethode = listeVerbMethode;
    }

    public void addVerbMethode(VerbMethod vm) {
        this.listeVerbMethode.add(vm);
    }

    public boolean contient(VerbMethod vm) {
        for (VerbMethod verbMethod : listeVerbMethode) {
            if (verbMethod.getMethode().equals(vm.getMethode()) && verbMethod.getVerb().equalsIgnoreCase(vm.getVerb())) {
                return true;
            }
        }

        return false;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public boolean needAuth() {
        return needAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }



    // public Method getMethodeName() {
    //     return methodeName;
    // }

    // public void setMethodeName(Method methodeName) {
    //     this.methodeName = methodeName;
    // }

    // public String getVerb() {
    //     return verb;
    // }

    // public void setVerb(String verb) {
    //     this.verb = verb;
    // }
    
}
