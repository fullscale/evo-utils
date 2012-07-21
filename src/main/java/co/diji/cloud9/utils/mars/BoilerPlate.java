package co.diji.cloud9.utils.mars;

import java.io.*;
import java.net.URL;
import java.util.Properties;

import java.io.StringWriter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

public class BoilerPlate {

    VelocityContext context = null;

    public BoilerPlate(String app) {
        try {
            Properties p = new Properties();
            p.put("resource.loader", "class");
            p.put("class.resource.loader.class", 
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init(p);
            context = new VelocityContext();
            context.put("app", app);
        } catch (Exception e) {}
    }

    public String controller() {
        return run("controller.vm");
    }

    public String css() {
        return run("css.vm");
    }

    public String html() {
        return run("html.vm");
    }

    public String js() {
        return run("js.vm");
    }

    private String run(String tmpl) {
        StringWriter sw = null;
        try {
            Template template = Velocity.getTemplate(tmpl);
            sw = new StringWriter();
            template.merge(context, sw);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return sw.toString();
    }
}