package dooooom.jmpd.client.gui.javafx;

/*
 * This class is used as a wrapper for a track, to be held in the JList.
 * The reason for this is that the JList (ListView?) will display a toString
 * of each item, and using this wrapper it will display only the track title.
 */

import dooooom.jmpd.data.Track;

public class PlayQueueTrackListItem {
    Track t;

    PlayQueueTrackListItem(Track t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return t.get("");
    }
}