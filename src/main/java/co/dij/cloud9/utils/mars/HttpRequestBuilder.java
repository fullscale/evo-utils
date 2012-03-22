package co.diji.cloud9.utils.mars;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class HttpRequestBuilder {

    private String method = "GET"; 
    private String path = ""; 
    private String body = "";
    private String host = "localhost"; 
    private String port = "2600";
    private int timeout = 10000;
    private boolean zip = false;

    public HttpRequestBuilder() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(".settings"));
            host = properties.getProperty("mars.default.host", "localhost");
            port = properties.getProperty("mars.default.port", "2600");
        } catch (IOException e) {}
    }

    public HttpRequest build() {
        return new HttpRequest (
            method, 
            path, 
            body, 
            host, 
            port, 
            timeout,
            zip
        );
    }

    public HttpRequestBuilder method (String method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder path (String path) {
        this.path = path;
        return this;
    }

    public HttpRequestBuilder body (String body) {
        this.body = body;
        return this;
    }

    public HttpRequestBuilder host (String host) {
        this.host = host;
        return this;
    }

    public HttpRequestBuilder port (String port) {
        this.port = port;
        return this;
    }

    public HttpRequestBuilder timeout (int timeout) {
        this.timeout = timeout;
        return this;
    }

    public HttpRequestBuilder zip (boolean zip) {
        this.zip = zip;
        return this;
    }
}