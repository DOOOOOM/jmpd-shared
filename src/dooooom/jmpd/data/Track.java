package dooooom.jmpd.data;

import java.util.HashMap;

public class Track extends HashMap<String,String> 
{
	public Track(String artist, String album, String title)
	{
		this.put("artist",artist);
		this.put("album", album);
		this.put("title", title);
	}
	
	public Track()
	{
		this("Unknown Artist","Unknown Album","Unknown Title");
	}
}
