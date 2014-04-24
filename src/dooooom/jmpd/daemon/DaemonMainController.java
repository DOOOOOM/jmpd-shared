package dooooom.jmpd.daemon;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class DaemonMainController implements Runnable, RequestController {
    private static Properties daemonConfiguration = Configure();

    public static void main(String[] args) {
        DaemonMainController dmc = new DaemonMainController();
        dmc.run();
    }

    public void run() {
        DaemonConnectionController dcc = new DaemonConnectionController(getPortNumber(), this);

        Thread dccThread = new Thread(dcc);
        dccThread.start();
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
            in = new FileInputStream("jmpd.properties");
            prop.load(in);
        } catch (FileNotFoundException e) {
            System.out.println("No user configuration. Creating new file");
            createConfig();
        } catch (IOException e) {

        } finally {
            try {
                in.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        return prop;
    }

    private static void createConfig() {
        String userName = System.getProperty("user.name");
        String s = System.getProperty("file.separator");
        String osName = System.getProperty("os.name");

        File configFile;

        String linuxConfig = s + "home" + s + "" + userName.toLowerCase() + s + ".config" + s + "jmpd" + s + "jmpd.properties";
        String windows7Config = "C:" + s + "Users" + s + userName + s + "jmpd.properties";
        String windows8Config = windows7Config;
        String windowsVistaConfig = windows7Config;
        String windowsXPConfig = "C:" + s + "Documents and Settings" + s + userName + s + "My Documents" + s + "jmpd.properties";
        String solarisConfig = linuxConfig;
        String macConfig = s + "Users" + s + userName.toLowerCase() + s + "jmpd.properties";

        String windows7MusicFolderName = "C:" +s+ "Users" +s+ userName +s+ "Music";
        String windows8MusicFolderName = windows7MusicFolderName;
        String windowsVistaMusicFolderName = windows7MusicFolderName;
        String windowsXPMusicFolderName = "C:" +s+ "Documents and Settings" +s+ userName +s+ "My Documents" +s+ "My Music";
        String linuxMusicFolderName = s+ "home" +s+ "" + userName.toLowerCase() +s+ "music";
        String solarisMusicFolderName = linuxMusicFolderName;
        String macMusicFolderName = "/Users/" + userName.toLowerCase() + "/Music";

        String basePath;
        String musicPath;

        try {
            if (osName.equalsIgnoreCase("windows 7")) {
                basePath = "C:" + s + "Users" + s + userName + s;
                musicPath = windows7MusicFolderName;
                configFile = new File(windows7Config);
                configFile.mkdirs();
                configFile.createNewFile();
                setDefaultConfiguration(configFile, basePath, musicPath);
            } else if (osName.equalsIgnoreCase("windows 8")) {
                basePath = "C:" + s + "Users" + s + userName + s;
                musicPath = windows8MusicFolderName;
                configFile = new File(windows8Config);
                configFile.mkdirs();
                configFile.createNewFile();
                setDefaultConfiguration(configFile, basePath, musicPath);
            } else if (osName.equalsIgnoreCase("windows vista")) {
                musicPath = windowsVistaMusicFolderName;
                basePath = "C:" + s + "Users" + s + userName + s;
                configFile = new File(windowsVistaConfig);
                configFile.mkdirs();
                configFile.createNewFile();
                setDefaultConfiguration(configFile, basePath, musicPath);
            } else if (osName.equalsIgnoreCase("windows xp")) {
                basePath = "C:" + s + "Documents and Settings" + s + userName + s + "My Documents" + s;
                musicPath = windowsXPMusicFolderName;
                configFile = new File(windowsXPConfig);
                configFile.mkdirs();
                configFile.createNewFile();
                setDefaultConfiguration(configFile, basePath, musicPath);
            } else if (osName.equalsIgnoreCase("linux")) {
                basePath = s + "home" + s + "" + userName.toLowerCase() + s + ".config" + s + "jmpd" + s;
                musicPath = linuxMusicFolderName;
                configFile = new File(linuxConfig);
                configFile.mkdirs();
                configFile.createNewFile();
                setDefaultConfiguration(configFile, basePath, musicPath);
            } else if (osName.equalsIgnoreCase("mac os") || osName.equalsIgnoreCase("mac os x")) {
                basePath = s + "Users" + s + userName.toLowerCase() + s;
                musicPath = macMusicFolderName;
                configFile = new File(macConfig);
                configFile.mkdirs();
                configFile.createNewFile();
                setDefaultConfiguration(configFile, basePath, musicPath);
            } else if (osName.equalsIgnoreCase("solaris")) {
                basePath = s + "home" + s + "" + userName.toLowerCase() + s + ".config" + s + "jmpd" + s;
                musicPath = solarisMusicFolderName;
                configFile = new File(solarisConfig);
                configFile.mkdirs();
                configFile.createNewFile();
                setDefaultConfiguration(configFile, basePath, musicPath);
            } else {
                System.err.println("Unable to determine operating system.");
            }
        } catch (IOException e) {

        }
    }

    private static void setDefaultConfiguration(File config, String basePath, String musicPath) {
        try {
            Properties props = new Properties();
            props.setProperty("Database", basePath + "database" );
            props.setProperty("Port", "" + 5005);
            props.setProperty("MusicFolder", musicPath);
            OutputStream out = new FileOutputStream(config);
            props.store(out, "");
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
        if (daemonConfiguration.getProperty("Database") != null)
            return daemonConfiguration.getProperty("Database");
        else
            return null;
    }
}
