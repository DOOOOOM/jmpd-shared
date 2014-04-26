package dooooom.jmpd.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;


public class LyricsFetcher
{
	static String baseURL = "http://www.azlyrics.com/lyrics/";
	
	/*
	 * The major method here is fetchLyrics. Other methods are helpers
	 */
	
	public LyricsFetcher()
	{
		
	}
	
	public static String fetchLyrics(String artistName, String songName)
	{
		return fetch(artistName, songName);
	}
	
	private static String constructURL(String artistName, String songName)
	{
		artistName = toAlphabetic(artistName);
		songName = toAlphabetic(songName);
		return baseURL + artistName + "/" + songName + ".html";
	}
	
	private static String toAlphabetic(String str)
	{
		str =  str.replaceAll("[^a-zA-Z0-9]", "");
		//str =  str.replaceAll("[`~]", "$");
		return str.toLowerCase();
	}
	
	private static String fetch(String artistName, String songName)
	{
		byte[] utf8Bytes = null;
		// This array of bytes will store UTF_8 encoded values of each character, for each line
		// we print (html page has utf-8 charset)
		
		String lyricsText = "";	// String to return
		
		try
		{
			URL lyricsPage = new URL(constructURL(artistName, songName));
			
	        BufferedReader in = new BufferedReader(new InputStreamReader(lyricsPage.openStream()));

	        String inputLine;
	        boolean lastLineHasNotBeenPrinted = true, inputHasBegun = false;
	        
	        loop: while ((inputLine = in.readLine()) != null && lastLineHasNotBeenPrinted)
	        {
	            if(inputHasBegun && lastLineHasNotBeenPrinted)
	            {
	            	if(!inputLine.contains("<br />"))
	            		break;
	            	
	            	inputLine = inputLine.replace("<br />", ""); //remove line break tags
	            	inputLine = inputLine.replaceAll("<.{1,3}>", ""); //remove italics tags
	            	inputLine += "\n";            	
	            	utf8Bytes = inputLine.getBytes(); //divide the string obtained bytewise 
	            									  // and store each byte in utf8Bytes
	            	
	            	inputLine = new String(utf8Bytes, StandardCharsets.UTF_8); 
	            	//converts each char(byte) in utf8Bytes from UTF_8 to Unicode (which is the default
	            	// character set) and creates a String out of them 
	            	
	            	lyricsText += inputLine;
	            	//a new line is added to the output
	            	
	            	continue;
	            }
	        	
	        	if(inputLine.contains("<!-- start of lyrics -->"))
	            {
	            	inputHasBegun = true;
	            	continue;
	            }            	
	        }
	        
	        in.close();
		} catch (Exception e)
		{
			return "[lyrics unavailable]";
		}
        
        if (lyricsText.equals(null))
        	lyricsText = "No Lyrics Found.\n"; //Default message
        
        return lyricsText;
	}
}
