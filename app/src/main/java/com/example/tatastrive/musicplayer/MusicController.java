package com.example.tatastrive.musicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by Tata Strive on 10/26/2017.
 */
public class MusicController extends MediaController
{
    Context c;
    public MusicController(Context c)
    {
        super(c);
        this.c = c;
    }
    public void hide(){}

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            ((SongList)c).onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


}


