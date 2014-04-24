package dooooom.jmpd.tcptest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class DaemonMainController implements Runnable,RequestController {
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
        request.put("ack","ack");
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

        } catch (IOException e) {

        }finally {
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
        String sep = System.getProperty("file.separator");
        String osName = System.getProperty("os.name");

        String linuxConfig = "";

        if(osName.equalsIgnoreCase("windows 7")) {

        } else if(osName.equalsIgnoreCase("windows vista")) {

        } else if(osName.equalsIgnoreCase("windows xp")) {

        } else if(osName.equalsIgnoreCase("linux")) {

        } else if(osName.equalsIgnoreCase("mac os") || osName.equalsIgnoreCase("mac os x")) {

        } else if(osName.equalsIgnoreCase("solaris")) {

        } else {
            System.err.println("Unable to determine operating system.");
        }
    }

    private static int getPortNumber() {
        int port = 5005;
        if(daemonConfiguration.getProperty("Port") != null) {
            port = Integer.parseInt(daemonConfiguration.getProperty("Port"));
        }
        return port;
    }

    public static String getMusicFolder() {
        if(daemonConfiguration.getProperty("MusicFolder") != null)
            return daemonConfiguration.getProperty("MusicFolder");
        else
            return null;
    }
}
