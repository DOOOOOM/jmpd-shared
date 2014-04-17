package dooooom.jmpd.client.gui.javafx;

import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.testing.TrackListGenerator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    private LibraryController libraryController;

    @FXML private ListView<String> artist_list_view;
    @FXML private ListView<String> album_list_view;
    @FXML private ListView<Track> track_list_view;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        libraryController = new LibraryController(artist_list_view,album_list_view,track_list_view,this);

        /*
		 * This line adds random garbage data to the library in order to test library filter panes.
		 * Comment it out if you don't want garbage data
		 */
        libraryController.setLibrary(TrackListGenerator.randomTracksGib(10));
    }
}
