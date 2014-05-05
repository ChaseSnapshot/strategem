package com.sneaky.stratagem.graphics.widgets;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import android.opengl.GLES20;
import android.opengl.Matrix;
import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.graphics.Color;
import scatcat.graphics.DrawUtils;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleColorShader;

/**
 * A filled bar class that provides a bar display that can
 * be filled from not at all to completely.
 * @author R. Matt McCann
 *
 */
public class StatsBar implements RenderableMVP {
    private final float backgroundEdgeHeight;
    private final int   backgroundEdgeVBO;
    private final float backgroundEdgeWidth;
    
    private final float backgroundMainHeight;
    private final int   backgroundMainVBO;
    private final float backgroundMainWidth;
    
    private float currentStat = 1.0f;
    
    private final float foregroundEdgeHeight;
    private final int   foregroundEdgeVBO;
    private final float foregroundEdgeWidth;
    
    private final float foregroundMainHeight;
    private final int   foregroundMainVBO;
    private final float foregroundMainWidth;
    
    private float maxStat = 1.0f;
    
    private final SimpleColorShader shader;
    
    /**
     * Constructor. Established the rendering properties based off the
     * provided parameters. Percent filled can be updated at any time.
     * 
     * @param height Height of the bar.
     * @param width Width of the bar.
     * @param foregroundColor Foreground color of the bar.
     * @param backgroundColor Background color of the bar.
     */
    @Inject
    protected StatsBar(final SimpleColorShader shader,
                       final Device device,
                       @Assisted("Height") final float height, 
                       @Assisted("Width") final float width,
                       @Assisted("ForegroundColor") final Color foregroundColor,
                       @Assisted("BackgroundColor") final Color backgroundColor) {
        this.shader = shader;
        
        backgroundEdgeHeight = height;
        backgroundEdgeVBO = DrawUtils.buildUnitCirclePcVbo(backgroundColor);
        backgroundEdgeWidth = backgroundEdgeHeight / device.getAspectRatio();
        
        backgroundMainHeight = height;
        backgroundMainVBO = DrawUtils.buildUnitSquarePcVbo(backgroundColor);
        backgroundMainWidth = width;
        
        final float foreToBackRatio = 0.8f;
        
        foregroundEdgeHeight = height * foreToBackRatio;
        foregroundEdgeVBO = DrawUtils.buildUnitCirclePcVbo(foregroundColor);
        foregroundEdgeWidth = foregroundEdgeHeight / device.getAspectRatio();
        
        foregroundMainHeight = height * foreToBackRatio;
        foregroundMainVBO = DrawUtils.buildUnitSquarePcVbo(foregroundColor);
        foregroundMainWidth = width;
    }
    
    public interface StatsBarFactory {
        StatsBar create(@Assisted("Height") final float height, 
                        @Assisted("Width") final float width,
                        @Assisted("ForegroundColor") final Color foregroundColor,
                        @Assisted("BackgroundColor") final Color backgroundColor);
    }

    public float getBackgroundEdgeWidth() { return backgroundEdgeWidth; }
    
    /**
     * Draws the stats bar.
     * 
     * @param mvpMatrix Current model-view-projection matrix.
     */
    @Override
    public void render(final MVP mvp) {
        shader.activate();
 
        // Render the main segment of the background panel
        float[] modelViewMatrix = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, backgroundMainWidth, 
                backgroundMainHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(modelViewMatrix));
        shader.setVBO(backgroundMainVBO);
        shader.draw();
        
        // Render the left rounded edge of the background panel
        modelViewMatrix = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, -backgroundMainWidth / 2.0f, 
                0.0f, 0.0f);
        Matrix.scaleM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, backgroundEdgeWidth, 
                backgroundEdgeHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(modelViewMatrix));
        shader.setVBO(backgroundEdgeVBO);
        shader.draw(GLES20.GL_TRIANGLE_FAN, DrawUtils.NUM_CIRCLE_VERTICES);
        
        // Render the right rounded edge of the background panel
        modelViewMatrix = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, backgroundMainWidth / 2.0f, 
                0.0f, 0.0f);
        Matrix.scaleM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, backgroundEdgeWidth, 
                backgroundEdgeHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(modelViewMatrix));
        shader.setVBO(backgroundEdgeVBO);
        shader.draw(GLES20.GL_TRIANGLE_FAN, DrawUtils.NUM_CIRCLE_VERTICES);
        
        // Render the foreground center panel
        modelViewMatrix = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, 
                (currentStat / maxStat - 1.0f) * foregroundMainWidth / 2.0f, 0.0f, 0.0f);
        if (maxStat > 0.0f) {
            Matrix.scaleM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, 
                    currentStat / maxStat * foregroundMainWidth, foregroundMainHeight, 1.0f);
        } else {
            Matrix.scaleM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, foregroundMainWidth, 
                    foregroundMainHeight, 1.0f);
        }
        shader.setMVPMatrix(mvp.collapseM(modelViewMatrix));
        shader.setVBO(foregroundMainVBO);
        shader.draw();
       
        // Render the left edge of the foreground
        modelViewMatrix = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, -foregroundMainWidth / 2.0f,
                0.0f, 0.0f);
        Matrix.scaleM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, 
                foregroundEdgeWidth, foregroundEdgeHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(modelViewMatrix));
        shader.setVBO(foregroundEdgeVBO);
        shader.draw(GLES20.GL_TRIANGLE_FAN, DrawUtils.NUM_CIRCLE_VERTICES);
        
        // Render the right edge of the background
        modelViewMatrix = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, 
                foregroundMainWidth / 2.0f - (1.0f - currentStat / maxStat) * foregroundMainWidth, 0.0f, 0.0f);
        Matrix.scaleM(modelViewMatrix, Constants.BEGINNING_OF_BUFFER, 
                foregroundEdgeWidth, foregroundEdgeHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(modelViewMatrix));
        shader.setVBO(foregroundEdgeVBO);
        shader.draw(GLES20.GL_TRIANGLE_FAN, DrawUtils.NUM_CIRCLE_VERTICES);
    }
    
    public final void setCurrentStat(final float stat) {
        currentStat = stat;
    }
    
    public final void setMaxStat(final float stat) {
        maxStat = stat;
    }
}
