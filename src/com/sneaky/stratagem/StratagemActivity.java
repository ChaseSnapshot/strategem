package com.sneaky.stratagem;

import com.google.inject.Injector;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import static com.google.common.base.Preconditions.checkArgument;

public class StratagemActivity extends Activity {
    /** Injector used for handling dependency injection by this activity. */
    private Injector injector;
    
    /** Surface view. */
    private GLSurfaceView view;
    
    public final Context getContext() { return this.getApplicationContext(); }
    
    public final Injector getInjector() { return injector; }
    
    public final GLSurfaceView getView() { return view; }
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the program to full screen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onPause() {
        super.onPause();
        view.onPause();
    }
    
    /** {@inheritDoc} */
    @Override
    public final void onResume() {
        super.onResume();
        view.onResume();
    }
    
    public final void setInjector(final Injector injector) {
        checkArgument(injector != null, "Injector must not be null!");
        this.injector = injector;
    }
    
    public final void setView(final GLSurfaceView view) {
        checkArgument(view != null, "Surface view must not be null!");
        this.view = view;
        setContentView(view);
    }
}
