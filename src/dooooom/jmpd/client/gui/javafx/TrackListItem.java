package dooooom.jmpd.client.gui.javafx;

import dooooom.jmpd.data.Track;

/*
 * This class is used as a wrapper for a track, to be held in the JList.
 * The reason for this is that the JList (ListView?) will display a toString
 * of each item, and using this wrapper it will display only the track title.
 */

public class TrackListItem {
    private Track t;

    TrackListItem(Track t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return t.get("title");
    }

    public Track getTrack() {
        return t;
    }
}