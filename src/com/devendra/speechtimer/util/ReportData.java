package com.devendra.speechtimer.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Scanner;


import android.content.Context;
import android.widget.ArrayAdapter;



public class ReportData extends ArrayAdapter<SpeakerEntry>{

	private boolean changed = false;
	public ReportData(Context con, int resource, int textViewResourceId)
	{
		super(con, resource,textViewResourceId);
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
	
	
	public void setChanged()
	{
		changed = true;
	}
	
	public void commitChanges(Context con)
	{
		if (changed)
		{
			File file = new File(con.getFilesDir(), SpeakerEntry.REPORT_FILE);
	    	
	    	try 
	    	{
	            FileWriter fw = new FileWriter(file, false);
	            PrintWriter pw = new PrintWriter(fw);
	            
	            SpeakerEntry se = new SpeakerEntry();
	            
	            for(int i=0; i<getCount(); i++)
	            {
	            	se = getItem(i);
		            pw.println(se.toFileLine()); 
	            }
	            pw.close();
	            fw.close();
	    	}
	    	catch(Exception e)
	    	{
	    		// Ignore if file could not be write
	    	}	
		}
	}

}
