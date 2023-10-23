import java.util.Scanner;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;


public class SongGrabber extends Application
{
	Spotify spotify = new Spotify();
	
	// GUI
	Button pauseBtn = new Button("Pause");
	Button startBtn = new Button("Start");
	Button searchBtn = new Button("Grab Songs");
	
	TextField searchField = new TextField("");
	
	ListView<Artist> foundArtists = new ListView<Artist>();
	ListView<PlaylistSimplified> userPlaylists = new ListView<PlaylistSimplified>();
	
	BorderPane pane;
	
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
		pane = new BorderPane();
		
		BorderPane buttonPane = new BorderPane();
		HBox buttons = new HBox();
		buttons.setSpacing(150);
		buttons.getChildren().addAll(pauseBtn, startBtn, searchBtn);
		
		buttonPane.setCenter(buttons);
		
		GridPane topBar = new GridPane();
		Label searchLbl = new Label("Search for Artist");
		
		topBar.add(searchLbl, 0, 0);
		topBar.add(searchField, 0, 1);
		
		GridPane viewPane = new GridPane();
		viewPane.add(foundArtists, 0, 0);
		viewPane.add(userPlaylists, 1, 0);	
		
		buildPlaylistView();
		
		pane.setTop(topBar);
		pane.setCenter(viewPane);
		pane.setBottom(buttonPane);
	}
	
	private void registerHandlers()
	{
		pauseBtn.setOnAction(event -> 
		{
			Spotify.pausePlayback();
		});
		
		startBtn.setOnAction(event -> 
		{
			Spotify.resumePlayback();
		});
		
		searchField.setOnAction(event -> 
		{
			Artist[] allArtists = Spotify.searchArtists(searchField.getText());
			
			ObservableList<Artist> artistList = FXCollections.observableArrayList(allArtists);
			foundArtists.setItems(artistList);
			setArtistCellFactory();
			
		});
		
	}
	
	private void buildPlaylistView()
	{
		PlaylistSimplified[] playlists = Spotify.getUserPlaylists();
		PlaylistSimplified playlist = playlists[0];
		playlist.getName();
		ObservableList<PlaylistSimplified> playlistArr = FXCollections.observableArrayList(playlists);
		userPlaylists.setItems(playlistArr);
		setPlaylistCellFactory();
	}
	
	private void setArtistCellFactory()
	{
		foundArtists.setCellFactory(new Callback<ListView<Artist>, ListCell<Artist>>() 
				{

					@Override
					public ListCell<Artist> call(ListView<Artist> param)
					{
						ListCell<Artist> cell = new ListCell<Artist>() 
								{
									@Override
									protected void updateItem(Artist artist, boolean empty)
									{
										super.updateItem(artist, empty);
										if (artist != null)
										{
											setText(artist.getName());
										} else
										{
											setText("");
										}
									}
								};
						return cell;
					}
			
				});
	}
	
	private void setPlaylistCellFactory()
	{
		userPlaylists.setCellFactory(new Callback<ListView<PlaylistSimplified>, ListCell<PlaylistSimplified>>() 
				{

					@Override
					public ListCell<PlaylistSimplified> call(ListView<PlaylistSimplified> param)
					{
						ListCell<PlaylistSimplified> cell = new ListCell<PlaylistSimplified>() 
								{
									@Override
									protected void updateItem(PlaylistSimplified playlist, boolean empty)
									{
										super.updateItem(playlist, empty);
										if (playlist != null)
										{
											setText(playlist.getName());
										} else
										{
											setText("");
										}
									}
								};
						return cell;
					}
			
				});
	}
	
}
