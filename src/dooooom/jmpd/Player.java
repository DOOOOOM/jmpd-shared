package dooooom.jmpd;

import dooooom.jmpd.client.UDPClient;
import dooooom.jmpd.data.Command;
import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class Player extends Application {

    private static ArrayList<String> playQueueFiles = new ArrayList<String>();
    private static ArrayList<MediaPlayer> playQueue = new ArrayList<MediaPlayer>();
    private static boolean loopingRepeat = false;
    private static int currentPlayer;

//    public Player() {
//        PlayerControl p = new PlayerControl();
//    }

    @Override
    public void start(Stage arg0) {
        try {
            UDPServer server  = new UDPServer();
            Thread serverThread = new Thread(server);
            serverThread.start();

            PlayerControl control = new PlayerControl();
            Thread controllerThread = new Thread(control);
            controllerThread.start();

            setPlayQueue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPlayQueue() {
        for (int i = 0; i < playQueue.size(); i++) {
            if (i < playQueue.size() - 1) {
                final int t = i + 1;
                playQueue.get(i).setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        currentPlayer = t;
                        play();
                    }
                });
            } else if (loopingRepeat) {
                playQueue.get(playQueue.size() - 1).setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        playQueue.clear();
                        setPlayQueue();
                        currentPlayer = 0;
                        play();
                    }
                });
            }
        }
    }

    public static void add(ArrayList<String> newSongs) {
        for (String s : newSongs) {
            String path = s.replace(" ", "%20");
            System.out.println(path);
            final MediaPlayer p = new MediaPlayer(new Media(path));
            playQueue.add(p);
        }
        playQueueFiles.addAll(newSongs);
    }

    public static void toggle() {
        try {
            if (!playQueue.isEmpty()) {
                if (playQueue.get(currentPlayer).getStatus() == MediaPlayer.Status.PLAYING) {
                    pause();
                } else {
                    play();
                }
            }
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

    public static void pause() {
        try {
            if (!playQueue.isEmpty())
                playQueue.get(currentPlayer).pause();
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

    public static void play() {
        try {
            if (!playQueue.isEmpty()) {
                playQueue.get(currentPlayer).play();
                System.out.println("Now playing: " + playQueueFiles.get(currentPlayer));
            }
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

    public static void stopPlayback() {
        try {
            if (!playQueue.isEmpty())
                playQueue.get(currentPlayer).stop();
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

    public static void next() {
        try {
            if (!playQueue.isEmpty()) {
                playQueue.get(currentPlayer).stop();
                currentPlayer = getNextTrackIndex(currentPlayer);
                if (!((currentPlayer == 0)   //Exclude the corner case where we're at the
                        && !loopingRepeat)) //   end of the play queue and not repeating
                    play();
            }
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

    public static int getNextTrackIndex(int current) {
        int nextTrackIndex = 0;
        if (current != playQueue.size() - 1)
            nextTrackIndex = current + 1;
        return nextTrackIndex;
    }

    public static void prev() {
        try {
            if (!playQueue.isEmpty()) {
                stopPlayback();
                currentPlayer = getPrevTrackIndex(currentPlayer);
                play();
            }
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

    public static int getPrevTrackIndex(int current) {
        int nextTrackIndex = playQueue.size() - 1;
        if (current != 0)
            nextTrackIndex = current - 1;
        return nextTrackIndex;
    }

    public static MediaPlayer getCurrent() {
        if(playQueue.isEmpty())
            return null;
        else
            return playQueue.get(currentPlayer);
    }

    public static double getTime() {
        if(!playQueue.isEmpty())
            return getCurrent().getCurrentTime().toSeconds();
        else
            return 0.0;
    }

    //Testing only
    public void addSongs() {
        playQueueFiles.clear();
        ArrayList<String> pq = new ArrayList<String>();
        pq.addAll(returnPathNames());
        Collections.sort(pq);
        add(pq);
        setPlayQueue();
    }

    public ArrayList<String> returnPathNames()
    /**
     * This method provided with the default music directory for the system,
     * will return an array of Strings, each corresponding to the pathName
     * of the file in question. Directories will not be returned
     * @ Pre: the musicFolderPath is a valid String
     * @ Pre: the musicFolderPath is a valid directory path
     * @ Post: None of the paths returned by the method are folders
     *
     */
    {
        String s = System.getProperty("file.separator");
        File musicFolder = new File("C:/Users/phider/Music/Wave 1");

        File[] abstractPaths = musicFolder.listFiles();

        ArrayList<String> arrayToReturn = new ArrayList<String>();

        for(File f : abstractPaths) {
            if(f.isFile()) {
                String fpath = f.getAbsolutePath();
                fpath = fpath.replace('\\','/');
                arrayToReturn.add("file:///" + fpath);
            }
                //arrayToReturn.add("file:///" + f.getAbsolutePath());
        }

        ArrayList<String> list = new ArrayList<String>();

        for (String str : arrayToReturn)  // flush null values
        {
            if (str != null && s.length() > 0) {
                if ((str.substring(str.length() - 4, str.length()).equalsIgnoreCase(".mp3")))
                    list.add(str);
            }
        }
        arrayToReturn = list;

        return arrayToReturn;
    } //end Testing

    //TODO: remove main and launch from Server as a thread
    public static void main(String[] args) {
        launch(args);
    }

    public class PlayerControl implements Runnable {
//        public PlayerControl() {}
        public void run() {
            Scanner in = new Scanner(System.in);
            UDPClient client = new UDPClient();
            try {
                while (true) {
                    String input = in.nextLine();
                    System.out.println(input);
                    if (input.equals("t")) {
                        toggle();
                    } else if (input.equals("n")) {
                        next();
                    } else if (input.equals("p")) {
                        prev();
                    } else if (input.equals("s")) {
                        stopPlayback();
                    } else if (input.equals("a")) {
                        addSongs();
                    } else if (input.equals("c")) {
                        System.out.println(getTime());
                    } else if (input.equals("m")) {
                        try {
                            client.sendMessage(Command.TOGGLE, "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (NoSuchElementException e) {
                in.close();
            }
        }
    }

}
