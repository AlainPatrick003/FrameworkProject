package mg.itu.prom16.controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.Att;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.Model;
import mg.itu.prom16.annotations.Post;
import mg.itu.prom16.annotations.RequestParam;
import mg.itu.prom16.annotations.Url;
import mg.itu.prom16.map.Mapping;
import mg.itu.prom16.session.CustomSession;
import mg.itu.prom16.util.FileUpload;
import mg.itu.prom16.util.Utils;
import mg.itu.prom16.util.VerbMethod;
import mg.itu.prom16.views.ModelView;

@MultipartConfig
public class FrontController extends HttpServlet {

    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    boolean checked = false;
    HashMap<String, Mapping> lien = new HashMap<>();
    String error_message;

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        try {
            this.scan();
        } catch (Exception e) {
            error_message = e.getMessage();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        String[] requestUrlSplitted = request.getRequestURL().toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

        response.setContentType("text/html");
        if (error_message != null) {
            out.println("<h3 style= 'color:red'>" + error_message + "</h3>");
            response.sendError(500, error_message);
            return;
        }
        if (!lien.containsKey(controllerSearched)) {
            out.println("<p>" + "Method not found." + "</p>");
            // response.sendError(404, "Methode");
        } else {
            Mapping mapping = lien.get(controllerSearched);
            Method methode = null;
            // out.println("Methode trouvée dans: <strong> " + mapping.getClassName() + "
            // nombre de methode = "
            // + mapping.getListeVerbMethode().size() +
            // "</strong></br>");
            String verb = null;

            for (VerbMethod verbMethode : mapping.getListeVerbMethode()) {
                if (verbMethode.getVerb().equals(request.getMethod())) {
                    // out.println(verbMethode.getMethode() + " != " + request.getMethod() + " nom
                    // methode = " + verbMethode.getMethode() +"</br>");
                    verb = verbMethode.getVerb();
                    methode = verbMethode.getMethode();
                    mapping.setClassName(verbMethode.getMethode().getDeclaringClass().getName());
                }
            }

            try {

                Class<?> classe = Class.forName(mapping.getClassName());
                System.out.println("Classe maintenant = " + classe.getName());
                Object o = classe.getDeclaredConstructor().newInstance();
                Field[] fields = classe.getDeclaredFields();

                for (Field field : fields) {
                    if (field.getType().equals(CustomSession.class)) {
                        CustomSession cs = new CustomSession(request.getSession());
                        field.setAccessible(true);
                        if (field.get(o) == null) {
                            field.set(o, cs);
                        }

                        field.setAccessible(false);
                    }
                }

                Object[] listeAttribut = null;

                if (methode.getParameterCount() > 0) {
                    Parameter[] liste_paramettre = methode.getParameters();
                    // construire la liste d'objet
                    listeAttribut = getListeAttribut(liste_paramettre, request, out);
                }
                Object resultat = methode.invoke(o, listeAttribut);

                response.setContentType("text/json");
                if (resultat instanceof ModelView) {
                    ModelView mv = (ModelView) resultat;
                    RequestDispatcher dispath = request.getRequestDispatcher(mv.getUrl());
                    Set<String> keys = mv.getData().keySet();
                    for (String key : keys) {
                        request.setAttribute(key, mv.getData().get(key));
                        break;
                        // ..
                    }

                    if (Utils.isRestAPI(o, methode.getName())) {
                        String jsonResponse = gson.toJson(mv.getData());
                        out.println(jsonResponse);

                    } else {

                        dispath.forward(request, response);
                    }

                } else {
                    // out.println("resultat de la methode: " + resultat.toString());
                    String jsonResponse = gson.toJson(resultat);
                    out.println(jsonResponse);

                }

            } catch (Exception e) {
                // e.getStackTrace();
                e.printStackTrace();
                out.print("erreur: <h3 style= 'color:red'>" + e + "</h3>");
                response.sendError(500, e.getMessage());
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public void scan() throws Exception {
        System.out.println("Scaning");
        try {
            if (controllerPackage == null) {
                throw new Exception("controller-package null");

            }
            String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
            String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
            String packagePath = decodedPath + "\\" + controllerPackage.replace('.', '\\');
            File packageDirectory = new File(packagePath);
            if (packageDirectory.exists() && packageDirectory.isDirectory()) {
                File[] classFiles = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
                if (classFiles != null || classFiles.length == 0) {
                    for (File classFile : classFiles) {
                        String className = controllerPackage + '.'
                                + classFile.getName().substring(0, classFile.getName().length() - 6);
                        try {
                            Class<?> classe = Class.forName(className);
                            if (classe.isAnnotationPresent(Controller.class)) {
                                controller.add(classe.getSimpleName());

                                Method[] methodes = classe.getDeclaredMethods(); // Methodes rehetra ao anatin'ny
                                                                                 // controleur

                                for (Method methode : methodes) {
                                    if (methode.isAnnotationPresent(Url.class)) { // Raha misy Url
                                        // Mapping map = new Mapping(className, methode);
                                        Mapping map = new Mapping(className);
                                        String verb = "GET"; // Get par defaut

                                        if (methode.isAnnotationPresent(Post.class)) {
                                            // map.setVerb("POST");
                                            verb = "POST";
                                        }
                                        String valeur = methode.getAnnotation(Url.class).value(); // Maka ny url
                                                                                                  // andehanany
                                        VerbMethod vm = new VerbMethod(methode, verb);

                                        if (!lien.containsKey(valeur)) { // raha mbola tsy misy
                                            map.addVerbMethode(vm);
                                            lien.put(valeur, map);

                                        } else {
                                            // throw new Exception("Methode '" + valeur + "' mifangaro");
                                            Mapping mapping = lien.get(valeur);
                                            if (mapping.contient(vm)) {
                                                throw new Exception(
                                                        "L'url apparait plusieur fois avec la meme methode ");
                                            }

                                            mapping.addVerbMethode(vm);

                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw e;
                        }

                    }
                }
            } else {
                throw new Exception(packagePath + " n'existe pas ou n'est pas un dossier!");

            }
        } catch (Exception e) {
            throw e;
        }
    }

    public Object[] getListeAttribut(Parameter[] listeParamettre, HttpServletRequest request, PrintWriter out)
            throws Exception {

        Object[] reponse = new Object[listeParamettre.length];
        int i = 0;
        for (Parameter parameter : listeParamettre) {
            String value = "";
            if (!parameter.isAnnotationPresent(RequestParam.class)
                    && !parameter.getType().equals(CustomSession.class)) {
                throw new Exception("ETU2714 ==> Misy attribut tsy annoté\n");
            }
            if (parameter.getType().isAnnotationPresent(Model.class)) {
                // contruire une instance du model
                Object obj = parameter.getType().getDeclaredConstructor().newInstance();
                Field[] attributs = obj.getClass().getFields();
                for (Field field : attributs) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Att.class)) {
                        value = request.getParameter(field.getDeclaredAnnotation(Att.class).name());
                        field.set(obj, Utils.caster(value, field.getType()));
                    } else {
                        value = request.getParameter(field.getName());
                        field.set(obj, Utils.caster(value, field.getType()));
                    }

                    field.setAccessible(false);
                }

                reponse[i] = obj;

            } else {
                if (parameter.getType().equals(CustomSession.class)) {
                    reponse[i] = new CustomSession(request.getSession());
                }else if(parameter.getType().equals(FileUpload.class)){
                    String paramName = parameter.getDeclaredAnnotation(RequestParam.class).name();
                    System.out.println("ParamName = " + paramName);
                    reponse[i] = Utils.handleFileUpload(request, paramName);
                } 
                else {
                    value = request.getParameter(parameter.getDeclaredAnnotation(RequestParam.class).name());
                    reponse[i] = Utils.caster(value, parameter.getType());

                }
            }
            i++;
        }

        return reponse;

    }

}
