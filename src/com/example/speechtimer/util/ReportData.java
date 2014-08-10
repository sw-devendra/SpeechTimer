package com.example.speechtimer.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Scanner;


import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;



public class ReportData extends ArrayAdapter<SpeakerEntry>{

	public ReportData(Context con, int resource)
	{
		super(con, resource);
        try 
        {
        	File file = new File(con.getFilesDir(), SpeakerEntry.REPORT_FILE);
        	if(file.exists()) 
        	{         	
                Scanner s = new Scanner(file);
                while (s.hasNextLine())
                {              	
                	super.add(SpeakerEntry.fromFileLine(s.nextLine()));
                }
        	}
        } 
        catch (Exception e) 
        {
            // Ignore if could not read file
        }
	}
/*	
	public void commiteData(Context con)
	{
    	File file = new File(con.getFilesDir() + "/" + SpeakerEntry.REPORT_FILE);
    	
    	if(file.exists()) 
    	{
        	long l = file.lastModified();
        	Date now = new Date();
        	
        	if (now.getTime() - l > MAX_MEET_DURATION)
        	{
	        	try 
	        	{
		            FileWriter fw = new FileWriter(file, now.getTime() - l < MAX_MEET_DURATION) ;
		            PrintWriter pw = new PrintWriter(fw);
		            
		            for (int i=0; i< super.getCount();i++)
		            {
		            	pw.println(super.getItem(i).toFileLine()); 	
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
	*/

}
