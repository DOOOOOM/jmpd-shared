package dooooom.jmpd.daemon;

import dooooom.jmpd.data.Database;
import dooooom.jmpd.data.FileSystemScanner;
import dooooom.jmpd.data.Track;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.util.*;

public class DaemonMainController implements Runnable, RequestController {
    private static Properties daemonConfiguration;
    private Player player;

    public DaemonMainController(Player player) {
        this.player = player;
    }

    public void run() {
        daemonConfiguration = Configure();
        DaemonConnectionController dcc = new DaemonConnectionController(getPortNumber(), this);

        //temporarily hard-coded
        FileSystemScanner f = new FileSystemScanner("C:\\Music\\Incubus");
        ArrayList<Track> t = f.returnTracks();
        System.out.println(t);
        Collections.sort(t);
        Database.library = t;
        player.add(t);

        Thread dccThread = new Thread(dcc);
        dccThread.start();
        Player.setPlayQueue();
    }

    @Override
    public Map<String, Object> processRequest(Map<String, Object> request) {
        String cmd = (String) request.get("command");
        String req_id = (String) request.get("request_id");

        Map<String,Object> response = new HashMap<String,Object>();

        if(req_id == null) {
            //Bad request
            response.put("status_code","400");
            response.put("status_message","Bad Request: Missing request_id");
            return response;
        }

        if(cmd != null && cmd instanceof String) {
            response.put("request_id", req_id);

            if(cmd.equals("TOGGLE")) {
                player.toggle();
                response.put("status_code","200");
                response.put("status_message","OK");
                addTrackInfo(response);
//            } else if(cmd.equals("PLAY")) {
//
//            } else if(cmd.equals("PAUSE")) {
//
//            } else if(cmd.equals("STOP")) {
//
            } else if(cmd.equals("NEXT")) {
                player.next();
                response.put("status_code","200");
                response.put("status_message","OK");
                addTrackInfo(response);
            } else if(cmd.equals("PREV")) {
                player.prev();
                response.put("status_code","200");
                response.put("status_message","OK");
                addTrackInfo(response);
            } else if(cmd.equals("DATABASE")) {
                ArrayList<Track> data = Database.library;

                if(data != null) {
                    response.put("status_code", "200");
                    response.put("status_message", "OK");
                    response.put("data",data);
                } else {
                    response.put("status_code","404");
                    response.put("status_message","Not Found: database not found");
                }
//
//            } else if(cmd.equals("ADD")) {
//
//            } else if(cmd.equals("UPDATE")) {
//
//            } else if(cmd.equals("REMOVE")) {
//
//            } else if(cmd.equals("CURRENT")) {
//
//            } else if(cmd.equals("QUEUE")) {
//
//            } else if(cmd.equals("SET")) {
//
//            } else if(cmd.equals("PLADD")) {
//
//            } else if(cmd.equals("PLDEL")) {
//
            } else {
                response.put("status_code","501");
                response.put("status_message","Not Implemented: " + cmd);
            }

            //things to do no matter what was received
        } else {
            //Bad request
            response.put("status_code","400");
            response.put("status_message","Bad Request: No command given");
        }

        return response;
    }

    private void addTrackInfo(Map<String,Object> response) {
        Track currentTrack = player.getCurrentTrack();

        if(currentTrack != null) {
            response.put("track_id", currentTrack.get("id"));
            response.put("time", Double.toString(player.getTime()));
            response.put("state", player.getState());
        } else {

        }
    }

    private static Properties Configure() {
        Properties prop = new Properties();
        InputStream in = null;

        try {
            in = new FileInputStream(getDefaultConfigPath() + "jmpd.properties");
            prop.load(in);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO]    No user configuration. Creating new file");
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
