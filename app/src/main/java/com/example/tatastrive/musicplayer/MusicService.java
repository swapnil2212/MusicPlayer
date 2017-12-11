package com.example.tatastrive.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Tata Strive on 10/25/2017.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();

    private String songTitle="";
    private static final int NOTIFY_ID=1;
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        //player.stop();
        //player.release();
        return super.onUnbind(intent);
    }

    public void playSongs()
    {
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        songTitle=playSong.getTitle();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try
        {
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e)
        {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }
    public void initMusicPlayer()
    {
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
    }
    public void setList(ArrayList<Song> theSongs)
    {
        songs=theSongs;
    }

    public void setSong(int songIndex)
    {
        songPosn=songIndex;
    }



    public class MusicBinder extends Binder
    {
        MusicService getService()
        {
            return MusicService.this;
        }
    }
    @Override
    public void onCompletion(MediaPlayer mp)
    {
        if(player.getCurrentPosition()==0)
        {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        mp.start();
        Intent notIntent = new Intent(this, SongList.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);


        builder.setContentIntent(pendInt);
        builder.setSmallIcon(R.drawable.play1);
        //builder.setOngoing(true);
        builder.setContentTitle("Playing");
        builder.setContentText(songTitle);


        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification not = builder.build();
        manager.notify(NOTIFY_ID,not);

            //startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //player.stop();
        //player.release();
        //stopForeground(true);

    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn==0) songPosn=songs.size()-1;
        playSongs();
    }

    //skip to next
    public void playNext(){
        songPosn++;
        if(songPosn==songs.size()) songPosn=0;
        playSongs();
    }


}
