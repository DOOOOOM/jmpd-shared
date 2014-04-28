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
import javafx.scene.text.Font;

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

//    @FXML private ListView<PlayQueueTrackListItem> play_queue_list_view;

    @FXML private Button prev_button;
    @FXML private Button play_button;
    @FXML private Button next_button;

    @FXML private Button update_button;

    @FXML private Tab playqueue_tab;

    @FXML private Label track_label;
    @FXML private Slider seek_slider;

    @FXML private TextArea lyrics_text;
    @FXML private Label status_bar;

    private boolean playing = false;

    //private UDPClient client;
    private ClientConnectionController cc;

    /*
	 * The actual library of tracks to choose from
	 */
    private ArrayList<Track> library = new ArrayList<Track>();

    /*
     * Keep track of how many and which segments have been received for the current library
     */
    private Map<Integer,Boolean> dbSegmentsReceived = new HashMap<Integer,Boolean>();
    private Map<Integer,Boolean> pqSegmentsReceived = new HashMap<Integer,Boolean>();
    private int n_segments;

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
        play_queue_listview.setItems(playQueueList);

        //will not be connected at startup, so disable buttons and such
        onDisconnect();

        addActionListeners();
    }

    /* ********************************
     * TRACK INFO UPDATE
     */

    public void updateTrack(Track t) {
        updateTrackLabel(t);
        updateLyricsPane(t);
        updateSeekMax(t);
    }

    /*
     * Populates the label at the top of the GUI with information
     * based on the given track
     */
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
        track_label.setFont(Font.font("Arial", 14));
    }

    /*
     * If possible, updates the lyrics pane
     * with lyrics from the given track
     */
    public void updateLyricsPane(Track t) {
        String artist = t.get("artist");
        String title = t.get("title");

        String lyrics;
//        if(artist != null && !artist.isEmpty()
//                && title != null && !title.isEmpty())
//            lyrics = LyricsFetcher.fetchLyrics(t.get("artist"), t.get("title"));
//        else
            lyrics = "[lyrics unavailable]";

        lyrics_text.setText(lyrics);
    }

    public void updateSeekMax(Track t) {
        String lengthString = (String) t.get("length");

        if(lengthString != null && lengthString instanceof String) {
            try {
                double length = Double.parseDouble(lengthString);
                seek_slider.setMax(length);
                seek_slider.setDisable(false);
            } catch (NumberFormatException e) {
                seek_slider.setDisable(true);
            }
        }
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
//        updatePlayQueueListView();
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

//    private void updatePlayQueueListView() {
//        playQueueList.clear();
//
//        ArrayList<Track> playQueueTracks = (ArrayList<Track>) library.clone();
//
//        //add selected tracks to listmodel, wrapping in TrackJListItem objects
//        for(Track t : playQueueTracks) {
//            trackList.add(new TrackListItem(t));
//        }
//    }

    private void setQueue(ArrayList<Track> tl) {
        playQueueList.clear();

        for(Track t : tl) {
            playQueueList.add(new PlayQueueTrackListItem(t));
        }
    }

    private void addToPlayQueue(PlayQueueTrackListItem t) {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("command", "ADD");
        ArrayList<String> tracksToAdd = new ArrayList<String>();
        tracksToAdd.add(t.getTrack().get("id"));
        request.put("ids",tracksToAdd);
        cc.sendMap(request);
    }

    private void removeFromPlayQueue(PlayQueueTrackListItem t) {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("command", "REMOVE");
        ArrayList<String> tracksToRemove = new ArrayList<String>();
        tracksToRemove.add(t.getTrack().get("id"));
        request.put("ids",tracksToRemove);
        cc.sendMap(request);
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
         * DATABASE UPDATE CONTROL
         */

        /* Update database */
        update_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Map<String, Object> request = new HashMap<String, Object>();
                request.put("command", "UPDATE");
                cc.sendMap(request);
            }
        });

        /* ****************************
         * PLAY QUEUE UPDATE CONTROL
         */

        /* Update Play Queue */
//        playqueue_tab.setOnSelectionChanged(new EventHandler<Event>() {
//            @Override
//            public void handle(Event e) {
//                Map<String, Object> request = new HashMap<String, Object>();
//                request.put("command", "QUEUE");
//                cc.sendMap(request);
//            }
//        });

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

        track_list_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TrackListItem>() {
                   @Override
                   public void changed(ObservableValue<? extends TrackListItem> observableValue, TrackListItem tli, TrackListItem tli2) {
                       if(tli2 == null) {
                           status_bar.setText("");
                       } else {
                           Track t = (Track) tli2.getTrack().clone();
                           t.remove("filepath");
                           t.remove("id");
                           status_bar.setText(t.toString());
                       }
                   }
               });

        track_list_view.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        TrackListItem tli = track_list_view.getSelectionModel().getSelectedItems().get(0);
                        PlayQueueTrackListItem pqtli = new PlayQueueTrackListItem(tli.getTrack());
                        addToPlayQueue(pqtli);
                    }
                }
            }
        });

        /* ****************************
         * PLAYQUEUE SELECTION
         */
//        play_queue_list_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PlayQueueTrackListItem>() {
//            @Override
//            public void changed(ObservableValue<? extends PlayQueueTrackListItem> observableValue, PlayQueueTrackListItem ptli, PlayQueueTrackListItem ptli2) {
//                if(ptli2 == null) {
//                    status_bar.setText("");
//                } else {
//                    Track t = (Track) ptli2.getTrack().clone();
//                    t.remove("filepath");
//                    t.remove("id");
//                    status_bar.setText(t.toString());
//                }
//            }
//        });
//
        play_queue_listview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        PlayQueueTrackListItem ptli = play_queue_listview.getSelectionModel().getSelectedItems().get(0);
                        PlayQueueTrackListItem pqtli = new PlayQueueTrackListItem(ptli.getTrack());
                        removeFromPlayQueue(pqtli);
                    }
                }
            }
        });
    }

    @Override
    public void processResponse(Map<String, Object> request, Map<String, Object> response) {
        System.err.println("[DEBUG]   " + request + "\n          " + response);

        String cmd = (String) request.get("command");

        if(cmd != null) {
            String status = (String) response.get("status_code");

            if(cmd.equals("TOGGLE")) {

            } else if(cmd.equals("PLAY")) {

            } else if(cmd.equals("PAUSE")) {

            } else if(cmd.equals("STOP")) {

            } else if(cmd.equals("NEXT")) {

            } else if(cmd.equals("PREV")) {

            } else if(cmd.equals("DATABASE")) {
                if(status != null && status.equals("200")) {
                    ArrayList<Track> newLibrary = new ArrayList<Track>();
                    ArrayList<Map<String,String>> data = (ArrayList<Map<String,String>>) response.get("data");

                    dbSegmentsReceived = new HashMap<Integer,Boolean>();

                    if(data != null) {
                        for(Map<String,String> t : data) {
                            newLibrary.add(new Track(t));
                        }

                        setLibrary(newLibrary);
                    }
                } else if (status != null && status.equals("206")) {
                    ArrayList<Track> newLibrary = (ArrayList<Track>) library.clone();
                    ArrayList<Map<String,String>> data = (ArrayList<Map<String,String>>) response.get("data");

                    if(data != null) {
                        for(Map<String,String> t : data) {
                            newLibrary.add(new Track(t));
                        }

                        setLibrary(newLibrary);
                    }
                }

                if (status != null && (status.equals("200") || status.equals("206"))) {
                    try {
                        int segment_id = Integer.parseInt((String) response.get("segment_id"));
                        n_segments = Integer.parseInt((String) response.get("n_segments"));

                        dbSegmentsReceived.put(segment_id, true);
                        for(int i = 0; i < segment_id; i++) {
                            Boolean received = dbSegmentsReceived.get(i);

                            if(received == null || received == false) {
                                Map<String, Object> new_request = new HashMap<String, Object>();
                                new_request.put("command", "DATABASE");
                                new_request.put("segment_id", Integer.toString(i));
                                System.err.println("[INFO]    Retry grabbing database segment " + i);
                                cc.sendMap(new_request);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[ERROR]   Bad parse on segment_id or n_segments: " + response);
                    }
                }



            } else if(cmd.equals("ADD")) {
                if(status != null && status.equals("200")) {
                    ArrayList<String> ids = (ArrayList<String>) request.get("ids");

                    ArrayList<Track> tracksToAdd = new ArrayList<Track>();

                    for(String id : ids) {
                        tracksToAdd.addAll(Database.search(library,"id",id));
                    }

                    if(tracksToAdd.size() == 1) {
                        status_bar.setText("Added 1 track (" + new PlayQueueTrackListItem(tracksToAdd.get(0)) + ") to play queue.");
                    } else {
                        status_bar.setText("Added " + tracksToAdd.size() + " tracks to play queue.");
                    }

                    Map<String, Object> newRequest = new HashMap<String, Object>();
                    newRequest.put("command", "QUEUE");
                    cc.sendMap(newRequest);
                }



            } else if(cmd.equals("UPDATE")) {
                Map<String, Object> newRequest = new HashMap<String, Object>();
                newRequest.put("command", "DATABASE");
                cc.sendMap(newRequest);



            } else if(cmd.equals("REMOVE")) {
                ArrayList<String> ids = (ArrayList<String>) request.get("ids");

                ArrayList<Track> tracksToAdd = new ArrayList<Track>();

                for(String id : ids) {
                    tracksToAdd.addAll(Database.search(library,"id",id));
                }

                if(tracksToAdd.size() == 1) {
                    status_bar.setText("Removed 1 track (" + new PlayQueueTrackListItem(tracksToAdd.get(0)) + " from play queue.");
                } else {
                    status_bar.setText("Removed " + tracksToAdd.size() + " tracks from play queue.");
                }

                Map<String, Object> newRequest = new HashMap<String, Object>();
                newRequest.put("command", "QUEUE");
                cc.sendMap(newRequest);



            } else if(cmd.equals("CURRENT")) {

            } else if(cmd.equals("QUEUE")) {
                if(status != null && status.equals("200")) {
                    ArrayList<Track> newQueue = new ArrayList<Track>();
                    ArrayList<Map<String,String>> data = (ArrayList<Map<String,String>>) response.get("data");

                    pqSegmentsReceived = new HashMap<Integer,Boolean>();

                    if(data != null) {
                        for(Map<String,String> t : data) {
                            newQueue.add(new Track(t));
                        }

                        setQueue(newQueue);
                    }
                } else if (status != null && status.equals("206")) {
                    ArrayList<Track> newQueue = (ArrayList<Track>) library.clone();
                    ArrayList<Map<String,String>> data = (ArrayList<Map<String,String>>) response.get("data");

                    if(data != null) {
                        for(Map<String,String> t : data) {
                            newQueue.add(new Track(t));
                        }

                        setQueue(newQueue);
                    }
                }

                if (status != null && (status.equals("200") || status.equals("206"))) {
                    try {
                        int segment_id = Integer.parseInt((String) response.get("segment_id"));
                        n_segments = Integer.parseInt((String) response.get("n_segments"));

                        pqSegmentsReceived.put(segment_id, true);
                        for(int i = 0; i < segment_id; i++) {
                            Boolean received = pqSegmentsReceived.get(i);

                            if(received == null || received == false) {
                                Map<String, Object> new_request = new HashMap<String, Object>();
                                new_request.put("command", "QUEUE");
                                new_request.put("segment_id", Integer.toString(i));
                                System.err.println("[INFO]    Retry grabbing queue segment " + i);
                                cc.sendMap(new_request);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[ERROR]   Bad parse on segment_id or n_segments: " + response);
                    }
                }



            } else if(cmd.equals("SET")) {

            } else if(cmd.equals("PLADD")) {

            } else if(cmd.equals("PLDEL")) {

            }

            //things to do no matter what was received

            if(response.containsKey("track_id")) {
                ArrayList<Track> tl = Database.search(library,"id",(String) response.get("track_id"));

                if(tl.isEmpty()) {
                    System.err.println("[WARN]    Currently playing track not found in client database");
                } else {
                    updateTrack(tl.get(0));
                }
            }

            if(response.containsKey("time")) {
                String timeString = (String) response.get("time");

                if(timeString != null && timeString instanceof String) {
                    try {
                        double time = Double.parseDouble(timeString);
                        seek_slider.setValue(time);
                    } catch (NumberFormatException e) {

                    }
                }
            }
        } else {
            System.err.println("[WARN]    Malformed request in processResponse(...)");
        }
    }

    @Override
    public void onConnect() {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("command", "DATABASE");
        cc.sendMap(request);

        request = new HashMap<String, Object>();
        request.put("command", "QUEUE");
        cc.sendMap(request);

        prev_button.setDisable(false);
        play_button.setDisable(false);
        next_button.setDisable(false);
        seek_slider.setDisable(false);
    }

    @Override
    public void onDisconnect() {
        prev_button.setDisable(true);
        play_button.setDisable(true);
        next_button.setDisable(true);
        seek_slider.setDisable(true);

        track_label.setText("--");
        track_label.setFont(Font.font("Arial", 14));
        seek_slider.setValue(0);
        lyrics_text.setText("");
        status_bar.setText("Disconnected");
    }

    @Override
    public void giveStatusInformation(String s) {
        status_bar.setText(s);
    }

    private class UpdateSeekTask extends TimerTask {
        @Override
        public void run() {

        }
    }

    public static class PlayQueueTrackListItem {
        Track t;

        PlayQueueTrackListItem(Track t) {
            this.t = t;
        }

        @Override
        public String toString() {
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

            return result;
        }

        public Track getTrack() {
            return t;
        }
    }

    public static class TrackListItem {
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
}
