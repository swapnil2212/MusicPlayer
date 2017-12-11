package com.example.tatastrive.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this,SongList.class);
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(1,1,1,"Song List");
        menu.add(1,1,2,"About");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case 1:
                Toast.makeText(MainActivity.this, "In Song List", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this,SongList.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

}

