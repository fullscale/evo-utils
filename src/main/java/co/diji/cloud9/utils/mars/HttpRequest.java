package co.diji.cloud9.utils.mars;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class HttpRequest {

	private String method;
    private String path;
    private String body;
    private String host;
    private String port;
    private String contentType;
    private int timeout;
    private boolean zip;

    private HttpURLConnection connection;
    private OutputStreamWriter request;
    private BufferedReader rd;
    private StringBuilder sb;
    private String line;
    private URL serverAddress;

	public HttpRequest (
        String method, 
        String path, 
        String body, 
        String host, 
        String port,
        String contentType,
        int timeout,
        boolean zip
    ) {
        this.method = method;
        this.path = path;
        this.body = body;
        this.host = host;
        this.port = port;
        this.contentType = contentType;
        this.timeout = timeout;
        this.zip = zip;
	}

    public Map<String, String> execute() {

        Map<String, String> files = new HashMap<String, String>();

        try {

            // PUT /v1/apps/{app}/{dir}/{id}
            serverAddress = new URL("http://" + host + ":" + port + "/v2/apps/" + path);

            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod(method);

            // these two methods will require output
            if (method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")) {
                connection.setDoOutput(true);
                if (!contentType.equals("")) {
                    connection.setRequestProperty("Content-Type", contentType);
                }
            }

            connection.setReadTimeout(timeout);
            connection.setRequestProperty("Connection", "close");
            connection.connect();

            if (method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")) {
                //get the output stream writer and write the output to the server
                request = new OutputStreamWriter(connection.getOutputStream());
                request.write(body);
                request.flush();
            }

            if (connection.getResponseCode() != 200) {
                System.out.println("Operation failed: " + path);
            }
        
            //read the result from the server
            if (zip == false) {
                rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();

                while ((line = rd.readLine()) != null) {
                    sb.append(line + '\n');
                }

                String[] parts = path.split(File.separator);
                String resource = "./" + parts[1] + File.separator + parts[2];
                files.put(resource, sb.toString());

            // returned a zip file
            } else {

                ZipInputStream zip = new ZipInputStream(new BufferedInputStream(connection.getInputStream()));
                ZipEntry entry = null;

                while ((entry = zip.getNextEntry()) != null) {

                    if (entry.isDirectory()) { continue; }
                    String[] pathParts = entry.getName().split(File.separator);
                    String resource = pathParts[1] + File.separator + pathParts[2];
                    files.put(resource, IOUtils.toString(zip, "UTF-8"));
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return files;
    }
}