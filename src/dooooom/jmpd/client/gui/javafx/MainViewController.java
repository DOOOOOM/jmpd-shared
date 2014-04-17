package dooooom.jmpd.client.gui.javafx;

import dooooom.jmpd.client.UDPClient;
import dooooom.jmpd.data.Command;
import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.testing.TrackListGenerator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    private LibraryController libraryController;

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

    private UDPClient client;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        libraryController = new LibraryController(artist_list_view,album_list_view,track_list_view,this);

        client = new UDPClient();
        Thread clientThread = new Thread(client);
        clientThread.start();

        /*
		 * This line adds random garbage data to the library in order to test library filter panes.
		 * Comment it out if you don't want garbage data
		 */
        libraryController.setLibrary(TrackListGenerator.randomTracksGib(1000));

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
        result += s + " - ";

        track_label.setText(result);
    }

    private void addActionListeners() {
        /*
         * Playback Controls
         */
        play_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    client.sendMessage(Command.TOGGLE, "");
                } catch (Exception e) {

                }
            }
        });

        prev_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    client.sendMessage(Command.PREV, "");
                } catch (Exception e) {

                }
            }
        });

        next_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    client.sendMessage(Command.NEXT, "");
                } catch (Exception e) {

                }
            }
        });
    }
}
