package dooooom.jmpd.client.gui.javafx;

import dooooom.jmpd.client.ClientConnectionController;
import dooooom.jmpd.client.ResponseController;
import dooooom.jmpd.data.Database;
import dooooom.jmpd.data.Track;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

//import dooooom.jmpd.data.TrackList;

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
    private ArrayList<Track> library = new ArrayList<Track>();

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

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        cc = new ClientConnectionController("localhost", 5005, this);
        Thread clientThread = new Thread(cc);
        clientThread.start();

        /*
		 * This line adds random garbage data to the library in order to test library filter panes.
		 * Comment it out if you don't want garbage data
		 */
        //setLibrary(TrackListGenerator.randomTracksGib(1000));

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

    public void setLibrary(ArrayList<Track> tl) {
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

        ArrayList<Track> availableTracks = (ArrayList<Track>) library.clone();

        //if there is an artist selected, filter the results
        if(selectedArtist != null && !selectedArtist.isEmpty()) {
            availableTracks = Database.search(availableTracks, "artist", selectedArtist);
        }

        //likewise for albums
        if(selectedAlbum != null && !selectedAlbum.isEmpty()) {
            availableTracks = Database.search(availableTracks, "album", selectedAlbum);
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
        System.err.println("[DEBUG]   " + request + "\n\t" + response);

        String cmd = (String) request.get("command");

        if(cmd != null && cmd instanceof String) {
            if(cmd.equals("TOGGLE")) {

            } else if(cmd.equals("PLAY")) {

            } else if(cmd.equals("PAUSE")) {

            } else if(cmd.equals("STOP")) {

            } else if(cmd.equals("NEXT")) {

            } else if(cmd.equals("PREV")) {

            } else if(cmd.equals("DATABASE")) {
                String status = (String) response.get("status_code");
                if(status != null && status.equals("200")) {
                    ArrayList<Track> newLibrary = new ArrayList<Track>();
                    ArrayList<Map<String,String>> data = (ArrayList<Map<String,String>>) response.get("data");

                    if(data != null) {
                        for(Map<String,String> t : data) {
                            newLibrary.add(new Track(t));
                        }

                        setLibrary(newLibrary);
                    }
                }
            } else if(cmd.equals("ADD")) {

            } else if(cmd.equals("UPDATE")) {

            } else if(cmd.equals("REMOVE")) {

            } else if(cmd.equals("CURRENT")) {

            } else if(cmd.equals("QUEUE")) {

            } else if(cmd.equals("SET")) {

            } else if(cmd.equals("PLADD")) {

            } else if(cmd.equals("PLDEL")) {

            }

            if(response.containsKey("track_id")) {
                ArrayList<Track> tl = Database.search(library,"id",(String) response.get("track_id"));

                if(tl.isEmpty()) {
                    System.err.println("[WARN]    Currently playing track not found in client database");
                } else {
                    updateTrackLabel(tl.get(0));
                }

            }

            //things to do no matter what was received
        } else {
            System.err.println("[WARN]    Malformed request in processResponse(...)");
        }
    }

    @Override
    public void onConnect() {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("command", "DATABASE");
        cc.sendMap(request);
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void giveStatusInformation(String s) {
        status_bar.setText(s);
    }
}
