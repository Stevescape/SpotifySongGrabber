import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class Shuffle
{
	public static void shufflePlaylist(PlaylistSimplified playlist)
	{
		
		PlaylistTrack[] playlistTracks = Requests.getPlaylistItems(playlist);
		
		ArrayList<Track> tracks = convertToTrack(playlistTracks);
		
		HashMap<String, ArrayList<Track>> artistMap = categorize(tracks);
		int length = tracks.size();
		
		HashMap<Integer, ArrayList<Track>> trackList = shuffleTrack(artistMap, length);
		
		Track[] combinedTracks = combineTrackList(trackList);
		
		
	}
	
	private static HashMap<Integer, ArrayList<Track>> shuffleTrack(HashMap<String, ArrayList<Track>> artistMap, int playlistLength)
	{
		HashMap<Integer, ArrayList<Track>> trackList = new HashMap<>();
		Random r = new Random();
		
		for (String artist : artistMap.keySet())
		{
			ArrayList<Track> tracks = artistMap.get(artist);
			
			int maxDisparity = (int) (playlistLength / tracks.size());
			int minDisparity = (int) (maxDisparity * 0.75);
			
			if (minDisparity >= maxDisparity)
				minDisparity = maxDisparity - 1;
			
			if (minDisparity < 1)
				minDisparity = 1;
			
			if (maxDisparity < minDisparity)
				maxDisparity = minDisparity + 5;
			
			
			boolean firstRun = true;
			int curPosition = 0;
			
			ArrayList<Track> copy = deepCopy(tracks);
			
			for (int i = 0; i < tracks.size(); i++)
			{
				// Grab a random track from trackList
				int trackIndex = getRandomTrack(copy);
				Track track = copy.get(trackIndex);
				copy.remove(trackIndex);
				
				// First track handled differently
				if (firstRun)
				{
					
					// Determine starting position of artist tracks
					// Artist with only 1 track is completely random
					if (tracks.size() == 1)
						curPosition = r.nextInt(0, playlistLength);
					else
						curPosition = r.nextInt(0, (int) (playlistLength * 0.05));
					
					firstRun = false;
				} else 
					curPosition += r.nextInt(minDisparity, maxDisparity);
				
				if (!trackList.containsKey(curPosition))
					trackList.put(curPosition, new ArrayList<Track>());
				trackList.get(curPosition).add(track);
			}
		}
		return trackList;
	}
	
	private static Track[] combineTrackList(HashMap<Integer, ArrayList<Track>> trackList)
	{
		 System.out.println(trackList.keySet());
		 return null;
	}
	
	private static ArrayList<Track> convertToTrack(PlaylistTrack[] tracks)
	{
		ArrayList<Track> combinedTracks = new ArrayList<Track>(); 
		
		String[] curIds = new String[tracks.length];
		
		for (int i = 0; i < tracks.length; i++)
		{
			PlaylistTrack curTrack = tracks[i];
			
			curIds[i] = curTrack.getTrack().getId();
		}
			
		for (int i = 0, track = 0; i <= tracks.length; i++, track++)
		{
			
		}
		
		return combinedTracks;
		
		
	}
	
	private static HashMap<String, ArrayList<Track>> categorize(ArrayList<Track> tracks)
	{
		HashMap<String, ArrayList<Track>> map = new HashMap<>();
		
		for (Track curTrack : tracks)
		{
			ArtistSimplified artist = curTrack.getArtists()[0];
			
			if (!map.containsKey(artist.getName()))
				map.put(artist.getName(), new ArrayList<Track>());
			
			map.get(artist.getName()).add(curTrack);
		}
		
		return map;
	}
	
	private static void addToPlaylist(PlaylistSimplified playlistDst, ArrayList<String> songs)
	{
		String[] songsList = new String[songs.size()];
		
		for (int i = 0; i < songs.size(); i++)
			songsList[i] = songs.get(i);
		
		Requests.addToPlaylist(playlistDst, songsList);
	}
	
	private static int getRandomTrack(ArrayList<Track> tracks)
	{
		Random r = new Random();
		
		return r.nextInt(0, tracks.size());
	}
	
	private static ArrayList<Track> deepCopy(ArrayList<Track> tracks)
	{
		ArrayList<Track> retVal = new ArrayList<>();
		
		for (Track track : tracks)
		{
			retVal.add(track);
		}
		
		return retVal;
	}
}

