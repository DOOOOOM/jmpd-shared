package dooooom.jmpd.data;

import com.mpatric.mp3agic.*;

import java.io.IOException;

/**
 * REQUIRES THE com PACKAGE IN our src FOLDER (https://github.com/mpatric/mp3agic/tree/master/src/main/java)
 * 
 * This class will carry out operations to extract diverse information from
 * the ID3 tag of a specified mp3 file. It will use functionalities from
 * the mp3agic package.
 *  
 * */


public class MetadataExtractor 
{
	public MetadataExtractor()
	{
		
	}
	
	public static String extractArtistFrom(String filePath)
	/*
	 * The extractArtistFrom method checks the ID3 version of the mp3 file and 
	 * extracts the artist's name from the MP3 file.
	 */
	{
		String artistName = null;
		
		try 
		{
			Mp3File mp3File = new Mp3File(filePath);



			if (mp3File.hasId3v2Tag())
			{
				ID3v2 id3v2Tag = mp3File.getId3v2Tag();
				artistName = id3v2Tag.getArtist();
			} else if (mp3File.hasId3v1Tag()) {
				ID3v1 id3v1Tag = mp3File.getId3v1Tag();
				artistName = id3v1Tag.getArtist();
			} else {
                System.err.print("[DEBUG]   No ID3 tags found for " + filePath + ": ");
            }
			
		} catch (UnsupportedTagException e) 
		  {
			e.printStackTrace();
		  } 
		  catch (InvalidDataException e) 
		  {
			  System.out.print("Invalid Data");
			  return " - Unknown Artist";
			  //e.printStackTrace();
		  } 
		  catch (IOException e) 
		  {			
			e.printStackTrace();
		  }

		if(artistName == null)
			artistName = "Unknown Artist";
		
		return artistName;
	}


	public static String extractTitleFrom(String filePath)
	/*
	 *  The extractNameFrom method checks the ID3 version of the mp3 file and 
	 * extracts the song title from the MP3 file.
	 */{
		String title = null;
		try 
		{
			Mp3File mp3File = new Mp3File(filePath);
			if (mp3File.hasId3v2Tag())
			{
				ID3v2 id3v2Tag = mp3File.getId3v2Tag();
				title = id3v2Tag.getTitle();
			}
			
			else if (mp3File.hasId3v1Tag())
			{
				ID3v1 id3v1Tag = mp3File.getId3v1Tag();
				title = id3v1Tag.getTitle();
			}			
			
		} catch (UnsupportedTagException e) 
		  {
			  e.printStackTrace();
		  } 
		  catch (InvalidDataException e) 
		  {
			  System.out.print("Invalid Data");
			  return " - Unknown Title";
			  //e.printStackTrace();
		  } 
		  catch (IOException e) 
		  {
			
			  e.printStackTrace();
		  }



		if(title == null) {
            int lastSlash = filePath.lastIndexOf('\\');
            if(filePath.lastIndexOf('/') > lastSlash)
                lastSlash = filePath.lastIndexOf('/');

            title = filePath.substring(lastSlash + 1).replaceAll("\\.mp3$","");
        }



		
		return title;
	}
	

	public static String extractAlbumFrom(String filePath)
	/*
	 * The extractAlbumFrom method checks the ID3 version of the mp3 file and 
	 * extracts the artist's name from the MP3 file.
	 */
	{
		String albumName = null;
		
		try 
		{
			Mp3File mp3File = new Mp3File(filePath);
			if (mp3File.hasId3v2Tag())
			{
				ID3v2 id3v2Tag = mp3File.getId3v2Tag();
				albumName = id3v2Tag.getAlbum();
			}
			
			else if (mp3File.hasId3v1Tag())
			{
				ID3v1 id3v1Tag = mp3File.getId3v1Tag();
				albumName = id3v1Tag.getAlbum();
			}			
			
		} catch (UnsupportedTagException e) 
		  {
			   e.printStackTrace();
		  } 
		  catch (InvalidDataException e) 
		  {
			System.out.print("Invalid Data");
			return " - Unknown Album";
			//e.printStackTrace();
		  } 
		  catch (IOException e) 
		  {
			   e.printStackTrace();
		  }
		try
		{
			if(albumName.equals(null))
				albumName = "Unknown Album";
		} catch (Exception e) 
		  {albumName = "Unknown Album";
			//e.printStackTrace();
		  }
		
		return albumName;
	}
	
	public static Double extractLengthFrom(String filePath)
	/*
	 * The extractLengthFrom method checks the length of the track
	 * and returns a double that represents the length
	 */
	{
		double trackLength = 0.0;
		
		Mp3File mp3File;
		try 
		{
			mp3File = new Mp3File(filePath);
			trackLength = mp3File.getLengthInSeconds();	
		} catch (UnsupportedTagException e) 
		  {
//			e.printStackTrace();
		  } 
		  catch (InvalidDataException e) 
		  {
//			e.printStackTrace();
		  } 
		  catch (IOException e) 
		  {
//			e.printStackTrace();
		  }
		
		 if (trackLength < 0)
			 trackLength = 0.0;		
			
				
		return trackLength;
	}
	
	public static String extractTrackFrom(String filePath)
	/*
	 * The extractTrackFrom method checks the ID3 version of the mp3 file and 
	 * extracts the track number from the MP3 file.
	 */
	{
		String track = null;
		
		try 
		{
			Mp3File mp3File = new Mp3File(filePath);
			if (mp3File.hasId3v2Tag())
			{
				ID3v2 id3v2Tag = mp3File.getId3v2Tag();
				track = id3v2Tag.getTrack();
			}
			
			else if (mp3File.hasId3v1Tag())
			{
				ID3v1 id3v1Tag = mp3File.getId3v1Tag();
				track = id3v1Tag.getTrack();
			}			
			
		} catch (UnsupportedTagException e) 
		  {
			   e.printStackTrace();
		  } 
		  catch (InvalidDataException e) 
		  {
			System.out.println("Invalid Data");
			return " - Unknown Track Number";
			//e.printStackTrace();
		  } 
		  catch (IOException e) 
		  {
			   e.printStackTrace();
		  }
		try
		{
			if(track.equals(null))
				track = "Unknown Track number";
		} catch (Exception e) 
		  {track = "Unknown Track number";
			//e.printStackTrace();
		  }
		
		return track;
	}
}
