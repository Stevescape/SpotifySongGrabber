import java.util.ArrayList;
import java.util.HashSet;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

public class Parser
{
	public static ArrayList<TrackSimplified> parseArtistSongs(Artist artist, PlaylistSimplified playlistDst)
	{
		
		
		HashSet<String> filterSongs = grabPlaylistTracks(playlistDst);
		
		ArrayList<String> validSongs = new ArrayList<>();
		ArrayList<TrackSimplified> invalidSongs = new ArrayList<>();
		
		// Grab all of artists albums
		AlbumSimplified[] albums = Requests.getArtistAlbums(artist);
		
		// Go through each album, check if in hashset (or has remix/instr)
		for (AlbumSimplified curAlbum : albums)
		{
			TrackSimplified[] albumTracks = Requests.getAlbumTracks(curAlbum);
			for (TrackSimplified curTrack : albumTracks)
			{
				String curName = curTrack.getName().toLowerCase();
				
				if (filterSongs.contains(curName))
				{
					invalidSongs.add(curTrack);
					continue;
				}
				
				if (curName.contains("ver."))
				{
					invalidSongs.add(curTrack);
					continue;
				}
				
				if (curName.contains("instr.") || curName.contains("inst.") || curName.contains("instrumental") || curName.contains("(inst)") || curName.contains("(instr)"))
				{
					invalidSongs.add(curTrack);
					continue;
				}
				
				if (curName.contains("remix"))
				{
					invalidSongs.add(curTrack);
					continue;
				}
				
				validSongs.add(curTrack.getUri());
				filterSongs.add(curName);
			}
		}
		
		if (validSongs.size() == 0)
		{
			return invalidSongs;
		}
		
		addToPlaylist(playlistDst, validSongs);
		return invalidSongs;
	}
	
	
	private static HashSet<String> grabPlaylistTracks(PlaylistSimplified playlist)
	{
		HashSet<String> filterSongs = new HashSet<>();
		
		// Add playlist tracks to the curList
		PlaylistTrack[] playlistTracks = Requests.getPlaylistItems(playlist);
		
		for (PlaylistTrack curTrack : playlistTracks)
		{
			IPlaylistItem curItem = curTrack.getTrack();
			filterSongs.add(curItem.getName().toLowerCase());
		}
		
		return filterSongs;
	}
	
//	private static String processTrackName(String trackName)
//	{
//		String[] splitName = trackName.split(" ");
//		String newWord = "";
//		boolean inParenth = false;
//		for (int i = 0; i < splitName.length; i++)
//		{
//			String curWord = splitName[i];
//			char firstLetter = curWord.charAt(0);
//			char lastLetter = curWord.charAt(curWord.length()-1);
//			
//			// Determine if either letter is a parenthesis
//			char toCheck = (lastLetter == ')') ? lastLetter : firstLetter;
//			
//			
//			if (toCheck == '(')
//					inParenth = true;
//			
//			if (!inParenth)
//			{
//				newWord += (i == 0) ? curWord : " " + curWord;	
//			}
//			
//			if (toCheck == ')')
//					inParenth = false;
//		}
//		
//		return newWord;
//	}
	
	private static void addToPlaylist(PlaylistSimplified playlistDst, ArrayList<String> songs)
	{
		String[] songsList = new String[songs.size()];
		
		for (int i = 0; i < songs.size(); i++)
			songsList[i] = songs.get(i);
		
		Requests.addToPlaylist(playlistDst, songsList);
	}
}
