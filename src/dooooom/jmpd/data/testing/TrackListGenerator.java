package dooooom.jmpd.data.testing;

import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.TrackList;

import java.util.ArrayList;
import java.util.Random;

public class TrackListGenerator {
	private static String getRandomSyllable() {
		Random r = new Random();
		
		String vowelPhenomes[] = { "a", "e", "i", "o", "u", "ae", "ee", "ie",
				"oe", "ue", "ei", "oo", "ar", "ur", "or", "au", "er", "ow",
				"oi", "air", "ear" };
		String consonantPhenomes[] = { "b", "d", "f", "g", "h", "j", "k", "l",
				"m", "n", "p", "r", "s", "t", "v", "w", "wh", "y", "z", "th",
				"ch", "sh" };
		
		
		String result = "";
		if(r.nextBoolean() == true) {
			
			result += consonantPhenomes[r.nextInt(vowelPhenomes.length)];
			result += vowelPhenomes[r.nextInt(vowelPhenomes.length)];
			result += consonantPhenomes[r.nextInt(vowelPhenomes.length)];
		} else {
			result += consonantPhenomes[r.nextInt(vowelPhenomes.length)];
			result += vowelPhenomes[r.nextInt(vowelPhenomes.length)];
		}
		
		return result;
	}
	
	private static String getRandomWord(int maxSyl) {
		Random r = new Random();
		
		int nSyl = r.nextInt(maxSyl) + 1;
		
		String result = "";
		
		for(int i = 0; i < nSyl; i++) {
			result += getRandomSyllable();
		}
		
		return result;
	}
	
	private static String getRandomSentance(int minwords, int maxwords, int maxSyl) {
		String result = "";
		Random r = new Random();
		
		int nWords = r.nextInt(maxwords - minwords + 1) + minwords;
		
		for(int i = 0; i < nWords; i++) {
			result += getRandomWord(maxSyl);
			if(i < nWords - 1)
				result += " ";
		}
		
		return result;
	}

	private static String getRandomString(int length) {
		Random r = new Random();

		String s = "";

		for (int i = 0; i < length; i++) {
			int n = r.nextInt(26) + 97;
			s = s.concat(Character.toString((char) n));
		}

		return s;
	}

	public static TrackList randomTracks(int nArtists) {
		TrackList tracks = new TrackList();

		final int albumsPerArtist = 3;
		final int tracksPerAlbum = 8;

		for (int i = 0; i < nArtists; i++) {
			String artist = getRandomString(4);

			for (int j = 0; j < albumsPerArtist; j++) {
				String album = getRandomString(6);

				for (int k = 0; k < tracksPerAlbum; k++) {
					Track track = new Track();
					track.put(
							"id",
							Integer.toString(i * albumsPerArtist
									* tracksPerAlbum + j * tracksPerAlbum + k));
					track.put("artist", artist);
					track.put("album", album);
					track.put("title", getRandomString(10));
					tracks.add(track);
				}
			}
		}

		return tracks;
	}
	
	public static ArrayList<Track> randomTracksGib(int nArtists) {
        ArrayList<Track> tracks = new ArrayList<Track>();

		final int albumsPerArtist = 3;
		final int tracksPerAlbum = 8;

		for (int i = 0; i < nArtists; i++) {
			String artist = getRandomSentance(1, 3, 3);

			for (int j = 0; j < albumsPerArtist; j++) {
				String album = getRandomSentance(1, 3, 3);

				for (int k = 0; k < tracksPerAlbum; k++) {
					Track track = new Track();
					track.put(
							"id",
							Integer.toString(i * albumsPerArtist
									* tracksPerAlbum + j * tracksPerAlbum + k));
					track.put("artist", artist);
					track.put("album", album);
					track.put("title", getRandomSentance(1, 3, 3));
					tracks.add(track);
				}
			}
		}

		return tracks;
	}
}
