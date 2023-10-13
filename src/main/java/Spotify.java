import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Scanner;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.PauseUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;

public class Spotify
{
	static final String CLIENT_ID = "f59708637b0f46958f3a5ad18f926a46";
	static final String CLIENT_SECRET = "<Redacted>";
	static final URI REDIRECT_URI = SpotifyHttpManager.makeUri("http://localhost:8080/callback/");
	static String code = "";

	private static SpotifyApi spotifyApi = new SpotifyApi.Builder()
			.setClientId(CLIENT_ID)
			.setClientSecret(CLIENT_SECRET)
			.setRedirectUri(REDIRECT_URI)
			.build();
	private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
          .scope("user-library-read,"
          		+ "playlist-read-private,"
          		+ "user-modify-playback-state,"
          		+ "user-library-modify,playlist-modify-private,"
          		+ "playlist-read-collaborative")
          .build();
	
	public Spotify()
	{
		String refreshToken = null;
		try
		{
			FileInputStream bytes = new FileInputStream("object.ser");
			ObjectInputStream file = new ObjectInputStream(bytes);
			
			refreshToken = (String) file.readObject();
			file.close();
		} catch (IOException | ClassNotFoundException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
		
		// Generate new refresh and new access or refresh codes
		if (refreshToken == null)
		{
			generateNewCode();
		} else 
		{
			spotifyApi.setRefreshToken(refreshToken);
			authorizationCodeRefresh();
		}
		
	}
	
	public void generateNewCode()
	{
		Scanner input = new Scanner(System.in);
		
		authorizationCodeUri();
		
		System.out.println("Open URL and give code");
		code = input.nextLine();
		input.close();
		
		authorizationCode();
	}
	
	public void authorizationCodeUri() 
	{
	    final URI uri = authorizationCodeUriRequest.execute();
	    System.out.println(uri.toString());
	}
	
	public void authorizationCode()
	{
		try
		{
			AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
				    .build();
			final AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();
			
//			spotifyApi = new SpotifyApi.Builder()
//					.setAccessToken(credentials.getAccessToken())
//					.setRefreshToken(credentials.getRefreshToken())
//					.build();
			
			spotifyApi.setAccessToken(credentials.getAccessToken());
			spotifyApi.setRefreshToken(credentials.getRefreshToken());
			
			// Write refresh token to object.ser
			FileOutputStream bytes = new FileOutputStream("object.ser");
			ObjectOutputStream file = new ObjectOutputStream(bytes);
			
			file.writeObject(credentials.getRefreshToken());
			file.close();
			
		    System.out.println("Expires in: " + credentials.getExpiresIn());
		} catch (IOException | SpotifyWebApiException | ParseException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}	
	
	public void authorizationCodeRefresh()
	{
		try
		{
			AuthorizationCodeRefreshRequest codeRefresh = spotifyApi.authorizationCodeRefresh().build();
			
			AuthorizationCodeCredentials credentials = codeRefresh.execute();
			
			spotifyApi.setAccessToken(credentials.getAccessToken());
		} catch (IOException | SpotifyWebApiException | ParseException e)
		{
			System.out.println("Error: " + e.getMessage());
			// If cannot refresh, generate new code
			generateNewCode();
		}
	}
	
	public void resumeUserPlayback()
	{
		StartResumeUsersPlaybackRequest resumePlaybackRequest = spotifyApi.startResumeUsersPlayback().build();
		
		try
		{
			resumePlaybackRequest.execute();
			System.out.println("Starting song");
		} catch (IOException | SpotifyWebApiException | ParseException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public void pauseUserPlayback()
	{
		PauseUsersPlaybackRequest pauseUsersPlaybackRequest = spotifyApi.pauseUsersPlayback()
				.build();
		try
		{
			String string = pauseUsersPlaybackRequest.execute();
			System.out.println("Null: " + string);
		} catch (IOException | SpotifyWebApiException | ParseException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
}
