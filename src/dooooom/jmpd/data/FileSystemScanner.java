package dooooom.jmpd.data;
/**
 * This class will carry out operations to help discover song
 * files in the user's music directory, and then return a TrackList for these songs
 * Besides constructors, the only method that should interface with the outside is returnTracks
 */

import net.sf.junidecode.Junidecode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSystemScanner
{
	String s = System.getProperty("file.separator");
	String musicFolderPath;

	/**
	 * We initialize constants that correspond to the default location of the
	 * Music library on different systems
	 */

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
        long startTime = System.currentTimeMillis();
		for(String path : paths)
		{
            if(Junidecode.unidecode(path).equals(path)) {
                Track t = new Track();
                t.put("artist", Junidecode.unidecode(MetadataExtractor.extractArtistFrom(path)));
                t.put("album", Junidecode.unidecode(MetadataExtractor.extractAlbumFrom(path)));
                t.put("title", Junidecode.unidecode(MetadataExtractor.extractTitleFrom(path)));
                t.put("length", MetadataExtractor.extractLengthFrom(path).toString());
                t.put("track", Junidecode.unidecode(MetadataExtractor.extractTrackFrom(path)));
                t.put("id", Integer.toString(nextID));
                t.put("filepath", path);

                trackList.add(t);
            } else {
                System.err.println("[WARN]    Ignoring file " + path + " because the path contains unicode.");
                System.err.println("          Recommend change filename to " + Junidecode.unidecode(path));
            }

            try {
                if (nextID % (paths.size() / 25) == 0)
                    System.err.println("[INFO]    Metadata collected " + (nextID * 100) / paths.size() + "%");
            } catch (ArithmeticException e) {

            }

			nextID++;
		}

        double executionTime = (startTime - System.currentTimeMillis()) / 1000.0;

        System.err.println("[INFO]    Metadata extraction finished operation on " + paths.size() + " files in " + executionTime + " seconds");

        ArrayList<Track> tl = new ArrayList<Track>(trackList);

		return tl;
	}


	public void pathRecurse(String folderPath, List<String> targetList)
	{
        File musicFolder = new File(folderPath);

        if(!musicFolder.exists()) {
            musicFolder.mkdirs();
        }

        File[] abstractPaths = musicFolder.listFiles();

        for(File path: abstractPaths)
        {   //only add it to the list if it's a .mp3 file
            if(path.isFile() && path.getPath().matches("^.+?\\.mp3")) {
                targetList.add(path.getPath());
            } else if(path.isDirectory()) { //otherwise we must go deeper
                pathRecurse(path.getPath(), targetList);
            } // or ignore it
        }
	}
}
