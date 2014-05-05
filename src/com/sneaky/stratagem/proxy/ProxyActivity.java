package com.sneaky.stratagem.proxy;

import com.sneaky.stratagem.music.BackgroundMusic;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class ProxyActivity extends Activity {
    private BackgroundMusic backgroundMusic;
    
    /** Handles proxying the renderers. */
    private ProxyView view;
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the program to full screen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        ProxyRenderer renderer = new ProxyRenderer(this);
        view = new ProxyView(this, renderer);
        
        renderer.setView(view);
        setContentView(view);
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onPause() {
        super.onPause();
        backgroundMusic.cancel(true);
        view.onPause();
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onResume() {
        super.onResume();
        backgroundMusic = new BackgroundMusic(this);
        backgroundMusic.execute(new Void[]{});
        view.onResume();
    }
}
