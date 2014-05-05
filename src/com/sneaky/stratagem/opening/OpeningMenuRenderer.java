package com.sneaky.stratagem.opening;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.ScreenPoint2D;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.InputHelper;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.StratagemRenderer2D;
import com.sneaky.stratagem.armyselection.ArmySelectionModule;
import com.sneaky.stratagem.armyselection.ArmySelectionRenderer;
import com.sneaky.stratagem.graphics.widgets.Button;
import com.sneaky.stratagem.graphics.widgets.Button.ButtonFactory;
import com.sneaky.stratagem.proxy.ProxyRenderer;
import com.sneaky.stratagem.units.UnitModule;

public class OpeningMenuRenderer extends StratagemRenderer2D {
    /** The module used to create the renderer. */
    private final OpeningMenuModule dependencyModule;
    
    /** "Exit" Button. */
    private final Button exitButton;
    private final ScreenPoint2D exitButtonPos;
    
    private final Injector injector;
    
    /** "Load Game" Button. */
    private final Button loadGameButton;
    private final ScreenPoint2D loadGameButtonPos;
    
    /** "New Game" Button. */
    private final Button newGameButton;
    private final ScreenPoint2D newGameButtonPos;
    
    /** "Options" Button. */
    private final Button optionsButton;
    private final ScreenPoint2D optionsButtonPos;
    
    /** Rendering container used for switching to the next renderer. */
    private final ProxyRenderer proxyRenderer;
    
    /** Used for rendering the scene. */
    private final SimpleTexturedShader shader;
    
    /** Splash Image Settings. */
    private final float splashHeight = 1.3f;
    private final int   splashTexture;
    private final float splashWidth;
    
    /** Guice injection constructor. */
    @Inject
    protected OpeningMenuRenderer(@Named("ButtonBackgroundTexture") final int buttonBackgroundTexture,
                                  final ButtonFactory buttonFactory,
                                  final OpeningMenuModule dependencyModule,
                                  final Device device,
                                  final Injector injector,
                                  final ProxyRenderer proxyRenderer,
                                  final SimpleTexturedShader shader,
                                  @Named("SplashTexture") final int splashTexture) {
        super(injector);
        
        this.dependencyModule = dependencyModule;
        this.injector = injector;
        this.proxyRenderer = proxyRenderer;
        this.shader = shader;
        this.splashTexture = splashTexture;
        this.splashWidth = splashHeight / device.getAspectRatio();
        
        // Construct the "New Game" button
        final float  height = 0.15f;
        final float  textHeightRatio = 0.6f;
        final float  spacing = 0.05f;
        final float  width = 0.2f;
        float xPos = -0.5f + 0.025f + width / 2.0f;
        newGameButton = buttonFactory.create(buttonBackgroundTexture, height, "New Game", textHeightRatio, width);
        newGameButtonPos = new ScreenPoint2D(xPos, -0.475f + height / 2.0f);
        
        // Construct the "Load Game" button
        xPos += spacing + width;
        loadGameButton = buttonFactory.create(buttonBackgroundTexture, height, "Load Game", textHeightRatio, width);
        loadGameButtonPos = new ScreenPoint2D(xPos, -0.475f + height / 2.0f);
        
        // Construct the "Options" button
        xPos += spacing + width;
        optionsButton = buttonFactory.create(buttonBackgroundTexture, height, "Options", textHeightRatio, width);
        optionsButtonPos = new ScreenPoint2D(xPos, -0.475f + height / 2.0f);
        
        // Construct the "Exit" button
        xPos += spacing + width;
        exitButton = buttonFactory.create(buttonBackgroundTexture, height, "Exit", textHeightRatio, width);
        exitButtonPos = new ScreenPoint2D(xPos, -0.475f + height / 2.0f);
        
        // Set the background color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /** Clean up allocated resources. */
    @Override
    public void close() {
        super.close();
        
        dependencyModule.close(injector);
    }
    
    /** {@inheritDoc} */
    @Override
    public void drawFrame(MVP mvp) {
        super.drawFrame(mvp);
        
        shader.activate();
        
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.055f, 0.2f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, splashWidth, splashHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(splashTexture);
        shader.draw();
        
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, newGameButtonPos.getX(), newGameButtonPos.getY(), 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        newGameButton.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, loadGameButtonPos.getX(), loadGameButtonPos.getY(), 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        loadGameButton.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, optionsButtonPos.getX(), optionsButtonPos.getY(), 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        optionsButton.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, exitButtonPos.getX(), exitButtonPos.getY(), 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        exitButton.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {
        if (InputHelper.isTouched(newGameButtonPos, newGameButton.getWidth(), 
                                  newGameButton.getHeight(), clickLocation)) {
            proxyRenderer.getView().queueEvent(new Runnable() {
                @Override
                public void run() {
                    final Injector injector = Guice.createInjector(new ArmySelectionModule(proxyRenderer),
                            new UnitModule(proxyRenderer));
                    
                    ArmySelectionRenderer renderer = injector.getInstance(ArmySelectionRenderer.class);
                    proxyRenderer.setRenderer(renderer);
                }
            });
            
            return true;
        }
        
        if (InputHelper.isTouched(exitButtonPos, exitButton.getWidth(), exitButton.getHeight(), clickLocation)) {
            injector.getInstance(Activity.class).finish();
        }
        
        return false;
    }

    @Override
    public boolean handleLongPress(NormalizedPoint2D pressLocation) { return false; }

    @Override
    public boolean handleZoom(float zoomFactor) { return false; }

    @Override
    public boolean handlePickUp(NormalizedPoint2D touchLocation) { return false; }

    @Override
    public boolean handleDrag(NormalizedPoint2D moveVector) { return false; }

    @Override
    public boolean handleDrop(NormalizedPoint2D dropLocation) { return false; }
}
