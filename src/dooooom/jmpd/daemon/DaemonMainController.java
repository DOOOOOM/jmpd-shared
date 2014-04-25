package dooooom.jmpd.daemon;

import java.io.*;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

public class DaemonMainController implements Runnable, RequestController {
    private static Properties daemonConfiguration;

    public static void main(String[] args) {
        DaemonMainController dmc = new DaemonMainController();
        dmc.run();
    }

    public void run() {
        daemonConfiguration = Configure();
        DaemonConnectionController dcc = new DaemonConnectionController(getPortNumber(), this);

        Thread dccThread = new Thread(dcc);
        dccThread.start();
        Player.setPlayQueue();
    }

    @Override
    public Map<String, Object> processRequest(Map<String, Object> request) {
        request.put("ack", "ack");
        return request;
    }

    private static Properties Configure() {
        Properties prop = new Properties();
        InputStream in = null;

        try {
            in = new FileInputStream(getDefaultConfigPath());
            prop.load(in);
        } catch (FileNotFoundException e) {
            System.out.println("No user configuration. Creating new file");
            createConfig();
        } catch (IOException e) {

        } finally {

        }
        return prop;
    }

    private static void createConfig() {
        File configFile, configDir;

        try {
            configDir = new File(getDefaultConfigPath());
            configFile = new File(getDefaultConfigPath() + "jmpd.properties");
            configDir.mkdirs();
            configFile.createNewFile();
            setDefaultConfiguration(configFile);
        } catch (IOException e) {

        }
    }

    private static void createDatabaseFile() {
        File dbFile;
        try {
            dbFile = new File(getDefaultConfigPath() + "database");
            dbFile.createNewFile();
            setBlankDatabase(dbFile);
        } catch (IOException e) {

        }
    }

    private static void setDefaultConfiguration(File config) {
        try {
            Properties props = new Properties();
            props.setProperty("TrackList", getDefaultConfigPath() + "database" );
            props.setProperty("Port", "" + 5005);
            props.setProperty("MusicFolder", getBasePath() + "Music");
            OutputStream out = new FileOutputStream(config);
            props.store(out, "");
        } catch (IOException e) {

        }
    }

    private static void setBlankDatabase(File db) {
        try {
            PrintWriter out = new PrintWriter(db);
            JsonGenerator gen = Json.createGenerator(out);
            gen.writeStartObject().writeEnd();
            gen.close();
        } catch (IOException e) {

        }
    }

    private static int getPortNumber() {
        int port = 5005;
        if (daemonConfiguration.getProperty("Port") != null) {
            port = Integer.parseInt(daemonConfiguration.getProperty("Port"));
        }
        return port;
    }

    public static String getMusicFolder() {
        if (daemonConfiguration.getProperty("MusicFolder") != null)
            return daemonConfiguration.getProperty("MusicFolder");
        else
            return null;
    }

    public static String getDatabasePath() {
            return daemonConfiguration.getProperty("Database");
    }

    public static String getBasePath() {
        String userName = System.getProperty("user.name");
        String s = System.getProperty("file.separator");
        String osName = System.getProperty("os.name");
        String basePath;

        if (osName.equalsIgnoreCase("windows 7")) {
            basePath = "C:" + s + "Users" + s + userName + s;
        } else if (osName.equalsIgnoreCase("windows 8")) {
            basePath = "C:" + s + "Users" + s + userName + s;
        } else if (osName.equalsIgnoreCase("windows vista")) {
            basePath = "C:" + s + "Users" + s + userName + s;
        } else if (osName.equalsIgnoreCase("windows xp")) {
            basePath = "C:" + s + "Documents and Settings" + s + userName + s + "My Documents" + s;
        } else if (osName.equalsIgnoreCase("linux")) {
            basePath = s + "home" + s + "" + userName.toLowerCase() + s;
        } else if (osName.equalsIgnoreCase("mac os") || osName.equalsIgnoreCase("mac os x")) {
            basePath = s + "Users" + s + userName.toLowerCase() + s;
        } else if (osName.equalsIgnoreCase("solaris")) {
            basePath = s + "home" + s + "" + userName.toLowerCase() + s;
        } else {
            System.err.println("Unable to determine operating system.");
            basePath = null;
        }
        return basePath;
    }

    public static String getDefaultConfigPath() {
        String configPath;
        String osName = System.getProperty("os.name");
        if (osName.equalsIgnoreCase("windows 7")) {
            configPath = getBasePath();
        } else if (osName.equalsIgnoreCase("windows 8")) {
            configPath = getBasePath();
        } else if (osName.equalsIgnoreCase("windows vista")) {
            configPath = getBasePath();
        } else if (osName.equalsIgnoreCase("windows xp")) {
            configPath = getBasePath();
        } else if (osName.equalsIgnoreCase("linux")) {
            configPath = getBasePath() + ".config/jmpd/";
        } else if (osName.equalsIgnoreCase("mac os") || osName.equalsIgnoreCase("mac os x")) {
            configPath = getBasePath();
        } else if (osName.equalsIgnoreCase("solaris")) {
            configPath = getBasePath() + ".config/jmpd/";
        } else {
            configPath = getBasePath();
        }
        return configPath;
    }
}
