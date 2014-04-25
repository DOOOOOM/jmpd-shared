package dooooom.jmpd.daemon;

import dooooom.jmpd.data.Database;
import dooooom.jmpd.data.Track;
import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.*;

public class Player extends Application {

	// The list of each track in the play queue, in
	// the order of playback.
    private static Database playQueueFiles;

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
            DaemonMainController server  = new DaemonMainController();
            Thread serverThread = new Thread(server);
            serverThread.start();
            Thread.sleep(1000);
            playQueueFiles = new Database();
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

    /**
     *	Precondition: The play queue exists
     *	Postcondition: Queue is populated with MediaPlayers
     */
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

    /**
     *	Precondition: Given a list of songs
     *	Postcondition: Given songs added to play queue
     */
    public static void add(Database newSongs) {
        for (Track t : newSongs) {
            String path = t.get("filepath").replace(" ", "%20");
            System.out.println(path);
            final MediaPlayer p = new MediaPlayer(new Media(path));
            playQueue.add(p);
        }
        playQueueFiles.addAll(newSongs);
        setPlayQueue();
    }

	/**
	*	Precondition: Given a list of songs
	*	Postcondition: Given songs removed from play queue
	*/
    public static void remove(Database removeSongs) {
        for(Track t: removeSongs) {
            String path = t.get("filepath").replace(" ", "%20");
            for (MediaPlayer p: playQueue) {
                if(path.equals(p.getMedia().getSource())) {
                    playQueue.remove(p);
                }
            }
        }
        playQueueFiles.removeAll(removeSongs);
        setPlayQueue();
    }

	/**
	*	Precondition: PlayQueue contains tracks
	* 	Postcondition: Playback state reversed
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
	*	Precondition: There is a current track being played
	* 	Postcondition: Track is now paused
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
	*	Precondition: There is a current track not being played
	* 	Postcondition: Track is now playing
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
	*	Precondition: There is a current track
	* 	Postcondition: Track is now paused at 0:00
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
	*	Precondition: There is a current track
	* 	Postcondition: The next track in the queue is now playing
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
	*	Precondition: Given a track index
	* 	Postcondition: Gives the next track in the queue
    *                  looping to the front if the last
    *                  track index is given
	*/
    public static int getNextTrackIndex(int current) {
        int nextTrackIndex = 0;
        if (current != playQueue.size() - 1)
            nextTrackIndex = current + 1;
        return nextTrackIndex;
    }

    /**
     *	Precondition: There is a current track
     * 	Postcondition: The previous track in the queue is now playing
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
     *	Precondition: Given a track index
     * 	Postcondition: Gives the previous track in the queue
     *                  looping to the back if the first
     *                  track index is given
     */
    public static int getPrevTrackIndex(int current) {
        int prevTrackIndex = playQueue.size() - 1;
        if (current != 0)
            prevTrackIndex = current - 1;
        return prevTrackIndex;
    }

	/**
	*	Precondition: There is a current track
	* 	Postcondition: Gives MediaPlayer for current track
	*/
    public static MediaPlayer getCurrent() {
        if(playQueue.isEmpty())
            return null;
        else
            return playQueue.get(currentPlayer);
    }

	/**
	*	Precondition: There is a current track
	* 	Postcondition: Gives metadata for the current track
	*/
    public static String getStat() {
        MediaPlayer p = getCurrent();
        Media m = p.getMedia();
        String meta = m.getMetadata().toString();
        System.out.println(meta);
        return meta;
    }

	/**
	*	Precondition: There is a current track
	* 	Postcondition: Gives the elapsed time for
    *                  the current track
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
    public class PlayerControl implements Runnable {
//        public PlayerControl() {}
        public void run() {
            Scanner in = new Scanner(System.in);
//            UDPClient client = new UDPClient();
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
//                        try {
//                            client.sendMessage(Command.TOGGLE, "");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            } catch (NoSuchElementException e) {
                in.close();
            }
        }
    }
    public void addSongs() {
        playQueueFiles.clear();
        Database pq = new Database();
//        pq.addAll(returnPathNames());
        add(pq);
        setPlayQueue();
    }
//endTest
}