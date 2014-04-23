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

	// The list of file paths for each track in the play queue, in 
	// the order of playback.
    private static ArrayList<String> playQueueFiles = new ArrayList<String>();

	// The list of media players for each track in the play queue, in 
	// the order of playback.
    private static ArrayList<MediaPlayer> playQueue = new ArrayList<MediaPlayer>();

	// Whether the play queue loops back to the first track or not
    private static boolean loopingRepeat = false;

	// Keeps a record of the current track so clients can request
	// elapsed time, etc
    private static int currentPlayer;

    @Override
    public void start(Stage arg0) {
        try {
            UDPServer server  = new UDPServer();
            Thread serverThread = new Thread(server);
			_requestContainer = jsonParser.jsonParser();
            serverThread.start();
//beginTest
//            PlayerControl control = new PlayerControl();
//            Thread controllerThread = new Thread(control);
//            controllerThread.start();
//endTest

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
            } else if (loopingRepeat) { //essentially restarts the play queue on the last track
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

	/**
	*	Precondition: Given a list of songs
	*	Postcondition: Given songs removed from play queue
	*/
    public static void remove(ArrayList<String> removeSongs) {
        playQueue.removeAll(removeSongs);
        playQueueFiles.removeAll(removeSongs);
    }

	/**
	*	Precondition:
	* 	Postcondition:
	*/
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

	/**
	*	Precondition:
	* 	Postcondition:
	*/
    public static void pause() {
        try {
            if (!playQueue.isEmpty())
                playQueue.get(currentPlayer).pause();
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

	/**
	*	Precondition:
	* 	Postcondition:
	*/
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

	/**
	*	Precondition:
	* 	Postcondition:
	*/
    public static void stopPlayback() {
        try {
            if (!playQueue.isEmpty())
                playQueue.get(currentPlayer).stop();
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

	/**
	*	Precondition:
	* 	Postcondition:
	*/
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

	/**
	*	Precondition:
	* 	Postcondition:
	*/
    public static int getNextTrackIndex(int current) {
        int nextTrackIndex = 0;
        if (current != playQueue.size() - 1)
            nextTrackIndex = current + 1;
        return nextTrackIndex;
    }

	/**
	*	Precondition:
	* 	Postcondition:
	*/
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

	/**
	*	Precondition:
	* 	Postcondition:
	*/
    public static int getPrevTrackIndex(int current) {
        int nextTrackIndex = playQueue.size() - 1;
        if (current != 0)
            nextTrackIndex = current - 1;
        return nextTrackIndex;
    }

	/**
	*	Precondition:
	* 	Postcondition:
	*/
    public static MediaPlayer getCurrent() {
        if(playQueue.isEmpty())
            return null;
        else
            return playQueue.get(currentPlayer);
    }

	/**
	*	Precondition:
	* 	Postcondition:
	*/
    public static String getStat() {
        MediaPlayer p = getCurrent();
        Media m = p.getMedia();
        String meta = m.getMetadata().toString();
        System.out.println(meta);
        return meta;
    }

	/**
	*	Precondition:
	* 	Postcondition:
	*/
    public static double getTime() {
        if(!playQueue.isEmpty())
            return getCurrent().getCurrentTime().toSeconds();
        else
            return 0.0;
    }

    public static void main(String[] args) {
        launch(args);
    }
//beginTest
//    public class PlayerControl implements Runnable {
////        public PlayerControl() {}
//        public void run() {
//            Scanner in = new Scanner(System.in);
//            UDPClient client = new UDPClient();
//            try {
//                while (true) {
//                    String input = in.nextLine();
//                    System.out.println(input);
//                    if (input.equals("t")) {
//                        toggle();
//                    } else if (input.equals("n")) {
//                        next();
//                    } else if (input.equals("p")) {
//                        prev();
//                    } else if (input.equals("s")) {
//                        stopPlayback();
//                    } else if (input.equals("a")) {
////                        addSongs();
//                    } else if (input.equals("c")) {
//                        System.out.println(getTime());
//                    } else if (input.equals("m")) {
//                        try {
//                            client.sendMessage(Command.TOGGLE, "");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            } catch (NoSuchElementException e) {
//                in.close();
//            }
//        }
//    }
//    public void addSongs() {
//        playQueueFiles.clear();
//        ArrayList<String> pq = new ArrayList<String>();
//        pq.addAll(returnPathNames());
//        Collections.sort(pq);
//        add(pq);
//        setPlayQueue();
//    }
//endTest

}
