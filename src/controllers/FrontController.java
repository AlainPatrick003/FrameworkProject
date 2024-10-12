package mg.itu.prom16.controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.annotations.Att;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.Get;
import mg.itu.prom16.annotations.Model;
import mg.itu.prom16.annotations.RequestParam;
import mg.itu.prom16.map.Mapping;
import mg.itu.prom16.views.ModelView;
import mg.itu.prom16.session.CustomSession;

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
            out.println("<h3 style= 'color:red'>" + error_message + "</h3>");
            return;
        }
        if (!lien.containsKey(controllerSearched)) {
            out.println("<p>" + "Method not found." + "</p>");
        } else {
            Mapping mapping = lien.get(controllerSearched);
            // out.println("Methode trouvée dans: <strong> " + mapping.getClassName() +
            // "</strong></br>");
            try {
                Class<?> classe = Class.forName(mapping.getClassName());
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

                Method methode = mapping.getMethodeName();
                Object[] listeAttribut = null;

                if (methode.getParameterCount() > 0) {
                    Parameter[] liste_paramettre = methode.getParameters();
                    // construire la liste d'objet
                    listeAttribut = getListeAttribut(liste_paramettre, request, out);
                }
                Object resultat = methode.invoke(o, listeAttribut);

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

                } else if (resultat instanceof String) {
                    out.println("resultat de la methode: " + resultat.toString());

                } else {
                    out.println("<h3 style= 'color:red'> Type de retour non reconnu!</h3>");
                }

            } catch (Exception e) {
                // e.getStackTrace();
                out.print("<h3 style= 'color:red'>" + e + "</h3>");
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
                                        Mapping map = new Mapping(className, methode);
                                        String valeur = methode.getAnnotation(Get.class).value();
                                        if (!lien.containsKey(valeur)) {
                                            lien.put(valeur, map);

                                        } else {
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
                        field.set(obj, caster(value, field.getType()));
                    } else {
                        value = request.getParameter(field.getName());
                        field.set(obj, caster(value, field.getType()));
                    }

                    field.setAccessible(false);
                }

                reponse[i] = obj;

            } 
            else {
                if (parameter.getType().equals(CustomSession.class)) {
                    reponse[i] = new CustomSession(request.getSession());
                }else {
                    value = request.getParameter(parameter.getDeclaredAnnotation(RequestParam.class).name());
                    reponse[i] = caster(value, parameter.getType());

                }
            }
            i++;
        }

        return reponse;

    }

    public Object caster(String value, Class<?> classe) {

        if (classe == int.class || classe == Integer.class) {
            return Integer.parseInt(value);
        } else if (classe == long.class || classe == Long.class) {
            return Long.parseLong(value);
        } else if (classe == double.class || classe == Double.class) {
            return Double.parseDouble(value);
        } else if (classe == float.class || classe == Float.class) {
            return Float.parseFloat(value);
        } else if (classe == boolean.class || classe == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (classe == byte.class || classe == Byte.class) {
            return Byte.parseByte(value);
        } else if (classe == short.class || classe == Short.class) {
            return Short.parseShort(value);
        } else if (classe == char.class || classe == Character.class) {
            if (value.length() != 1) {
                throw new IllegalArgumentException("Cannot convert string to char: " + value);
            }
            return value.charAt(0);
        } else if (classe == LocalDate.class) {
            return LocalDate.parse(value);
        } else {
            return value;
        }

        //done

    }
}
