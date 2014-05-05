package scatcat.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import scatcat.effects.Effect;
import scatcat.general.Constants;
import scatcat.graphics.DrawUtils;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.opengl.Matrix;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sneaky.stratagem.graphics.textures.SingleTexturePack;
import com.sneaky.stratagem.units.Unit;

/**
 * Unit of battlefield space that is occupiable by a single unit.
 * A tile should never be constructed directly, but rather the
 * TileFactory class should be used as said class encapsulates all
 * of the construction details for usable Tiles (aka a factory
 * pattern, duh.)
 * 
 * @author R. Matt McCann
 */
public class Tile implements RenderableMVP {
    private List<Object> effects = new ArrayList<Object>();
    
    /** Orientation of the highlight texture. */
    private HighlightOrientation highlightOrientation = HighlightOrientation.NORTH;
    
    private Optional<Integer> highlightTexture = Optional.absent();
    
    /** Whether or not the tile is currently selected. */
    private boolean isSelected = false;
    
    /** Texture used for displaying the selection of the tile. */
    private Optional<Integer> isSelectedTexture = Optional.<Integer>absent();
    
    private Optional<Obstacle> obstacle = Optional.absent();
    
    private final SimpleTexturedShader shader;
    
    /** Texture displayed on on the surface of the tile. */
    private int surfaceTexture;
    
    /** Whether or not to transpose the highlight texture. */
    private boolean transposeHighlight = false;
    
    /** Orientation of the highlight texture. */
    public enum HighlightOrientation {
        EAST,
        NORTH,
        SOUTH,
        WEST
    };
    
    /**
     * Constructor.
     * 
     * @param textureHandle The texture to render with.
     */
    @Inject
    protected Tile(SimpleTexturedShader shader) {
        this.shader = shader;
    }
    
    public synchronized void addEffect(final Object effect) {
        effects.add(effect);
    }
    
    public synchronized List<Object> getEffects() {
        return effects;
    }
    
    public synchronized void removeEffect(final Object effect) {
        effects.remove(effect);
    }
    
    /** {@inheritDoc} */
    @Override
    public synchronized final void render(final MVP mvp) {
        // Render the underlying texture of the tile
        shader.activate();
        shader.setMVPMatrix(mvp.collapse());
        shader.setTexture(surfaceTexture);
        shader.draw();
        
        // If there is a tile highlight that needs to be rendered
        //if (isAttackable || isMovable || isSelected) {
        if (highlightTexture.isPresent()) {
            // Transpose the texture if necessary
            final float[] model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 0.0f, 0.01f);

            // Orient the texture
            switch (highlightOrientation) {
                case NORTH:
                    Matrix.rotateM(model, Constants.NO_OFFSET, -90.0f, 0.0f, 0.0f, 1.0f);
                    break;
                case EAST:
                    Matrix.rotateM(model, Constants.NO_OFFSET, -180.0f, 0.0f, 0.0f, 1.0f);
                    break;
                case SOUTH:
                    Matrix.rotateM(model, Constants.NO_OFFSET, 90.0f, 0.0f, 0.0f, 1.0f);
                    break;
                case WEST:
                    Matrix.rotateM(model, Constants.NO_OFFSET, 0.0f, 0.0f, 0.0f, 1.0f);
                    break;
            }
            
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(highlightTexture.get());
            shader.draw();
        }
        
        // If the tile is selected
        if (isSelected) {
            final float[] model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 0.0f, 0.02f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(isSelectedTexture.get());   
            shader.draw();
        }
        // Otherwise render the team colors
        else if (mOccupant != null && (mOccupant.getOwner() != null)) { //TODO Fix this
            final float[] model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 0.0f, 0.02f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(mOccupant.getOwner().getTeamColorTexture());   
            shader.draw();
        }
        
    }
    
    private Vector<Effect>    mEffects = new Vector<Effect>();
    private float             mElevation;
    private Unit              mOccupant = null;
    private int               mVbo = DrawUtils.getUnitSquarePtVbo();
    public static final float SIZE = 1.0f;
    
    public Unit getOccupant() { return mOccupant; }
    
    public boolean hasObstacle() { return obstacle.isPresent(); }
    
    public boolean hasOccupant() { return (mOccupant != null); }
    public final boolean isSelected() { return isSelected; }

    public final void setHighlightOrientation(final HighlightOrientation highlightOrientation) { 
        this.highlightOrientation = highlightOrientation; }
    public synchronized void setHighlightTexture(final Optional<Integer> highlightTexture) {
        this.highlightTexture = highlightTexture;
    }
    public final void setIsSelected(boolean isSelected) { this.isSelected = isSelected; }
    public synchronized final void setIsSelectedTexture(int isSelectedTexture) { 
        this.isSelectedTexture = Optional.<Integer>of(isSelectedTexture); }
    public synchronized final void setOccupant(Unit unit) { mOccupant = unit; }
    public synchronized final void setSurfaceTexture(final int surfaceTexture) { this.surfaceTexture = surfaceTexture; }
    public final void setTransposeHighlight(boolean transposeHighlight) { 
        this.transposeHighlight = transposeHighlight; }
}
