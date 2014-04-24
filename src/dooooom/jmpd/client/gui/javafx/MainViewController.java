package dooooom.jmpd.client.gui.javafx;

import dooooom.jmpd.client.ClientConnectionController;
import dooooom.jmpd.client.ResponseController;
import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.TrackList;
import dooooom.jmpd.data.testing.TrackListGenerator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.*;

public class MainViewController implements Initializable,ResponseController {
    /*
     * GUI Elements injected from FXML
     */
    @FXML private ListView<String> artist_list_view;
    @FXML private ListView<String> album_list_view;
    @FXML private ListView<TrackListItem> track_list_view;

    @FXML private ListView<PlayQueueTrackListItem> play_queue_listview;

    @FXML private Button prev_button;
    @FXML private Button play_button;
    @FXML private Button next_button;

    @FXML private Label track_label;
    @FXML private Slider seek_slider;

    @FXML private TextArea lyrics_text;
    @FXML private Label status_bar;

    //private UDPClient client;
    private ClientConnectionController cc;

    /*
	 * The actual library of tracks to choose from
	 */
    private TrackList library = new TrackList();

    /*
     * ObservableList objects for the ListViews
     */
    private final ObservableList<String> artistList = FXCollections.observableArrayList();
    private final ObservableList<String> albumList = FXCollections.observableArrayList();
    private final ObservableList<TrackListItem> trackList = FXCollections.observableArrayList();
    private final ObservableList<PlayQueueTrackListItem> playQueueList = FXCollections.observableArrayList();

    /*
	 * Current Selections (for filtering purposes)
	 */
    private List<String> selectedArtists;
    private List<String> selectedAlbums;
    private TrackList filteredTracks;


    /*
     * HashMaps to track which artists created which albums, and which albums contain which tracks
     */
    private Map<String, ArrayList<String>> artistAlbums;
    private Map<String, ArrayList<Track>> albumTracks;

    /*
     * Track ID of the currently selected song
     */
    private int currentTrackID;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        cc = new ClientConnectionController("localhost", 4444, this);
        Thread clientThread = new Thread(cc);
        clientThread.start();

        /*
		 * This line adds random garbage data to the library in order to test library filter panes.
		 * Comment it out if you don't want garbage data
		 */
        setLibrary(TrackListGenerator.randomTracksGib(1000));

        artist_list_view.setItems(artistList);
        album_list_view.setItems(albumList);
        track_list_view.setItems(trackList);

        addActionListeners();
    }

    public void updateTrackLabel(Track t) {
        String result = "";

        String s;
        if ((s = t.get("title")) == null)
            s = "Unknown";
        result += s + " - ";
        if ((s = t.get("artist")) == null)
            s = "Unknown";
        result += s + " - ";
        if ((s = t.get("album")) == null)
            s = "Unknown";
        result += s;

        track_label.setText(result);
    }

    /* ********************************
     * LIBRARY CONTROL
     */

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

        if(selectedArtists == null || selectedArtists.isEmpty()) {
            //if no selected artist, add all albums
            for (String s : albumTracks.keySet())
                albumList.add(s);
        } else {
            //if there is a selected artist, filter albums
            for (String art : selectedArtists) {
                for (String s : artistAlbums.get(art))
                    albumList.add(s);
            }
        }
    }

    private void updateTrackListView() {
        trackList.clear();

        TrackList availableTracks = new TrackList();

        //if there is an artist selected, filter the results
        if(selectedArtists != null && !selectedArtists.isEmpty()) {
            for(String artist : selectedArtists)
                availableTracks.addAll(availableTracks.search("artist", artist));
        }

        //likewise for albums
        if(selectedAlbums != null && !selectedAlbums.isEmpty()) {
            for(String album : selectedAlbums)
                filteredTracks.addAll(availableTracks.search("album", album));
        }

        //add selected tracks to listmodel, wrapping in TrackJListItem objects
        for(Track t : availableTracks) {
            trackList.add(new TrackListItem(t));
        }
    }

    private void addToPlayQueue(PlayQueueTrackListItem t) {

    }

    private void removeFromPlayQueue(PlayQueueTrackListItem t) {

    }

    public void setPlayQueue(TrackList tl) {

    }

    private void addActionListeners() {
        /* ****************************
         * PLAYBACK CONTROLS
         */

        /* Play/Pause */
        play_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Map<String, Object> request = new HashMap<String, Object>();
                request.put("command", "TOGGLE");
                cc.sendMap(request);
            }
        });

        /* Previous Track */
        prev_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Map<String, Object> request = new HashMap<String, Object>();
                request.put("command", "PREV");
                cc.sendMap(request);
            }
        });

        /* Next Track */
        next_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Map<String, Object> request = new HashMap<String, Object>();
                request.put("command", "NEXT");
                cc.sendMap(request);
            }
        });

        /* ****************************
         * LIBRARY SELECTION
         */
        artist_list_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                selectedArtists = new ArrayList<String>();

                //if [any] selected
                if((artist_list_view.getSelectionModel().getSelectedIndices().size() == 1)
                    && (artist_list_view.getSelectionModel().getSelectedIndices().get(0) == 0)) {
                    //do nothing, there are no selected artists
                } else {
                    selectedArtists.addAll(artist_list_view.getSelectionModel().getSelectedItems());
                }

                updateAlbumListView();
                updateTrackListView();
            }
        });

        album_list_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                selectedAlbums = new ArrayList<String>();

                //if [any] selected
                if((album_list_view.getSelectionModel().getSelectedIndices().size() == 1)
                        && (album_list_view.getSelectionModel().getSelectedIndices().get(0) == 0)) {
                    //do nothing, there are no selected artists
                } else {
                    selectedAlbums.addAll(album_list_view.getSelectionModel().getSelectedItems());
                }

                updateTrackListView();
            }
        });

        track_list_view.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2) {
                        TrackListItem tli = track_list_view.getSelectionModel().getSelectedItems().get(0);
                        PlayQueueTrackListItem pqtli = new PlayQueueTrackListItem(tli.getTrack());
                        addToPlayQueue(pqtli);
                        status_bar.setText("Added 1 track (" + pqtli + ") to play queue.");
                    }
                }
            }
        });
    }

    @Override
    public void processResponse(Map<String, Object> request, Map<String, Object> response) {
        String cmd = (String) request.get("command");

        if(cmd != null && cmd instanceof String) {
            if(cmd.equals("TOGGLE")) {

            } else if(cmd.equals("PLAY")) {

            } else if(cmd.equals("PAUSE")) {

            } else if(cmd.equals("STOP")) {

            } else if(cmd.equals("NEXT")) {

            } else if(cmd.equals("PREV")) {

            } else if(cmd.equals("DATABASE")) {

            } else if(cmd.equals("ADD")) {

            } else if(cmd.equals("UPDATE")) {

            } else if(cmd.equals("REMOVE")) {

            } else if(cmd.equals("CURRENT")) {

            } else if(cmd.equals("QUEUE")) {

            } else if(cmd.equals("PLADD")) {

            } else if(cmd.equals("PLDEL")) {

            }
        } else {
            System.err.println("[WARN]    Malformed request in processResponse(...)");
        }
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void giveStatusInformation(String s) {
        status_bar.setText(s);
    }
}
