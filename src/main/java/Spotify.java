import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.PauseUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;

public class Spotify
{
	static final String clientId = "f59708637b0f46958f3a5ad18f926a46";
	static final String clientSecret = "cbfe0157f7374d4aa11d2e66176bba4a";
	static final URI redirectURI = SpotifyHttpManager.makeUri("http://localhost:8080/callback/");
	static String code = "";
	static final String codeVerifier = generateVerifier();
	static final String codeChallenge = generateChallenge(codeVerifier);
	

	private static SpotifyApi spotifyApi = new SpotifyApi.Builder()
			.setClientId(clientId)
			.setRedirectUri(redirectURI)
			.build();
	private static AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(codeChallenge)
          .scope("user-library-read,"
          		+ "playlist-read-private,"
          		+ "user-modify-playback-state,"
          		+ "user-read-playback-state,"
          		+ "user-library-modify,playlist-modify-private,"
          		+ "playlist-read-collaborative")
          .build();
	
	private static StartResumeUsersPlaybackRequest resumePlaybackRequest;
	private static PauseUsersPlaybackRequest pauseUsersPlaybackRequest;
	
	private static void instantiateRequests()
	{
		resumePlaybackRequest = spotifyApi.startResumeUsersPlayback().build();
		pauseUsersPlaybackRequest = spotifyApi.pauseUsersPlayback().build();
	}
	
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
			AuthorizationCodePKCERequest authorizationCodeRequest = spotifyApi.authorizationCodePKCE(code, codeVerifier)
				    .build();
			AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();
			
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
			
			instantiateRequests();
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
			AuthorizationCodePKCERefreshRequest codeRefresh = spotifyApi.authorizationCodePKCERefresh().build();
			AuthorizationCodeCredentials credentials = codeRefresh.execute();
			
			spotifyApi.setAccessToken(credentials.getAccessToken());
			spotifyApi.setRefreshToken(credentials.getRefreshToken());
			instantiateRequests();
		} catch (IOException | SpotifyWebApiException | ParseException e)
		{
			System.out.println("Error: " + e.getMessage());
			// If cannot refresh, generate new code
			generateNewCode();
		}
	}
	
	public void resumeUserPlayback()
	{
		try
		{
			resumePlaybackRequest.execute();
			System.out.println("Resuming song");
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public void pauseUserPlayback()
	{
		try
		{
			pauseUsersPlaybackRequest.execute();
			
			System.out.println("Pausing song");
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	
	private static String generateVerifier()
	{
		System.out.println("Generating Verifier");
		Random r = new Random();
		String challenge = "";
		int maxLength = r.nextInt(43, 129);
		String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		for (int i = 0; i < maxLength; i++)
		{
			challenge += possible.charAt(r.nextInt(possible.length()));
		}
		return challenge;
		
		
	}
	
	private static String generateChallenge(String verifier)
	{
		System.out.println("Generating Challenge");
		String encoded = null;
		try
		{
			MessageDigest encoder = MessageDigest.getInstance("SHA-256");
			byte[] hash = encoder.digest(verifier.getBytes());
			
			encoded = Base64.getUrlEncoder().encodeToString(hash);
			encoded = encoded.substring(0, encoded.length()-1);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			System.out.println("Error: Failed to generate challenge");
		}
		
		
		
		return encoded;
	}
	
	
}
