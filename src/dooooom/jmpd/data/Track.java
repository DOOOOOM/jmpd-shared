package dooooom.jmpd.data;

import java.util.HashMap;
import java.util.Map;

public class Track extends HashMap<String,String> implements Comparable<Track>
{
    public Track(Map<String,String> t) {
        for(Map.Entry<String,String> e : t.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

	public Track(String filepath, String artist, String album, String title)
	{
        this.put("filepath", filepath);
		this.put("artist",artist);
		this.put("album", album);
		this.put("title", title);
	}

    public static String lengthAsString(double seconds) {
        int hours = (int) seconds / 3600;
        int minutes = (int) (seconds % 3600) / 60;

        String response = "";

        if(hours > 0) {
            response += Integer.toString(hours) + ":";
        }
        if(minutes < 10 && hours > 0) {
            response += "0";
        }
        response += Integer.toString(minutes) + ":";
        if(((int) seconds % 60) < 10)
            response += "0";
        response += Integer.toString((int) seconds % 60);

        return response;
    }
	
	public Track()
	{
		this("", "Unknown Artist","Unknown Album","Unknown Title");
	}


    @Override
    public int compareTo(Track o) {
        int artistcompare = this.get("artist").compareToIgnoreCase(o.get("artist"));
        if(artistcompare == 0) {


            try {
                int albumcompare = this.get("album").compareToIgnoreCase(o.get("album"));
                if (albumcompare == 0) {

                    try {
                        int that = Integer.parseInt(this.get("track"));
                        int thing = Integer.parseInt(o.get("track"));
                        int trackcompare = that - thing;
                        if (trackcompare == 0) {
                            return 0;
                        } else if(trackcompare < 0) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } catch(Error e){
                        //track is null or something
                    }

                } else {
                    return albumcompare;
                }
            } catch(Error e){
                //album is null or something
            }


        } else {
            return artistcompare;
        }
        return 0;
    }
}
