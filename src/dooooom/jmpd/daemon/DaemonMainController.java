package dooooom.jmpd.daemon;

import dooooom.jmpd.data.Database;
import dooooom.jmpd.data.Track;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.util.*;

public class DaemonMainController implements Runnable, RequestController {
    private static Properties daemonConfiguration;
    private Player player;
    DaemonConnectionController dcc;

    public DaemonMainController(Player player) {
        this.player = player;
    }


    public void run() {
        daemonConfiguration = Configure();
         dcc = new DaemonConnectionController(getPortNumber(), this);

//        FileSystemScanner f = new FileSystemScanner(getMusicFolder());
//        ArrayList<Track> t = f.returnTracks();
//        System.out.println(t);
//        Collections.sort(t);
//        Database.library = t;

        Thread dccThread = new Thread(dcc);
        dccThread.start();

        Timer sendTrackInfoTimer = new Timer();
        //sendTrackInfoTimer.schedule(new SendTrackInfoTask(), 1000, 1000);
    }

    @Override
    public Map<String, Object> processRequest(Map<String, Object> request) {
        if(request == null)
            return null;

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
                Player.toggle();
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
                Player.next();
                response.put("status_code","200");
                response.put("status_message","OK");
                addTrackInfo(response);
            } else if(cmd.equals("PREV")) {
                Player.prev();
                response.put("status_code","200");
                response.put("status_message","OK");
                addTrackInfo(response);
            } else if(cmd.equals("DATABASE")) {
                ArrayList<Track> data = Database.library;

                if(data != null) {
                    final int max_tracks_per_response = 50;

                    if (request.containsKey("segment_id")) {
                        int i;

                        try {
                            i = Integer.parseInt((String) request.get("segment_id"));

                            int max = (i + 1) * max_tracks_per_response;

                            if (max >= data.size())
                                max = data.size() - 1;

                            if (i == 0) {
                                response.put("status_code", "200");
                                response.put("status_message", "OK");
                            } else {
                                response.put("status_code", "206");
                                response.put("status_message", "OK");
                            }

                            int nLists = (int) Math.ceil((double) data.size() / max_tracks_per_response);

                            response.put("segment_id",Integer.toString(i));
                            response.put("n_segments",Integer.toString(nLists));
                            response.put("data", data.subList(i * max_tracks_per_response, max));
                            response.put("request_id", req_id);
                        } catch (NumberFormatException e) {
                            response.put("status_code","400");
                            response.put("status_message","Bad Request: invalid segment_id");
                        }
                    } else {
                        ArrayList<Map<String, Object>> responses = new ArrayList<Map<String, Object>>();

                        int nLists = (int) Math.ceil((double) data.size() / max_tracks_per_response);
                        for (int i = 0; i < nLists; i++) {
                            Map<String, Object> sub_response = new HashMap<String, Object>();

                            int max = (i + 1) * max_tracks_per_response;
                            if (max >= data.size())
                                max = data.size() - 1;

                            if (i == 0) {
                                sub_response.put("status_code", "200");
                                sub_response.put("status_message", "OK");
                            } else {
                                sub_response.put("status_code", "206");
                                sub_response.put("status_message", "OK");
                            }

                            sub_response.put("segment_id",Integer.toString(i));
                            sub_response.put("n_segments",Integer.toString(nLists));
                            sub_response.put("data", data.subList(i * max_tracks_per_response, max));
                            sub_response.put("request_id", req_id);

                            responses.add(sub_response);
                        }

                        response.put("DCC_SEND_MULTIPLE", responses);
                    }
                } else {
                    response.put("status_code","404");
                    response.put("status_message","Not Found: database not found");
                }
            } else if(cmd.equals("ADD")) {
                if(request.containsKey("ids")) {
                    ArrayList<String> ids = (ArrayList<String>) request.get("ids");

                    ArrayList<Track> tracksToAdd = new ArrayList<Track>();

                    for(String id : ids) {
                        tracksToAdd.addAll(Database.search("id", id));
                    }

                    player.add(tracksToAdd);
                } else {
                    response.put("status_code","400");
                    response.put("status_message","Bad Request: ADD without ids");
                }
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

    public Map<String,Object> createTrackInfoResponse() {
        Map<String,Object> response = new HashMap<String,Object>();

        response.put("request_id","0");
        addTrackInfo(response);

        return response;
    }

    private void addTrackInfo(Map<String,Object> response) {
        Track currentTrack = player.getCurrentTrack();

        if(currentTrack != null) {
            response.put("track_id", currentTrack.get("id"));
            response.put("time", Double.toString(player.getTime()));
            response.put("state", Boolean.toString(player.getState()));
        } else {

        }
    }

    public class SendTrackInfoTask extends TimerTask {
        @Override
        public void run() {
            dcc.sendToAll(createTrackInfoResponse());
        }
    }

    /*
     * This method should be called by the Player whenever the track is changed
     * it notifies all clients of the changes
     */
    public void onTrackChange() {
        dcc.sendToAll(createTrackInfoResponse());
    }

    /* ********************************
     *       ABANDON ALL HOPE,
     *       YE WHO ENTER HERE
     */

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
            props.setProperty("Database", getDefaultConfigPath() + "database" );
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
            out.close();
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
