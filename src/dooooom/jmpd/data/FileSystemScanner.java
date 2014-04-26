package dooooom.jmpd.data;
/**
 * This class will carry out operations to help discover song
 * files in the user's music directory, and then return a TrackList for these songs
 * Besides constructors, the only method that should interface with the outside is returnTracks
 */

import dooooom.jmpd.daemon.DaemonMainController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSystemScanner
{
	String osName = System.getProperty("os.name").toLowerCase();
	String userName = System.getProperty("user.name");
	String s = System.getProperty("file.separator");
	String musicFolderPath;

	/**
	 * The osName variable will be assigned the value of a String
	 * that corresponds to the OS designation.
	 * The userName will be assigned the user name.
	 * s is the file separator (/ or \) dependent on systems.
	 */

	String windows7MusicFolderName = "C:" +s+ "Users" +s+ userName +s+ "Music";
	String windows8MusicFolderName = windows7MusicFolderName;
	String windowsVistaMusicFolderName = windows7MusicFolderName;
	String windowsXPMusicFolderName = "C:" +s+ "Documents and Settings" +s+ userName +s+ "My Documents" +s+ "My Music";
	String linuxMusicFolderName = s+ "home" +s+ "" + userName.toLowerCase() +s+ "music";
	String solarisMusicFolderName = linuxMusicFolderName;
	String macMusicFolderName = "/Users/" + userName.toLowerCase() + "/Music";

	/**
	 * We initialize constants that correspond to the default location of the
	 * Music library on different systems
	 */

//	public FileSystemScanner() // default constructor
//	{
//		musicFolderPath = findMusicFolder();
//	}

	public String getFolderPath()
	{
		return musicFolderPath;
	}

	public String getS()
	{
		return s;
	}


	public FileSystemScanner(String path)
	{
		musicFolderPath = path;
	}

	public ArrayList<Track> returnTracks()
	{
		ArrayList<Track> trackList = new ArrayList<Track>();
		int nextID = 1;
		ArrayList<String> paths = new ArrayList<String>();
        System.err.println("[INFO]    Begin scanning filesystem at " + musicFolderPath);
        pathRecurse(musicFolderPath, paths);
        System.err.println("[INFO]    Finished scanning filesystem, now extracting metadata");
		for(String path : paths)
		{
			Track t = new Track();
			t.put("artist", MetadataExtractor.extractArtistFrom(path));
			t.put("album", MetadataExtractor.extractAlbumFrom(path));
			t.put("title", MetadataExtractor.extractTitleFrom(path));
			t.put("length", MetadataExtractor.extractLengthFrom(path).toString());
			t.put("track", MetadataExtractor.extractTrackFrom(path));
			t.put("id", new Integer(nextID).toString());
			t.put("filepath", path);

			trackList.add(t);

            try {
                if (nextID % (paths.size() / 25) == 0)
                    System.err.println("[INFO]    Metadata collected " + (nextID * 100) / paths.size());
            } catch (ArithmeticException e) {

            }

			nextID++;
		}

        System.err.println("[INFO]    Metadata extraction finished");

		TrackList tl = new TrackList(trackList);

		return tl;
	}


	public void pathRecurse(String folderPath, List<String> targetList)
	{
        File musicFolder = new File(folderPath);

        File[] abstractPaths = musicFolder.listFiles();

        for(File path: abstractPaths)
        {   //only add it to the list if it's a .mp3 file
            if(path.isFile() && path.getPath().matches("^.+?\\.mp3")) {
                targetList.add(path.getPath());
            } else if(path.isDirectory()) { //otherwise we must go deeper
                pathRecurse(path.getPath(), targetList);
            } // /or ignore it
        }

	}




	private String findMusicFolder()
	{
        if(DaemonMainController.getMusicFolder() == null) {
            if(osName.equalsIgnoreCase("windows 7"))
                musicFolderPath = windows7MusicFolderName;

            else if(osName.equalsIgnoreCase("windows vista"))
                musicFolderPath = windowsVistaMusicFolderName;

            else if(osName.equalsIgnoreCase("windows xp"))
                musicFolderPath = windowsXPMusicFolderName;

            else if(osName.equalsIgnoreCase("linux"))
                musicFolderPath = linuxMusicFolderName;

            else if(osName.equalsIgnoreCase("mac os") || osName.equalsIgnoreCase("mac os x"))
                musicFolderPath = macMusicFolderName;

            else if(osName.equalsIgnoreCase("solaris"))
                musicFolderPath = solarisMusicFolderName;

            return musicFolderPath;
        } else {
            return DaemonMainController.getMusicFolder();
        }
	}
}
