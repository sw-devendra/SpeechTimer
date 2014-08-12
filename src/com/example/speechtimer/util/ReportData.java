package com.example.speechtimer.util;

import java.io.File;
import java.util.Date;
import java.util.Scanner;


import android.content.Context;
import android.widget.ArrayAdapter;



public class ReportData extends ArrayAdapter<SpeakerEntry>{

	public ReportData(Context con, int resource)
	{
		super(con, resource);
        try 
        {
        	File file = new File(con.getFilesDir(), SpeakerEntry.REPORT_FILE);
        	if(file.exists()) 
        	{
            	long l = file.lastModified();
            	Date now = new Date();
            	
            	// If the file was created during current meeting
            	if ((now.getTime() - l < SpeakerEntry.MAX_MEET_DURATION))
            	{
	                Scanner s = new Scanner(file);
	                while (s.hasNextLine())
	                {              	
	                	super.add(SpeakerEntry.fromFileLine(s.nextLine()));
	                }
	                s.close();
            	}
        	}
        } 
        catch (Exception e) 
        {
            // Ignore if could not read file
        }
	}

}
