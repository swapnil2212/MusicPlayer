package com.example.tatastrive.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.System.exit;

public class SongList extends AppCompatActivity implements AdapterView.OnItemClickListener, MediaController.MediaPlayerControl, SensorEventListener
{
    ListView songListView;
    ArrayList<Song> songList;
    SongAdapter songAdapter;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    private MusicController controller;

    private boolean paused=false, playbackPaused=false;

    private SensorManager sensorManager;
    private long lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        songListView = (ListView)findViewById(R.id.listView);
        songListView.setOnItemClickListener(this);

        songList = new ArrayList<>();

        getSongList();

        songAdapter = new SongAdapter(this,songList);
        songListView.setAdapter(songAdapter);

        setController();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            musicBound = false;
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        if(playIntent==null)
        {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy()
    {
       /* stopService(playIntent);
        musicSrv=null;
        super.onDestroy();*/
        if (musicBound) unbindService(musicConnection);
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    public void getSongList()
    {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst())
        {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }
    public void songPicked(View view)
    {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSongs();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        songPicked(view);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause()
    {
        playbackPaused=true;
        musicSrv.pausePlayer();
        paused = true;

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(paused)
        {
            setController();
            paused=false;
        }
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        controller.hide();
        super.onStop();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getDur();
        else return 0;

    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        {
            return musicSrv.getPosn();
        }

        else return 0;
    }

    @Override
    public void seekTo(int pos)
    {
        musicSrv.seek(pos);

    }

    @Override
    public boolean isPlaying()
    {
        if(musicSrv!=null && musicBound)
        return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage()
    {
        return 0;
    }

    @Override
    public boolean canPause()
    {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController()
    {
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.listView));
        controller.setEnabled(true);
    }

    //play next
    private void playNext()
    {
        musicSrv.playNext();
        if(playbackPaused)
        {
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev()
    {
        musicSrv.playPrev();
        if(playbackPaused)
        {
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        //stopService(playIntent); //use this only if you want stop the player here
        //musicSrv=null; //use this only if you want stop the player here
        this.finish();
        moveTaskToBack(true);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            getAccelerometer(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    private void getAccelerometer(SensorEvent event)
    {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= 5) //
        {
            if (actualTime - lastUpdate < 400)
            {
                return;
            }
            lastUpdate = actualTime;
            //Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT).show();

            playNext();
            musicSrv.playNext();

        }
    }
}



