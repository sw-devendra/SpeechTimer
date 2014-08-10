package com.example.speechtimer.util;

public class SpeakerEntry
{
	public static final String REPORT_FILE="speechtimer.data";
	public static final long MAX_MEET_DURATION = 180*60*1000 ; // minute*seconds*Ms
	public String type;
	public String name;
	public String duration;
	
	public String toString()
	{
		return new String(type + " " + name + "= " + duration);
	}
	
	public String toFileLine()
	{
		return type + "," + name + "," + duration;
	}
	
	public static SpeakerEntry fromFileLine(String s)
	{
    	String[] items = s.split(",");
    	SpeakerEntry se = new SpeakerEntry();
    	if (items.length >= 3) // May not be true in case of file curruption
    	{
	    	se.type = items[0];
	    	se.name = items[1];
	    	se.duration = items[2];
    	}   	
    	return se;
	}
}
