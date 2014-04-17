package dooooom.jmpd;
/**
 * This class will carry out operations to help discover song
 * files in the user's music directory, and then return a TrackList for these songs
 * Besides constructors, the only method that should interface with the outside is returnTracks
 */

import dooooom.jmpd.MetadataExtractor;
import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.TrackList;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

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
	String linuxMusicFolderName = s+ "home" +s+ "" + userName.toLowerCase() +s+ "Music";
	String solarisMusicFolderName = linuxMusicFolderName;
	String macMusicFolderName = "/Users/" + userName.toLowerCase() + "/Music";	
	
	/**
	 * We initialize constants that correspond to the default location of the 
	 * Music library on different systems
	 */
	
	public FileSystemScanner() // default constructor
	{
		musicFolderPath = findMusicFolder();	
	}
	
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
		String[] paths = this.returnPathNames();
		for(String path : paths)
		{
			Track t = new Track();
			t.put("artist", MetadataExtractor.extractArtistFrom(path));
			t.put("album", MetadataExtractor.extractAlbumFrom(path));
			t.put("title", MetadataExtractor.extractTitleFrom(path));
			t.put("id", new Integer(nextID).toString());
			t.put("filepath", path);
			trackList.add(t);
			nextID++;
		}

		TrackList tl = new TrackList(trackList);

		return tl;
	}
	
	public String[] returnPathNames() 
	/**
	 * This method provided with the default music directory for the system,
	 * will return an array of Strings, each corresponding to the pathName
	 * of the file in question. Directories will not be returned
	 * @ Pre: the musicFolderPath is a valid String
	 * @ Pre: the musicFolderPath is a valid directory path
	 * @ Post: None of the paths returned by the method are folders
	 * 
	 */	
	{
		File musicFolder = new File(musicFolderPath);
		
		File[] abstractPaths = musicFolder.listFiles();		
		
		String[] arrayToReturn = new String[abstractPaths.length];
		
		for(int i = 0, j = 0; i < abstractPaths.length && j < arrayToReturn.length; j++)
		{
			if(abstractPaths[j].isFile()) //removes folders from list
			{	
				arrayToReturn[i] = abstractPaths[j].getPath();
				i++;
			}
		}
		
		List<String> list = new ArrayList<String>();

	    for(String str : arrayToReturn)  // flush null values
	    {
	       if(str != null && s.length() > 0) 
	       {
	          if( (str.substring(str.length()-4,str.length()).equalsIgnoreCase(".mp3")) )
	          	list.add(str);
	       }
	    }
		
		arrayToReturn = list.toArray(new String[list.size()]);		
		
		return arrayToReturn;
	}
	
	
	private String findMusicFolder()
	{
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
	}	
}
