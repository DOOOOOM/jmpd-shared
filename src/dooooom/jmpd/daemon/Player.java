package dooooom.jmpd.daemon;

import dooooom.jmpd.data.Track;
import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class Player extends Application {
    // The class responsible for controlling most of the external
    // functions of the server
    private static DaemonMainController server;

	// The list of each track in the play queue, in
	// the order of playback.
    private static ArrayList<Track> playQueueTracks = new ArrayList<Track>();

    // The current song being played
    private static MediaPlayer currentPlayback;

    // References to playQueueTracks needed for sequential playback
    private static Track currentTrack;
    private static Track prevTrack;
    private static Track nextTrack;

    private static int currentIndex;

	// Whether the play queue loops back to the first track or not
    private static boolean loopingRepeat = true;

    @Override
    public void start(Stage arg0) {
        System.out.println("Java Music Player Daemon  Copyright (C) 2014  ");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.");
        System.out.println("This is free software, and you are welcome to redistribute it");
        System.out.println("under certain conditions; type `show c' for details.");

        try {
            server  = new DaemonMainController();
            Thread serverThread = new Thread(server);
            serverThread.start();
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

        try {
            trackPath = parseUrl(trackPath);
            currentPlayback = new MediaPlayer(new Media(trackPath));
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR]   IllegalArgumentException in Player, encoding string: " + trackPath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR]   Unclassified exception in Player.parseUrl(), encoding string: " + trackPath);
            e.printStackTrace();
        }

        if(oldCurrent != null)
            oldCurrent.dispose();

        int i = playQueueTracks.indexOf(newCurrent);
        currentIndex = i;

        setPlaybackChain(i);
    }

    public static void setPlaybackChain(int i) {
        if(i == 0) {
            if(loopingRepeat)
                setPrevTrack(playQueueTracks.get(playQueueTracks.size()-1));
            else
                setPrevTrack(null);

            setNextTrack(playQueueTracks.get(getNextTrackIndex(i)));

            currentPlayback.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    server.onTrackChange();
                    setCurrentTrack(nextTrack);
                    play();
                }
            });
        } else if(i == playQueueTracks.size() - 1 && !loopingRepeat) {
            setNextTrack(null);
            setPrevTrack(playQueueTracks.get(getPrevTrackIndex(i)));

            currentPlayback.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    server.onTrackChange();
                }
            });
        } else {
            setPrevTrack(playQueueTracks.get(getPrevTrackIndex(i)));
            setNextTrack(playQueueTracks.get(getNextTrackIndex(i)));

            currentPlayback.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    server.onTrackChange();
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
        boolean lastTrackCase = false;
        if(newSongs == null)
            return;

        if(playQueueTracks.size() != 0) {
            if(currentTrack.equals(playQueueTracks.get(playQueueTracks.size()-1))) {
                lastTrackCase = true;
            }
        }

        playQueueTracks.addAll(newSongs);

        if(playQueueTracks.equals(newSongs))
            setCurrentTrack(playQueueTracks.get(0));

        if(lastTrackCase) {
            int currentIndex = playQueueTracks.indexOf(currentTrack);
            setNextTrack(playQueueTracks.get(getNextTrackIndex(currentIndex)));
            currentPlayback.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    setCurrentTrack(nextTrack);
                    play();
                }
            });
        }
    }

	/**
	*	Precondition: Given a list of songs
	*	Postcondition: Given songs removed from play queue
	*/
    public static void remove(ArrayList<Track> removeSongs) {
        if(removeSongs.containsAll(playQueueTracks)) {
            stopPlayback();
            currentPlayback.dispose();
            currentPlayback = null;
            currentTrack = null;
            prevTrack = null;
            nextTrack = null;
            currentIndex = 0;
            playQueueTracks.removeAll(removeSongs);
            return;
        }

        ArrayList<Track> evaluate = playQueueTracks;
        evaluate.removeAll(removeSongs);

        if(removeSongs.contains(nextTrack)
                && removeSongs.contains(prevTrack)
                && removeSongs.contains(currentTrack)) {

        } else if(removeSongs.contains(nextTrack)
                && removeSongs.contains(prevTrack)) {
            setPlaybackChain(currentIndex);
        } else if(removeSongs.contains(currentTrack)
                && removeSongs.contains(nextTrack)) {
            server.onTrackChange();
            stopPlayback();
            int nextIndex = playQueueTracks.indexOf(nextTrack);
            if(nextIndex == playQueueTracks.size() - 1 && loopingRepeat) {
                setCurrentTrack(evaluate.get(0));
                play();
            } else if(nextIndex == playQueueTracks.size() - 1 && !loopingRepeat) {
                stopPlayback();
                prevTrack = null;
                nextTrack = null;
                currentIndex = 0;
                setCurrentTrack(evaluate.get(0));
            } else {
                Track skipNext = evaluate.get(getNextTrackIndex(nextIndex));
                setCurrentTrack(skipNext);
            }
        } else if(removeSongs.contains(currentTrack)
                && removeSongs.contains(prevTrack)) {
            server.onTrackChange();
            stopPlayback();
            if(currentIndex == playQueueTracks.size() - 1 && loopingRepeat) {
                setCurrentTrack(evaluate.get(0));
                play();
            } else if(currentIndex == playQueueTracks.size() - 1 && !loopingRepeat) {
                stopPlayback();
                prevTrack = null;
                nextTrack = null;
                currentIndex = 0;
                setCurrentTrack(evaluate.get(0));
            } else {
                setCurrentTrack(nextTrack);
            }
        } else if(removeSongs.contains(prevTrack)) {
            setPlaybackChain(currentIndex);
        } else if(removeSongs.contains(currentTrack)) {
            server.onTrackChange();
            stopPlayback();
            if(currentIndex == playQueueTracks.size() - 1 && loopingRepeat) {
                setCurrentTrack(evaluate.get(0));
                play();
            } else if(currentIndex == playQueueTracks.size() - 1 && !loopingRepeat) {
                stopPlayback();
                prevTrack = null;
                nextTrack = null;
                currentIndex = 0;
                setCurrentTrack(evaluate.get(0));
            } else {
                setCurrentTrack(nextTrack);
            }
        } else if(removeSongs.contains(nextTrack)) {
            setPlaybackChain(currentIndex);
        }

        playQueueTracks.removeAll(removeSongs);
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
                server.onTrackChange();
                setCurrentTrack(nextTrack);
                play();
            }
        } catch (MediaException e) {
            System.out.println("Invalid media type.");
        }
    }

    public static void seekTo(double time) {
        Duration newTime = new Duration(time);
        currentPlayback.seek(newTime);
    }

    public static Track getTrackFromIndex(int i) {
        if(playQueueTracks.get(i) != null) {
            return playQueueTracks.get(i);
        } else {
            return null;
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


    /**
     *   Returns the Track object of the current media.
     */
    public static Track getCurrentTrack() {
        return currentTrack;
    }

    /**
    *   Returns the playback state of the current player.
    */
    public static int getState() {
        if(currentPlayback != null) {
            if(currentPlayback.getStatus() == MediaPlayer.Status.PLAYING) {
                return 1;
            }
        }
        return 0;
    }

	/**
	*	Precondition: There is a current track
	* 	Postcondition: Gives metadata for the current track
	*/
    public static ArrayList<Track> getPlayQueue() {
        return playQueueTracks;
    }

	/**
	*	Precondition: There is a current track
	* 	Postcondition: Gives the elapsed time for
    *                  the current track
	*/
    public static double getTime() {
        if(currentPlayback != null)
            return currentPlayback.getCurrentTime().toSeconds();
        else
            return 0.0;
    }

    public static void clearQueue() {
        stopPlayback();
        currentPlayback.dispose();
        currentPlayback = null;
        currentTrack = null;
        prevTrack = null;
        nextTrack = null;
        currentIndex = 0;
        playQueueTracks.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }

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

    public static String parseUrl(String s) throws Exception {
        URL u = new URL(s);
        URL y = new URI(
                u.getProtocol(),
                u.getAuthority(),
                u.getPath(),
                u.getQuery(),
                u.getRef()).
                toURL();
        return y.toString();
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
