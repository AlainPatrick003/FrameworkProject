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
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.Att;
import mg.itu.prom16.annotations.Auth;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.Model;
import mg.itu.prom16.annotations.Post;
import mg.itu.prom16.annotations.RequestParam;
import mg.itu.prom16.annotations.Url;
import mg.itu.prom16.annotations.Validation.*;
import mg.itu.prom16.map.Mapping;
import mg.itu.prom16.session.CustomSession;
import mg.itu.prom16.views.ModelView;
import mg.itu.prom16.util.*;

@MultipartConfig
public class FrontController extends HttpServlet {

    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    boolean checked = false;
    HashMap<String, Mapping> lien = new HashMap<>();
    String error_message;
    private String hote_name;

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        this.hote_name = getInitParameter("auth");  
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
        // A effacer
        request.getSession().setAttribute(this.hote_name, "admin");
        // ******

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

                ParametreMethodes params = null;
                Object[] listeAttribut = null;
                if (methode.getParameterCount() > 0) {
                    System.out.println("Mmiditra ato foana");
                    Parameter[] liste_paramettre = methode.getParameters();
                    // construire la liste d'objet
                    params = getListeAttribut(liste_paramettre, request, out);
                    if (!params.getErrorMap().isEmpty()) {
                        // Retrieve the previous ModelView from session if it exists
                        ModelView previousModelView = (ModelView) request.getSession().getAttribute("page_precedent");
                        System.out.println("Previous = " + previousModelView.getUrl());
                        if (previousModelView != null) {
                            previousModelView.mergeValidationErrors(params.getErrorMap());
                            previousModelView.mergeValidationValues(params.getValueMap());
                            Utils.sendModelView(previousModelView, request, response);
                            // return previousModelView;
                        }
                    }
                    listeAttribut = params.getMethodParams();

                }
                Utils.checkAuthProfil(mapping, request, hote_name);
                Object resultat = methode.invoke(o, listeAttribut);

                response.setContentType("text/json");
                if (resultat instanceof ModelView) {
                    
                    request.getSession().setAttribute("page_precedent", resultat);
                    Utils.sendModelView((ModelView)resultat, request, response);

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
                            boolean needAuth = false;
                            String profile = "";
                            Class<?> classe = Class.forName(className);
                            if (classe.isAnnotationPresent(Controller.class)) {
                                if (classe.isAnnotationPresent(Auth.class)) {
                                    needAuth = true;
                                    profile = classe.getDeclaredAnnotation(Auth.class).value();
                                }
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
                                            map.setNeedAuth(needAuth);
                                            map.setProfil(profile);
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

    public ParametreMethodes getListeAttribut(Parameter[] listeParamettre, HttpServletRequest request, PrintWriter out)
            throws Exception {

        Object[] reponse = new Object[listeParamettre.length];
        ParametreMethodes params = null;
        Map<String, String> errorMap = new HashMap<>();
        Map<String, Object> valueMap = new HashMap<>();
        int i = 0;
        for (Parameter parameter : listeParamettre) {
            String value = "";
            String paramName = "";
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
                paramName = parameter.getDeclaredAnnotation(RequestParam.class).name();
                if (parameter.getType().equals(CustomSession.class)) {
                    reponse[i] = new CustomSession(request.getSession());
                } else if (parameter.getType().equals(FileUpload.class)) {
                    
                    System.out.println("ParamName = " + paramName);
                    reponse[i] = Utils.handleFileUpload(request, paramName);
                } else {
                    value = request.getParameter(parameter.getDeclaredAnnotation(RequestParam.class).name());
                    reponse[i] = Utils.caster(value, parameter.getType());

                }
            }
            // *** validation **//
            if (parameter.isAnnotationPresent(Valid.class)) {
                List<ResponseValidation> errors = mg.itu.prom16.util.Contraintes.validateObject(reponse[i]);
                for (ResponseValidation responseValidation : errors) {
                    if (!responseValidation.getErrors().isEmpty()) {
                        errorMap.put("error_" + responseValidation.getInputName(),
                                String.join(", ", responseValidation.getErrors()));
                        valueMap.put("value_" + responseValidation.getInputName(),
                                responseValidation.getValue());
                    }
                }
            } else {
                List<ResponseValidation> errors = mg.itu.prom16.util.Contraintes.valider(reponse[i],
                        parameter.getAnnotations(),
                        paramName);
                if (!errors.get(0).getErrors().isEmpty()) {
                    System.out.println("ERror_" + paramName);

                    errorMap.put("error_" + paramName, String.join(", ", errors.get(0).getErrors()));
                    valueMap.put("value_" + paramName, reponse[i]);
                }
            }
            i++;
        }

        params = new ParametreMethodes(reponse, errorMap, valueMap);

        return params;

    }

}
