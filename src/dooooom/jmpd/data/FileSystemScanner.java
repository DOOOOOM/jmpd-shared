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
			t.put("length", MetadataExtractor.extractLengthFrom(path).toString());
			t.put("track", MetadataExtractor.extractTrackFrom(path));
			t.put("id", new Integer(nextID).toString());
			t.put("filepath", path);
			trackList.add(t);
			nextID++;
		}

		TrackList tl = new TrackList(trackList);

		return tl;
	}
	
	
	public void pathRecurse(File file, List<String> targetList)
	{		
		if(file.isDirectory())
		{
			if (file.list().length > 0)
			{	
				File[] f = file.listFiles();
				for(File fil : f)
					pathRecurse(fil, targetList);
			}
			else return;			
		}
		else
		{
			String str = file.getPath();
			if(str.substring(str.length()-4,str.length()).equalsIgnoreCase(".mp3")) 
				targetList.add(file.getPath());
		}
		
		//return;
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
		
		/*String[] arrayToReturn = new String[abstractPaths.length];
		
		for(int i = 0, j = 0; i < abstractPaths.length && j < arrayToReturn.length; j++)
		{
			if(abstractPaths[j].isFile()) //removes folders from list
			{	
				arrayToReturn[i] = abstractPaths[j].getPath();
				i++;
			}
		}*/
		
		List<String> list = new ArrayList<String>();

	    /*for(String str : arrayToReturn)  // flush null values
	    {
	       if(str != null && s.length() > 0) 
	       {
	          if( (str.substring(str.length()-4,str.length()).equalsIgnoreCase(".mp3")) )
	          	list.add(str);
	       }
	    }
		
		arrayToReturn = list.toArray(new String[list.size()]);*/	
		
		for(File f : abstractPaths)
			pathRecurse(f, list);
		
		//String[] go = new String[list.size()]; 
		//for(int i = 0; i < go.length; i++)
		
			//go[i] = list.get(i);
		
		
		//System.out.println("The size of the list is" + list.size());
		
		String[] go = list.toArray(new String[0]);
			
		//return (String[]) list.toArray();
		
		
		
		//System.out.println("The size of go is" + go.length);
		return go;
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
