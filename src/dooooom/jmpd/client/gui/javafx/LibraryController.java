package dooooom.jmpd.client.gui.javafx;

import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.TrackList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LibraryController {
    /*
	 * The actual library of tracks to choose from
	 */
    private TrackList library = new TrackList();

    /*
     * GUI Elements
     */
    private ListView<String> artist_list_view;
    private ListView<String> album_list_view;
    private ListView<Track> track_list_view;

    /*
     * ObservableList objects for the ListViews
     */
    private final ObservableList<String> artistList = FXCollections.observableArrayList();
    private final ObservableList<String> albumList = FXCollections.observableArrayList();
    private final ObservableList<Track> trackList = FXCollections.observableArrayList();

    /*
	 * Current Selections (for filtering purposes)
	 */
    private String selectedArtist;
    private String selectedAlbum;

    /*
     * HashMaps to track which artists created which albums, and which albums contain which tracks
     */
    private Map<String, ArrayList<String>> artistAlbums;
    private Map<String, ArrayList<Track>> albumTracks;

    /*
     * Track ID of the currently selected song
     */
    private int currentTrackID;

    /*
     * MainView that contains this LibraryPanel
     */
    private MainViewController mainViewController;


    public LibraryController(ListView<String> artist_list_view, ListView<String> album_list_view, ListView<Track> track_list_view, MainViewController mainViewController) {
        this.artist_list_view = artist_list_view;
        this.album_list_view = album_list_view;
        this.track_list_view = track_list_view;
        this.mainViewController = mainViewController;

        artist_list_view.setItems(artistList);
        album_list_view.setItems(albumList);
        track_list_view.setItems(trackList);
    }

    public void setLibrary(TrackList tl) {
        library = tl;

        artistAlbums = new HashMap<String, ArrayList<String>>();
        albumTracks = new HashMap<String, ArrayList<Track>>();

        for (Track t : tl) {
            //add new arraylist if this artist has not been encountered before
            if (!artistAlbums.containsKey(t.get("artist")))
                artistAlbums.put(t.get("artist"), new ArrayList<String>());

            ArrayList<String> albums = artistAlbums.get(t.get("artist"));
            if(!albums.contains(t.get("album")))
                albums.add(t.get("album"));

            //repeat above code for album/titles
            if (!albumTracks.containsKey(t.get("album")))
                albumTracks.put(t.get("album"), new ArrayList<Track>());

            //add track to album listing
            albumTracks.get(t.get("album")).add(t);
        }

        updateArtistListView();
    }

    /*
	 * After changes have been made to the selection, update the JLists with the new filters
	 */
    private void updateArtistListView() {
        artistList.clear();
        artistList.add("[any]");

        for (String s : artistAlbums.keySet()) {
            artistList.add(s);
        }
    }

    private void updateAlbumListView() {
        albumList.clear();

        albumList.add("[any]");

        if(selectedArtist == null || selectedArtist.isEmpty()) {
            //if no selected artist, add all albums
            for (String s : albumTracks.keySet())
                albumList.add(s);
        } else {
            //if there is a selected artist, filter albums
            for (String s : artistAlbums.get(selectedArtist))
                albumList.add(s);
        }
    }

        /*
         * This class is used as a wrapper for a track, to be held in the JList.
         * The reason for this is that the JList (ListView?) will display a toString
         * of each item, and using this wrapper it will display only the track title.
         */
    private class TrackListItem {
        Track t;

        TrackListItem(Track t) {
            this.t = t;
        }

        @Override
        public String toString() {
            return t.get("title");
        }
    }
}