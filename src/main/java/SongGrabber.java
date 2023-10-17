import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class SongGrabber extends Application
{
	Spotify spotify = new Spotify();
	Button pauseBtn = new Button("Pause");
	Button startBtn = new Button("Start");
	Button getDevicesBtn = new Button("Devices");
	
	public static void main(String[] args)
	{
		
		
		launch(args);
		
		
	}

	
	@Override
	public void start(Stage stage)
	{
		GridPane pane = new GridPane();
		
		pauseBtn.setOnAction(event -> 
		{
			spotify.pauseUserPlayback();
		});
		
		startBtn.setOnAction(event -> 
		{
			spotify.resumeUserPlayback();
		});
		
		
		pane.add(startBtn, 0, 0);
		pane.add(pauseBtn, 1, 0);
		pane.add(getDevicesBtn, 2, 0);
		
		Scene scene = new Scene(pane, 500, 500);

		stage.setScene(scene);
		stage.show();

	}

}
