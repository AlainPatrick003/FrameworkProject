package mg.itu.prom16.controllers;

import mg.itu.prom16.annotations.*;
import mg.itu.prom16.map.Mapping;
import mg.itu.prom16.views.ModelView;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
        PrintWriter out = response.getWriter();
        String[] requestUrlSplitted = request.getRequestURL().toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

        response.setContentType("text/html");
        if (error_message != null) {
            out.println( "<h3 style= 'color:red'>" + error_message + "</h3>");
            return;
        }      
        if (!lien.containsKey(controllerSearched)) {
            out.println("<p>" + "Method not found." + "</p>");
        } else {
            Mapping mapping = lien.get(controllerSearched);
            out.println("Methode trouvée dans: <strong> " + mapping.getClassName() + "</strong></br>");
            try {
                Class<?> classe = Class.forName(mapping.getClassName());
                Object o = classe.getDeclaredConstructor().newInstance();
                Method methode = classe.getDeclaredMethod(mapping.getMethodeName());
                Object resultat = methode.invoke(o);

                if (resultat instanceof ModelView) {
                    ModelView mv = (ModelView) resultat;
                    RequestDispatcher dispath = request.getRequestDispatcher(mv.getUrl());
                    Set<String> keys = mv.getData().keySet();
                    for (String key : keys) {
                        request.setAttribute(key, mv.getData().get(key));
                        break;
                        // ..
                    }

                    dispath.forward(request, response);

                }else if (resultat instanceof String) {
                    out.println("resultat de la methode: " + resultat.toString());
                    
                }else {
                    out.println( "<h3 style= 'color:red'> Type de retour non reconnu!</h3>");
                }
                
                
            } catch (Exception e) {
                out.println(e.getStackTrace());
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

    public void scan() throws Exception{
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

                                Method[] methodes = classe.getDeclaredMethods();

                                for (Method methode : methodes) {
                                    if (methode.isAnnotationPresent(Get.class)) {
                                        Mapping map = new Mapping(className, methode.getName());
                                        String valeur = methode.getAnnotation(Get.class).value();
                                        if (!lien.containsKey(valeur)) {
                                            lien.put(valeur, map);
                                            
                                        }else{
                                            throw new Exception("Methode '" + valeur + "' mifangaro");
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw e;
                        }

                    }
                }
            }else {
                throw new Exception(packagePath + " n'existe pas ou n'est pas un dossier!");

            }
        } catch (Exception e) {
            throw e;
        }
    }

    
}
