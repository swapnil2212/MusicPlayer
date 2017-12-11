package com.example.tatastrive.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Tata Strive on 10/25/2017.
 */
public class SongAdapter extends BaseAdapter
{
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

   /* public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(c);
    }*/
    public SongAdapter(Context c,ArrayList<Song> theSongs)
    {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }
    @Override
    public int getCount()
    {
        return songs.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //map to song layout
        LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.song_row, parent, false);
        //get title and artist views
        TextView songView = (TextView)songLay.findViewById(R.id.songNameTextView);
        TextView artistView = (TextView)songLay.findViewById(R.id.artistNameTextView);
        //get song using position
        Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }


}
