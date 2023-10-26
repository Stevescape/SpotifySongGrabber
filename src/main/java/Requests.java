/**
 *  Deals with creating requests to send to the spotify api
 * 
 * 	@author Steven Truong
 */
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import se.michaelthelin.spotify.requests.data.player.PauseUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchArtistsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetSeveralTracksRequest;

public class Requests
{
	private static PauseUsersPlaybackRequest pauseRequest;
	private static StartResumeUsersPlaybackRequest resumeRequest;
	private static SearchArtistsRequest searchRequest;
	private static GetListOfCurrentUsersPlaylistsRequest playlistRequest;
	private static GetAlbumsTracksRequest getAlbumTracksRequest;
	private static GetArtistsAlbumsRequest getAlbumsRequest;
	private static GetPlaylistsItemsRequest getPlaylistItemsRequest;
	private static AddItemsToPlaylistRequest addToPlaylistRequest;
	private static GetSeveralTracksRequest getSeveralTracksRequest;
	
	private static SpotifyApi api;
	
	public static void setApi(SpotifyApi api)
	{
		Requests.api = api;
		initializeRequests();
	}
	
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
	
	public static Artist[] searchArtists(String query)
	{
		searchRequest = api.searchArtists(query).limit(10).build();
		
		try
		{
			Paging<Artist> artists = searchRequest.execute();
			return artists.getItems();
			
		} catch (IOException | ParseException | SpotifyWebApiException e)
		{
			System.out.println("Search Error: " + e.getMessage());
		}
		// For compiler
		return null;
	}
	
	public static AlbumSimplified[] getArtistAlbums(Artist artist)
	{
		getAlbumsRequest = api.getArtistsAlbums(artist.getId()).build();
		
		try
		{
			return getAlbumsRequest.execute().getItems();
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Album Get Error: " + e.getMessage());
		}
		return null;
	}
	
	public static TrackSimplified[] getAlbumTracks(AlbumSimplified album)
	{
		getAlbumTracksRequest = api.getAlbumsTracks(album.getId()).build();
		
		try
		{
			return getAlbumTracksRequest.execute().getItems();
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Album Track Get Error: " + e.getMessage());
		}
		return null;
	}
	
	public static PlaylistSimplified[] getUserPlaylists()
	{
		try
		{
			Paging<PlaylistSimplified> playlists = playlistRequest.execute();
			return playlists.getItems();
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Playlist Error: " + e.getMessage());
		}
		return null;
	}
	
	public static PlaylistTrack[] getPlaylistItems(PlaylistSimplified playlist)
	{
		
		getPlaylistItemsRequest = api.getPlaylistsItems(playlist.getId()).build();
		
		try
		{
			return getPlaylistItemsRequest.execute().getItems();
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Track Get Error: " + e.getMessage());
		}
		return null;
	}
	
	public static void addToPlaylist(PlaylistSimplified playlist, String[] songs)
	{
		addToPlaylistRequest = api.addItemsToPlaylist(playlist.getId(), songs).build();
		
		try
		{
			addToPlaylistRequest.execute();
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Playlist Adding Error: " + e.getMessage());
		}
		
	}
	
	public static ArrayList<Track> getSeveralTracks(String[] ids)
	{
		getSeveralTracksRequest = api.getSeveralTracks(ids).build();
		
		try
		{
			Track[] tracks = getSeveralTracksRequest.execute();
			ArrayList<Track> tracksArr = new ArrayList<>();
			
			for (Track curTrack : tracks)
			{
				tracksArr.add(curTrack);
			}
			
			return tracksArr;
		} catch (ParseException | SpotifyWebApiException | IOException e)
		{
			System.out.println("Track Grabbing Error: " + e.getMessage());
		}
		
		return null;
	}
	
	private static void initializeRequests()
	{
		pauseRequest = api.pauseUsersPlayback().build();
		resumeRequest = api.startResumeUsersPlayback().build();
		playlistRequest = api.getListOfCurrentUsersPlaylists().build();
	}
}
