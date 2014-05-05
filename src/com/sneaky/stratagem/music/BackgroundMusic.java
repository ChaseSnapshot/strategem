package com.sneaky.stratagem.music;

import com.sneaky.stratagem.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;

public class BackgroundMusic extends AsyncTask<Void, Void, Void> {
    private final Activity activity;
    
    public BackgroundMusic(final Activity activity) {
        this.activity = activity;
    }
    
    @Override
    protected Void doInBackground(Void... params) {
        MediaPlayer player = MediaPlayer.create(activity, R.raw.test_soundtrack);
        player.setLooping(true);
        player.setVolume(100, 100);
        player.start();
        
        return null;
    }
}
