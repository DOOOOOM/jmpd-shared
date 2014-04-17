package dooooom.jmpd;

import java.io.IOException;

import com.mpatric.mp3agic.*; 

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
	
	static String extractArtistFrom(String filePath)
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
			}
			
			else if (mp3File.hasId3v1Tag())
			{
				ID3v1 id3v1Tag = mp3File.getId3v1Tag();
				artistName = id3v1Tag.getArtist();
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
		
		try
		{
			if(artistName.equals(null))
				artistName = "Unknown Artist";
		} catch (Exception e) 
		  {artistName = "Unknown Artist";
			//e.printStackTrace();
		  }
		
		return artistName;
	}


	static String extractTitleFrom(String filePath)
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
		
		try
		{
			if(title.equals(null))
				title = "Unknown Title";
		} catch (Exception e) 
		  {title = "Unknown Title";
			//e.printStackTrace();
		  }
		
		return title;
	}
	

	static String extractAlbumFrom(String filePath)
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
}
