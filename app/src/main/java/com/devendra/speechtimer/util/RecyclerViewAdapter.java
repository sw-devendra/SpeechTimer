package com.devendra.speechtimer.util;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devendra.speechtimer.R;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private ArrayList<SpeakerEntry> data;
    private boolean changed;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        RelativeLayout relativeLayout;

        public MyViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.txtTitle);
        }
    }

    public RecyclerViewAdapter(Context con) {
        this.data = new ArrayList<>();

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
                        this.data.add(SpeakerEntry.fromFileLine(s.nextLine()));
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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
        // ToDo: Find best way to show color itemView.findViewById(R.id.relativeLayout).setBackgroundColor(Color.parseColor("#7694A1"));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position).toString());
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        changed = true;
    }

    public void restoreItem(SpeakerEntry item, int position) {
        data.add(position, item);
        notifyItemInserted(position);
        changed = true;
    }

    public ArrayList<SpeakerEntry> getData() {
        return data;
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

                SpeakerEntry se;

                for(int i=0; i<data.size(); i++)
                {
                    se = data.get(i);
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

