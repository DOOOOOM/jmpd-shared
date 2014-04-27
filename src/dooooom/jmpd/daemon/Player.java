package dooooom.jmpd.daemon;

import dooooom.jmpd.data.FileSystemScanner;
import dooooom.jmpd.data.Track;
import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Player extends Application {
    // The class responsible for controlling most of the external
    // functions of the server
    DaemonMainController server;

	// The list of each track in the play queue, in
	// the order of playback.
    private static ArrayList<Track> playQueueTracks = new ArrayList<Track>();

    // The current song being played
    private static MediaPlayer currentPlayback;

    // References to playQueueTracks needed for sequential playback
    private static Track currentTrack;
    private static Track prevTrack;
    private static Track nextTrack;

	// Whether the play queue loops back to the first track or not
    private static boolean loopingRepeat = true;

    @Override
    public void start(Stage arg0) {
        try {
            server  = new DaemonMainController(this);
            Thread serverThread = new Thread(server);
            serverThread.start();
//beginTest
            PlayerControl control = new PlayerControl();
            Thread controllerThread = new Thread(control);
            controllerThread.start();
//endTest
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *	Precondition: Given a Track object
     *	Postcondition: Track is now the current track, and loaded for playback
     */

    public static void setCurrentTrack(Track newCurrent) {
        if(newCurrent == null)
            return;

        MediaPlayer oldCurrent = currentPlayback;
        currentTrack = newCurrent;
        String trackPath = "file:///" + newCurrent.get("filepath").replace("\\","/");

        trackPath = encodeURIComponent(trackPath);

        currentPlayback = new MediaPlayer(new Media(trackPath));

        if(oldCurrent != null)
            oldCurrent.dispose();

        int i = playQueueTracks.indexOf(newCurrent);

        if(i == 0) {
            if(loopingRepeat)
                setPrevTrack(playQueueTracks.get(playQueueTracks.size()-1));
            else
                setPrevTrack(null);

            setNextTrack(playQueueTracks.get(getNextTrackIndex(i)));

            currentPlayback.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    setCurrentTrack(nextTrack);
                    play();
                }
            });
        } else if(i == playQueueTracks.size() - 1 && !loopingRepeat) {
            setNextTrack(null);
            setPrevTrack(playQueueTracks.get(getPrevTrackIndex(i)));
        } else {
            setPrevTrack(playQueueTracks.get(getPrevTrackIndex(i)));
            setNextTrack(playQueueTracks.get(getNextTrackIndex(i)));

            currentPlayback.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    setCurrentTrack(nextTrack);
                    play();
                }
            });
        }
    }

    public static void setPrevTrack(Track newPrevious) {
        prevTrack = newPrevious;
    }

    public static void setNextTrack(Track newNext) {
        nextTrack = newNext;
    }

    /**
     *	Precondition: Given a list of songs
     *	Postcondition: Given songs added to play queue
     */
    public static void add(ArrayList<Track> newSongs) {
        playQueueTracks.addAll(newSongs);

        if(playQueueTracks.equals(newSongs))
            setCurrentTrack(playQueueTracks.get(0));
    }

	/**
	*	Precondition: Given a list of songs
	*	Postcondition: Given songs removed from play queue
	*/
    public static void remove(ArrayList<Track> removeSongs) {
       playQueueTracks.removeAll(removeSongs);

        if(removeSongs.contains(currentTrack)) {
            stopPlayback();
        }
        if(removeSongs.contains(prevTrack)) {
            setCurrentTrack(currentTrack);
            play();
        }
        if(removeSongs.contains(nextTrack)) {
            setCurrentTrack(currentTrack);
            play();
        }
    }

	/**
	*	Precondition: PlayQueue contains tracks
	* 	Postcondition: Playback state reversed
	*/
    public static void toggle() {
        try {
            if (currentPlayback != null) {
                if (currentPlayback.getStatus() == MediaPlayer.Status.PLAYING) {
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
            if(currentPlayback != null) {
                currentPlayback.pause();
            }
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
            if(currentPlayback != null) {
                currentPlayback.play();
                System.out.println("Now playing: " + currentTrack);
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
            if(currentPlayback != null) {
                currentPlayback.stop();
            }
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
            if(currentPlayback != null && nextTrack != null) {
                setCurrentTrack(nextTrack);
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
        if (current != playQueueTracks.size() - 1)
            nextTrackIndex = current + 1;
        return nextTrackIndex;
    }

    /**
     *	Precondition: There is a current track
     * 	Postcondition: The previous track in the queue is now playing
     */
    public static void prev() {
        try {
            if(currentPlayback != null && prevTrack != null) {
                setCurrentTrack(prevTrack);
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
        int prevTrackIndex = playQueueTracks.size() - 1;
        if (current != 0)
            prevTrackIndex = current - 1;
        return prevTrackIndex;
    }

	/**
	*	Precondition: There is a current track
	* 	Postcondition: Gives MediaPlayer for current track
	*/
    public static MediaPlayer getCurrent() {
        return currentPlayback;
    }

    public static Track getCurrentTrack() {
        return currentTrack;
    }

    public static boolean getState() {
        return true;
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
        if(currentPlayback != null)
            return getCurrent().getCurrentTime().toSeconds();
        else
            return 0.0;
    }

    public static void main(String[] args) {
        launch(args);
    }
//beginTest
    public class PlayerControl implements Runnable {
        public void run() {
            Scanner in = new Scanner(System.in);
            System.out.println("[INFO]    Started player control");
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
                    } else if (input.equals("i")) {
                        server.onTrackChange();
                    }
                }
            } catch (NoSuchElementException e) {
                in.close();
            }
        }
    }

    public void addSongs() {
        FileSystemScanner f = new FileSystemScanner("/home/zap/music/Baths/Obsidian");
        ArrayList<Track> t = f.returnTracks();
        add(t);
    }
//endTest

    public static String encodeURIComponent(String s)
    {
        StringBuilder o = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (isUnsafe(ch)) {
                o.append('%');
                o.append(toHex(ch / 16));
                o.append(toHex(ch % 16));
            }
            else o.append(ch);
        }
        return o.toString().replace(" ","%20");
    }

    private static char toHex(int ch)
    {
        return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch)
    {
        if (ch > 128 || ch < 0)
            return true;
        return " []$&+,;=?@<>#%".indexOf(ch) >= 0;
    }
}
