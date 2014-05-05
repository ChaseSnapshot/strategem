package com.sneaky.stratagem;

import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.flow.GameFlowControllerImpl;
import com.sneaky.stratagem.proxy.ProxyActivity;
import com.sneaky.stratagem.proxy.ProxyRenderer;
import com.sneaky.stratagem.proxy.ProxyView;

/**
 * Base Guice dependency module.
 * 
 * @author R. Matt McCann
 */
public class StratagemModule extends AbstractModule {
    private final ProxyRenderer renderer;
    
    public StratagemModule(final ProxyRenderer renderer) {
        this.renderer = renderer;
    }
    
    @Override
    protected void configure() {
        bind(Activity.class).toInstance(renderer.getActivity());
        bind(Context.class).toInstance(renderer.getContext());
        bind(GameFlowController.class).to(GameFlowControllerImpl.class).in(Singleton.class);
        bind(ProxyActivity.class).toInstance(renderer.getActivity());
        bind(ProxyRenderer.class).toInstance(renderer);
        bind(ProxyView.class).toInstance(renderer.getView());
        bind(Renderer.class).toInstance(renderer);
        bind(GLSurfaceView.class).toInstance(renderer.getView());
        
        install(new FactoryModuleBuilder().build(GlyphStringFactory.class));
    }
    
    protected ProxyView getView() { return renderer.getView(); }
}
