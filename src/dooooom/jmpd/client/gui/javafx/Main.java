package dooooom.jmpd.client.gui.javafx;

//        Java Music Player Daemon / Java Music Player Client
//        Copyright (C) 2014  Andrew Frost, Essenam Kakpo, Zach Purnell, Philip Rodning, and Obi
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <http://www.gnu.org/licenses/>.

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main_view.fxml"));
        primaryStage.setTitle("jMPC");
        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add
                (MainViewController.class.getResource("/assets/client.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
