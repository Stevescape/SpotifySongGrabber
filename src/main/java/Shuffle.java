import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class Shuffle
{
	public static void shufflePlaylist(PlaylistSimplified playlist, String userId)
	{

		PlaylistTrack[] playlistTracks = Requests.getPlaylistItems(playlist);
		
		ArrayList<Track> tracks = convertToTrack(playlistTracks);

		HashMap<String, ArrayList<Track>> artistMap = categorize(tracks);
		int length = tracks.size();

		HashMap<Integer, ArrayList<Track>> trackList = shuffleTrack(artistMap, length);

		ArrayList<Track> combinedTracks = combineTrackList(trackList);

		String playlistName = "WockaShuffle " + playlist.getName();
		Playlist newPlaylist = Requests.createPlaylist(userId, playlistName);

		addToPlaylist(newPlaylist, combinedTracks);
	}

	private static HashMap<Integer, ArrayList<Track>> shuffleTrack(HashMap<String, ArrayList<Track>> artistMap,
			int playlistLength)
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

			if (maxDisparity <= minDisparity)
				maxDisparity = minDisparity + 3;

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

	private static ArrayList<Track> combineTrackList(HashMap<Integer, ArrayList<Track>> trackList)
	{
		ArrayList<Integer> numbersArr = new ArrayList<Integer>();
		Object[] numbers = trackList.keySet().toArray();

		for (int i = 0; i < numbers.length; i++)
			numbersArr.add((int) numbers[i]);

		Collections.sort(numbersArr);

		ArrayList<Track> tracks = new ArrayList<>();

		for (int i : numbersArr)
		{
			ArrayList<Track> curSongs = trackList.get(i);

			while (curSongs.size() > 0)
			{
				int random = getRandomTrack(curSongs);
				tracks.add(curSongs.get(random));
				curSongs.remove(random);
			}
		}

		return tracks;
	}

	private static ArrayList<Track> convertToTrack(PlaylistTrack[] tracks)
	{
		ArrayList<Track> combinedTracks = new ArrayList<Track>();

		String[] ids = new String[tracks.length];

		for (int i = 0; i < tracks.length; i++)
		{
			PlaylistTrack curTrack = tracks[i];

			ids[i] = curTrack.getTrack().getId();
		}

		String[] curIds = new String[50];

		for (int i = 0, track = 0; i < tracks.length; i++, track++)
		{
			// On the track after curIds is full
			if (track >= 50 || i == tracks.length - 1)
			{
				if (i == tracks.length - 1)
					curIds[track] = ids[i];
				ArrayList<Track> curSongs = Requests.getSeveralTracks(curIds);
				combinedTracks.addAll(curSongs);
				curIds = new String[50];

				// Reset track
				track = 0;
			}

			if (i == tracks.length - 1)
				break;
			curIds[track] = ids[i];
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

	private static void addToPlaylist(Playlist playlistDst, ArrayList<Track> songs)
	{
		String[] curList = null;
		if (songs.size() > 50)
			curList = new String[50];
		else
			curList = new String[songs.size()];

		for (int i = 0, track = 0; i < songs.size(); i++, track++)
		{
			// On the track after curIds is full
			if (track >= 50 || i == songs.size() - 1)
			{
				if (i == songs.size() - 1)
					curList[track] = songs.get(i).getUri();
				Requests.addToPlaylist(playlistDst, curList);

				if (songs.size() - (i + 1) > 50)
					curList = new String[50];
				else
					curList = new String[songs.size() - i];

				// Reset track
				track = 0;
			}

			if (i == songs.size() - 1)
				break;
			curList[track] = songs.get(i).getUri();
		}

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
