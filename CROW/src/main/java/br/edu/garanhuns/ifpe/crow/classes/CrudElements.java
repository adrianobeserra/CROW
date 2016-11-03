/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.garanhuns.ifpe.crow.classes;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author casa01
 */
public class CrudElements<T> {

    private static final String TEXT = "text";
    private static final String SUBMIT = "submit";

    public static final String GET = "GET";
    public static final String POST = "POST";

    private String input(String name, String type, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("<input type=\"");
        sb.append(type);
        sb.append("\" name=\"");
        sb.append(name);
        sb.append("\" value=\"");
        sb.append(value);
        sb.append("\">");

        return sb.toString();
    }

    private String input(String name, String type, String value, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        sb.append(": <input type=\"");
        sb.append(type);
        sb.append("\" name=\"");
        sb.append(name);
        sb.append("\" value=\"");
        sb.append(value);
        sb.append("\">");

        return sb.toString();
    }

    public String create(Class classBean, String method, String action) {
        String m;
        m = method.isEmpty() ? CrudElements.POST : method;

        StringBuilder sb = new StringBuilder();
        sb.append("<form ");
        sb.append(" method=\"");
        sb.append(m);
        sb.append("\" action=\"");
        sb.append(action);
        sb.append("\"");
        sb.append(">");

        Field[] fields = classBean.getDeclaredFields();
        for (Field f : fields) {
            try {
                sb.append(this.input(f.getName(), CrudElements.TEXT, "", f.getName()));
            } catch (SecurityException | IllegalArgumentException ex) {
                Logger.getLogger(CrudElements.class.getName()).log(Level.SEVERE, null, ex);
            }

            sb.append("<br/>");
        }
        sb.append(this.input("", CrudElements.SUBMIT, "enviar"));
        sb.append("</form>");

        return sb.toString();
    }

    public String update(T objectBean, String method, String action) {
        Class classBean;
        classBean = objectBean.getClass();
        String m;
        m = method.isEmpty() ? CrudElements.POST : method;
        StringBuilder sb = new StringBuilder();
        sb.append("<form ");
        sb.append(" method=\"");
        sb.append(m);
        sb.append("\" action=\"");
        sb.append(action);
        sb.append("\"");
        sb.append(">");

        Field[] fields = classBean.getDeclaredFields();
        for (Field f : fields) {
            try {
                Method meth = classBean.getMethod("get" + StringUtil.upperCaseFirst(f.getName()));
                String value = String.valueOf(meth.invoke(objectBean)).equals("null") ? "" : String.valueOf(meth.invoke(objectBean));
                sb.append(this.input(f.getName(), CrudElements.TEXT, value, f.getName()));
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                Logger.getLogger(CrudElements.class.getName()).log(Level.SEVERE, null, ex);
            }
            sb.append("<br/>");
        }
        sb.append(this.input("", CrudElements.SUBMIT, "enviar"));
        sb.append("</form>");
        return sb.toString();
    }

    public String list(Class classBean, List<T> list) throws TemplateException {
        Configuration cfg = new Configuration();
        Writer out = new StringWriter();
        try {
            TemplateLoader templateLoader = new ClassTemplateLoader(getClass(), "/templates/");
            cfg.setTemplateLoader(templateLoader);
            Template template = cfg.getTemplate("crud.html");
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("crudColunas", this.getBeanNames(classBean));
            data.put("crudValores", list);
            template.process(data, out);
            out.flush();
        } catch (MalformedTemplateNameException ex) {
            Logger.getLogger(CrudElements.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException | TemplateException ex) {
            Logger.getLogger(CrudElements.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CrudElements.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out.toString();
    }

    private List<String> getBeanNames(Class classBean) {
        List<String> beanNames = new ArrayList<>();
        Field[] fields = classBean.getDeclaredFields();
        for (Field f : fields) {
            try {
                beanNames.add(f.getName());
            } catch (SecurityException | IllegalArgumentException ex) {
                Logger.getLogger(CrudElements.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return beanNames;
    }
    
    private List<String> getBeanValues(List<T> list, Class classBean) {
        List<String> beanValues = new ArrayList<>();
        for (Object objectBean : list) {
            for(String atributte: this.getBeanNames(classBean)){
                Method meth;
                try {
                    meth = classBean.getMethod("get"+StringUtil.upperCaseFirst(atributte));
                    String value = String.valueOf(meth.invoke(objectBean)).equals("null")?"":String.valueOf(meth.invoke(objectBean));
                    beanValues.add(value);
                } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                    Logger.getLogger(CrudElements.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return beanValues;
    }
}
