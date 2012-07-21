package co.diji.cloud9.utils.mars;

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
import java.util.*;
import java.io.*;
import java.lang.StringBuilder;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import java.util.Map;

public final class Mars {

    private static final String[] VALID_TYPES = {"conf", "html", "css", "images", "js", "controllers"};
    
    /**
     * Application entry point
     */
	public static void main (String[] args) {

        String command = null;
        String arg = null;
        File appRoot = getCwd();

        if (args.length == 2) {
            command = args[0];
            arg = args[1];

            // handle create
            if (command.equals("create")) {
                create(arg);

            // handle clone
            } else if (command.equals("clone")) {
                try {
                    clone(arg);
                } catch (Exception e) {}

            // handle push
            } else if (command.equals("push")) {
                try {
                    List<File> files = new ArrayList<File>();
                    if (!arg.startsWith("./")) {
                        arg = "./" + arg;
                    }
                    files.add(new File(arg));
                    push(files);
                } catch (Exception e) {}

            // handle pull
            } else if (command.equals("pull")) {
                try {
                    if (!arg.startsWith("./")) {
                        arg = "./" + arg;
                    }
                    pull(arg);
                } catch (Exception e) {} 

            // handle invalid command
            } else {
                showHelp();
            }

        } else if (args.length == 1) {
            command = args[0];

            // handle push (entire project)
            if (command.equals("push")) {
                try {
                    List<File> files = Mars.getFileListing(appRoot);
                    for (File f : files) {
                        System.out.println(f.getAbsolutePath());
                    }
                    push(files);
                } catch (Exception e) {}
            
            // hanlde pull (entire project)
            } else if (command.equals("pull")) {
                try {
                    pull();
                } catch (Exception e) {}

            // handle invalid command
            } else {
                showHelp();
            }

        // handle invalid args
        } else {
            showHelp();
        }
    }

    /**
     * Displays help message.
     */
    public static void showHelp () {
        System.out.println("usage: mars <command> [<args>]\n");
        System.out.println("The most commonly used mars commands are:");
        System.out.println("    clone      Clone an application into a new directory");
        System.out.println("    create     Create a new application from default template");
        System.out.println("    pull       Fetch application resources from a remote cluster");
        System.out.println("    push       Update remote application resources");
        System.out.println("");
    }

    static public void clone (
        String app
    ) throws IOException {
        createApp(app);
        
        HttpRequest request = new HttpRequestBuilder()
                                    .method("GET")
                                    .path(app)
                                    .zip(true)
                                    .build();

        Map<String, String> response = request.execute();
        
        for (String file : response.keySet()) {
            String code = response.get(file);

            String[] parts = file.split(File.separator);
            File dir = new File(app + File.separator + parts[0]);

            if (dir.exists() == false) {
                dir.mkdir();
            }

            FileWriter localResource = new FileWriter(app + File.separator + file);
            localResource.write(code);
            localResource.close();

            System.out.println("    [success] Creating resource: " + app + File.separator + file);
        }
    }

    static public void pull () throws IOException {
        // do a full export on the app
        String app = FilenameUtils.getName(System.getProperty("user.dir"));

        HttpRequest request = new HttpRequestBuilder()
                                    .method("GET")
                                    .path(app)
                                    .zip(true)
                                    .build();

        Map<String, String> response = request.execute();
        
        for (String file : response.keySet()) {
            String code = response.get(file);

            String[] parts = file.split(File.separator);
            File dir = new File(parts[0]);

            if (dir.exists() == false) {
                dir.mkdir();
            }

            FileWriter localResource = new FileWriter(file);
            localResource.write(code);
            localResource.close();

            System.out.println("    [success] Updating resource: " + file);
        }
    }

    public static void pull (
        String resource
    ) throws IOException {

        String app = FilenameUtils.getName(System.getProperty("user.dir"));
        String resourcePath = app + resource.substring(1, resource.length());

        HttpRequest request = new HttpRequestBuilder()
                                    .method("GET")
                                    .path(resourcePath)
                                    .build();

        Map<String, String> response = request.execute();

        // write file to proper location
        String code = response.get(resource);
        FileWriter localResource = new FileWriter(resource);
        localResource.write(code);
        localResource.close();

        System.out.println("    [success] Updating resource: " + resource);
    }

    /**
     * Pushes modified files to the cluster
     */
    public static void push (
        List<File> resources
    ) throws IOException {

        String app = FilenameUtils.getName(System.getProperty("user.dir"));

        for(File resource : resources ) {
            // translates file system path to valid URL
            String path = resource.getPath();
            String resourcePath = app + path.substring(1, path.length()); 

            // create JSON payload 
            JSONObject payload = new JSONObject();
            payload.put("mime", getMime(path));
            payload.put("code", read(path));

            HttpRequest request = new HttpRequestBuilder()
                                        .method("PUT")
                                        .path(resourcePath)
                                        .body(payload.toString())
                                        .contentType("application/json")
                                        .build();
            request.execute();

        }
    }

    /**
     * Creates the initial application directory structure + boilerplate
     */
    public static void create (String app) {

        System.out.println("Creating new application: " + app + "\n");
        System.out.println("Base Directory: " + System.getProperty("user.dir"));
        File appRoot = createApp(app);

        BoilerPlate boiler = new BoilerPlate(app);

        try {
            File images = mkdir(appRoot, "images");
            System.out.println("    [success] Created dir: " + images.getCanonicalPath());
        } catch (Exception e) {
           System.out.println("    [error] Failed to create resource: images"); 
        }

        try {
            File controllers = mkdir(appRoot, "controllers");
            System.out.println("    [success] Created dir: " + controllers.getCanonicalPath());
            String file = controllers.getCanonicalPath() + File.separator + "examples.js";
            FileWriter fstream = new FileWriter(file);
            fstream.write(boiler.controller());
            fstream.close();
            System.out.println("    [success] New resource: " + file);
        } catch (Exception e) {
            System.out.println("    [error] Failed to create resource: controller");
        }

        try {
            File css = mkdir(appRoot, "css");
            System.out.println("    [success] Created dir: " + css.getCanonicalPath());
            String file = css.getCanonicalPath() + File.separator + "style.css";
            FileWriter fstream = new FileWriter(file);
            fstream.write(boiler.css());
            fstream.close();
            System.out.println("    [success] New resource: " + file);
        } catch (Exception e) {
            System.out.println("    [error] Failed to create resource: css");
        }

        try {
            File html = mkdir(appRoot, "html");
            System.out.println("    [success] Created dir: " + html.getCanonicalPath());
            String file = html.getCanonicalPath() + File.separator + "index.html";
            FileWriter fstream = new FileWriter(file);
            fstream.write(boiler.html());
            fstream.close();
            System.out.println("    [success] New resource: " + file);
        } catch (Exception e) {
            System.out.println("    [error] Failed to create resource: html");
        }

        try {
            File js = mkdir(appRoot, "js");
            System.out.println("    [success] Created dir: " + js.getCanonicalPath());
            String file = js.getCanonicalPath() + File.separator + app + ".js";
            FileWriter fstream = new FileWriter(file);
            fstream.write(boiler.js());
            fstream.close();
            System.out.println("    [success] New resource: " + file);
        } catch (Exception e) {
            System.out.println("    [error] Failed to create resource: js");
        }

        try {
            FileWriter fstream = new FileWriter(app + File.separator + ".settings");
            fstream.write("mars.default.host=localhost\n");
            fstream.write("mars.default.port=2600\n");
            fstream.close();
            System.out.println("\nCreated project settings file.");
        } catch (IOException e) {}

        System.out.println("Created Cloud9 Application at " + 
            System.getProperty("user.dir") + File.separator + app);
    }

    /**
     * Creates the application directory structure and boilerplate code.
     */
    static public File createApp (String name) {
        File root = null;
        try {
            File cwd = getCwd();
            root = new File(cwd.getCanonicalPath() + File.separator + name);

            if (root.exists() == false) {
                root.mkdir();
            }
        } catch (Exception e) {
            System.out.println("Unable to create application structure.");
        }
        return root; 
    }

    /**
     * Returns the current working directory where this was executed.
     */
    public static File getCwd () {
        File cwd = null;
        try {
            cwd = new File (".");
        } catch (Exception e) {
            System.out.println("Unable to obtain current working directory.");
        }
        return cwd; 
    }

    /**
     * Creates a directory under the given root directory.
     */
    private static File mkdir (File root, String dirname) {
        File dir = null;
        try {
            dir = new File(root.getCanonicalPath() + File.separator + dirname);
            dir.mkdir();
        } catch (Exception e) {
            System.out.println("Unable to create directory: " + dirname);
        }
        return dir;
    }

    /**
    * Recursively walk a directory tree and return a List of all
    * Files found; the List is sorted using File.compareTo().
    */
    static public List<File> getFileListing (
        File aStartingDir
    ) throws FileNotFoundException {
        validateDirectory(aStartingDir);
        List<File> result = getFileListingNoSort(aStartingDir);
        Collections.sort(result);
        return result;
    }

    static private List<File> getFileListingNoSort (
        File aStartingDir
    ) throws FileNotFoundException {
        List<File> result = new ArrayList<File>();
        File[] filesAndDirs = aStartingDir.listFiles();
        List<File> filesDirs = Arrays.asList(filesAndDirs);
        for(File file : filesDirs) {
            if ( ! file.isFile() ) {
                //must be a directory
                //recursive call!
                List<File> deeperList = getFileListingNoSort(file);
                result.addAll(deeperList);
            } else {
                String type = file.getParent();
                if (type == null || type.length() < 3) {
                    type = "";
                } else {
                    type = type.substring(2);
                }
                
                if (Arrays.asList(VALID_TYPES).contains(type)) {
                    result.add(file); // only add files of valid type
                } else {
                    System.out.println("Skipping invalid file: " + file);
                }
            }
        }
        return result;
    }
    /**
    * Directory is valid if it exists, does not represent a file, and can be read.
    */
    static private void validateDirectory (
        File aDirectory
    ) throws FileNotFoundException {
        if (aDirectory == null) {
            throw new IllegalArgumentException (
                "Directory should not be null."
            );
        }
        if (!aDirectory.exists()) {
            throw new FileNotFoundException (
                "Directory does not exist: " + aDirectory
            );
        }
        if (!aDirectory.isDirectory()) {
            throw new IllegalArgumentException (
                "Is not a directory: " + aDirectory
            );
        }
        if (!aDirectory.canRead()) {
            throw new IllegalArgumentException (
                "Directory cannot be read: " + aDirectory
            );
        }
    }

    /** 
     * Read the contents of the given file. 
     */
    static private String read (String fileName) throws IOException {
        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner = new Scanner(new FileInputStream(fileName), "UTF-8");
        try {
            while (scanner.hasNextLine()){
                text.append(scanner.nextLine() + NL);
            }
        } finally {
            scanner.close();
        }
        return text.toString();
    }

    /**
     * Returns the mime-type for the given resource.
     */
    static private String getMime (String path) {

        String[] paths = path.split(File.separator);
        String dir = paths[paths.length-2];        
        String mimetype = "text/plain";

        if (dir.equals("html")) {
            mimetype = "text/html";

        } else if (dir.equals("css")) {
            mimetype = "text/css";

        } else if (dir.equals("js")) {
            mimetype = "application/javascript";

        } else if (dir.equals("images")) {
            String ext = FilenameUtils.getExtension(path);
            mimetype = "image/" + ext;

        } else if (dir.equals("controllers")) {
            mimetype = "application/javascript";
        }
        return mimetype;
    }

}