import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class SongGrabber extends Application
{
	Spotify spotify = new Spotify();
	
	// GUI
	Button pauseBtn = new Button("Pause");
	Button startBtn = new Button("Start");
	GridPane pane;
	
	public static void main(String[] args)
	{
		launch(args);
	}

	
	@Override
	public void start(Stage stage)
	{
		
		buildGui();
		registerHandlers();
		
		
		
		
		Scene scene = new Scene(pane, 500, 500);

		stage.setScene(scene);
		stage.show();

	}
	
	private void buildGui()
	{
		pane = new GridPane();
		pane.add(startBtn, 0, 0);
		pane.add(pauseBtn, 1, 0);
	}
	
	private void registerHandlers()
	{
		pauseBtn.setOnAction(event -> 
		{
			spotify.pauseUserPlayback();
		});
		
		startBtn.setOnAction(event -> 
		{
			spotify.resumeUserPlayback();
		});
		
	}

}
