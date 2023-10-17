import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import se.michaelthelin.spotify.requests.data.player.PauseUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.apache.hc.core5.http.ParseException;

public class Spotify
{
	protected static final String clientID = "f59708637b0f46958f3a5ad18f926a46";
	protected static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080/callback/");
	protected static String verifier = generateVerifier();
	protected static String challenge = generateChallenge();
	protected static URI uri;
	protected static String code;
	
	
	private static SpotifyApi api = new SpotifyApi.Builder()
			.setClientId(clientID)
			.setRedirectUri(redirectUri)
			.build();
	
	private static PauseUsersPlaybackRequest pauseRequest;
	private static StartResumeUsersPlaybackRequest resumeRequest;
	
	public static void pausePlayback()
	{
		try
		{
			pauseRequest.execute();
			System.out.println("Pausing Song");
		} catch (IOException | ParseException | SpotifyWebApiException e)
		{
			System.out.println("Pause Error: " + e.getMessage());
		}
	}
	
	public static void resumePlayback()
	{
		try
		{
			resumeRequest.execute();
			System.out.println("Resuming Song");
		} catch (IOException | ParseException | SpotifyWebApiException e)
		{
			System.out.println("Resume/Start Error: " + e.getMessage());
		}
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
			if (refreshToken != null)
			{
				api.setRefreshToken(refreshToken);
				authCodeRefresh();
			} else 
			{
				authWorkflow();
			}
		} catch (ClassNotFoundException | IOException e)
		{
			System.out.println("Constructor Error: " + e.getMessage());
			authWorkflow();
		}
		initializeRequests();
	}
	
	private static void authCodeUriRequest()
	{
		AuthorizationCodeUriRequest authUriRequest = api.authorizationCodePKCEUri(challenge)
				.scope("user-library-read,"
						+ "playlist-read-private,"
						+ "user-modify-playback-state,"
						+ "user-library-modify,"
						+ "playlist-modify-private,"
						+ "playlist-read-collaborative")
				.show_dialog(false)
				.build();
		
		uri = authUriRequest.execute(); 
		System.out.println(uri);
	}
	
	private static void authCodeRequest()
	{
		AuthorizationCodePKCERequest authRequest = api.authorizationCodePKCE(code, verifier).build();
		
		try
		{
			AuthorizationCodeCredentials credentials = authRequest.execute();
			
			api.setAccessToken(credentials.getAccessToken());
			api.setRefreshToken(credentials.getRefreshToken());
			writeRefreshToken();
			System.out.println("Expires in: " + credentials.getExpiresIn());
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("AuthRequest Error: " + e.getMessage());
		}
	}
	
	private static void authCodeRefresh()
	{
		AuthorizationCodePKCERefreshRequest authRefreshRequest = api.authorizationCodePKCERefresh()
				.build();
		
		try
		{
			AuthorizationCodeCredentials credentials = authRefreshRequest.execute();
			
			api.setAccessToken(credentials.getAccessToken());
			api.setRefreshToken(credentials.getRefreshToken());
			writeRefreshToken();
			System.out.println("Expires in: " + credentials.getExpiresIn());
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Refresh Error: " + e.getMessage());
			authWorkflow();
		}
	}
	
	private static void initializeRequests()
	{
		pauseRequest = api.pauseUsersPlayback().build();
		resumeRequest = api.startResumeUsersPlayback().build();
	}
	
	private static void authWorkflow()
	{
		Scanner input = new Scanner(System.in);
		
		authCodeUriRequest();
		System.out.println("Open URL and give code:");
		code = input.nextLine();
		authCodeRequest();
		
		input.close();
	}
	
	private static void writeRefreshToken()
	{
		
		try
		{
			FileOutputStream bytes = new FileOutputStream("object.ser");
			ObjectOutputStream file = new ObjectOutputStream(bytes);
			
			file.writeObject(api.getRefreshToken());
			file.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String generateVerifier()
	{
		Random r = new Random();
		int length = r.nextInt(43, 129);
		String code = "";
		String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		for (int i = 0; i < length; i++)
		{
			code += possible.charAt(r.nextInt(possible.length()));
		}
		
		return code;
	}
	
	private static String generateChallenge()
	{
		try
		{
			MessageDigest encoder = MessageDigest.getInstance("SHA-256");
			byte[] encoded = encoder.digest(verifier.getBytes());
			
			String encodedStr = Base64.getUrlEncoder().encodeToString(encoded);
			encodedStr = encodedStr.substring(0, encodedStr.length()-1);
			return encodedStr;
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
