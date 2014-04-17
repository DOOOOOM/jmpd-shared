package dooooom.jmpd.client.gui.javafx;

import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.TrackList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private ListView<TrackListItem> track_list_view;

    /*
     * ObservableList objects for the ListViews
     */
    private final ObservableList<String> artistList = FXCollections.observableArrayList();
    private final ObservableList<String> albumList = FXCollections.observableArrayList();
    private final ObservableList<TrackListItem> trackList = FXCollections.observableArrayList();

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

    public LibraryController(ListView<String> artist_list_view, ListView<String> album_list_view, ListView<TrackListItem> track_list_view, MainViewController mainViewController) {
        this.artist_list_view = artist_list_view;
        this.album_list_view = album_list_view;
        this.track_list_view = track_list_view;
        this.mainViewController = mainViewController;

        artist_list_view.setItems(artistList);
        album_list_view.setItems(albumList);
        track_list_view.setItems(trackList);

        addActionListeners();
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

        updateAllListViews();
    }

    /*
	 * After changes have been made to the selection, update the JLists with the new filters
	 */
    private void updateAllListViews() {
        updateArtistListView();
        updateAlbumListView();
        updateTrackListView();
    }

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

    private void updateTrackListView() {
        trackList.clear();

        TrackList availableTracks = (TrackList) (library.clone());

        //if there is an artist selected, filter the results
        if(selectedArtist != null && !selectedArtist.isEmpty()) {
            availableTracks = availableTracks.search("artist", selectedArtist);
        }

        //likewise for albums
        if(selectedAlbum != null && !selectedAlbum.isEmpty()) {
            availableTracks = availableTracks.search("album", selectedAlbum);
        }

        //add selected tracks to listmodel, wrapping in TrackJListItem objects
        for(Track t : availableTracks) {
            trackList.add(new TrackListItem(t));
        }
    }

    private void addActionListeners() {
        artist_list_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                //if [any] selected
                if(artist_list_view.getSelectionModel().getSelectedIndices().get(0) == 0)
                    selectedArtist = "";
                else
                    selectedArtist = s2;
                updateAlbumListView();
                updateTrackListView();
            }
        });

        album_list_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                //if [any] selected
                if(album_list_view.getSelectionModel().getSelectedIndices().get(0) == 0)
                    selectedAlbum = "";
                else
                    selectedAlbum = s2;

                updateTrackListView();
            }
        });
    }
}