package scatcat.map;

import java.util.Iterator;
import java.util.Vector;

import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.huds.UnitHUD;
import com.sneaky.stratagem.units.Unit;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.GridPoint2D;
import scatcat.general.points.Point3D;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.TileSelectionHandler;
import android.opengl.Matrix;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This is the battlefield on which combat takes places. It consists
 * of a collection of tiles, which constitute the basic layout of the
 * battlefield. Additionally, the tiles base layer can be populated
 * with a collection of structures and other features of consequence.
 * Construction of the tiles starts with the bottom left corner.
 * 
 * @author R. Matt McCann
 */
@Singleton
public class Battlefield implements RenderableMVP {
    /** Currently active battlefield action. Given first crack at handling click actions. */
    private Optional<TileSelectionHandler> activeAction = Optional.<TileSelectionHandler>absent();
    
    private final Device device;
    
    /** Base of the island. */
    //private Model islandBase;
    private final int islandBack;
    private final int islandFront;
    
    /** Currently selected (read:focused) tile. */
    private Optional<GridPoint2D> selectedTile = Optional.<GridPoint2D>absent();
    
    private final SimpleTexturedShader shader;
    
    private final TileFactory tileFactory;
    
    /** Used to set the active unit. */
    private final UnitHUD unitHUD;
    
    @Inject
	protected Battlefield(final Device device,
	                      final SimpleTexturedShader shader,
	                      final TextureFactory textureFactory,
	                      final TileFactory tileFactory,
	                      final UnitHUD unitHUD) {
		mLayout = new Vector<Vector<Tile>>();
		
		islandBack = textureFactory.loadTexture(R.drawable.island_back);
		islandFront = textureFactory.loadTexture(R.drawable.island_front);
		
		this.device = device;
		this.shader = shader;
		this.tileFactory = tileFactory;
		this.unitHUD = unitHUD;
	}
    
	/**
	 * Removes the provided unit from the battlefield. //TODO Make faster?
	 * 
	 * @param Unit To be removed.
	 */
	public final void removeUnit(final Unit unit) {
	    for (Vector<Tile> row : mLayout) {
	        for (Tile tile : row) {
	            if (tile.hasOccupant() && (tile.getOccupant() == unit)) {
	                tile.setOccupant(null);
	            }
	        }
	    }
	}
	
	/** {@inheritDoc} */
    @Override
    public void render(MVP mvp) {
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        
        shader.activate();
        float[] temp = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(temp, Constants.NO_OFFSET, -1.1f, -2.6f, -1.0f);
        Matrix.scaleM(temp, Constants.NO_OFFSET, 0.8f, 0.8f, 1.0f);
        Matrix.scaleM(temp, Constants.NO_OFFSET, 2.3f, 1.0f, 1.0f);
        Matrix.scaleM(temp, Constants.NO_OFFSET, 10.0f, 10.0f, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(temp));
        shader.setTexture(islandBack);
        shader.draw();
        
        // Rotate to the viewing angle
        Matrix.rotateM(model, Constants.BEGINNING_OF_BUFFER, X_AXIS_VIEWING_ANGLE, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(model, Constants.BEGINNING_OF_BUFFER, Z_AXIS_VIEWING_ANGLE, 0.0f, 0.0f, 1.0f);
        
        // Render the tiles
        Matrix.translateM(model, 0, Tile.SIZE / 2.0f, Tile.SIZE / 2.0f, 0.1f);
        mvp.push(MVP.Type.MODEL, model);
        for (Iterator<Vector<Tile>> rowIter = mLayout.iterator(); rowIter.hasNext();) {
            Vector<Tile> row = rowIter.next();
            
            // Move to the next row
            Matrix.translateM(model, Constants.BEGINNING_OF_BUFFER, 0.0f, -Tile.SIZE, 0.0f);
            mvp.push(MVP.Type.MODEL, model);
            
            // For each tile in the row
            for (Iterator<Tile> colIter = row.iterator(); colIter.hasNext();) {  
                // Move to the next column
                Matrix.translateM(model, Constants.BEGINNING_OF_BUFFER, -Tile.SIZE, 0.0f, 0.0f);
                mvp.push(MVP.Type.MODEL, model);
                
                // Render the tile
                Tile current = colIter.next();
                current.render(mvp);
        
                model = mvp.pop(MVP.Type.MODEL);
            }
            
            model = mvp.pop(MVP.Type.MODEL);
        }
        model = mvp.pop(MVP.Type.MODEL);
        
        // Render the units
        for (Iterator<Vector<Tile>> rowIter = mLayout.iterator(); rowIter.hasNext();) {
            Vector<Tile> row = rowIter.next();
            
            // Move to the next row
            Matrix.translateM(model, Constants.BEGINNING_OF_BUFFER, 0.0f, -Tile.SIZE, 0.0f);
            mvp.push(MVP.Type.MODEL, model);
            
            // For each tile in the row
            for (Iterator<Tile> colIter = row.iterator(); colIter.hasNext();) {  
                // Move to the next column
                Matrix.translateM(model, Constants.BEGINNING_OF_BUFFER, -Tile.SIZE, 0.0f, 0.0f);
                mvp.push(MVP.Type.MODEL, model);
                
                // If the tile has an occupant
                Tile current = colIter.next();
                synchronized (current) {
                    // Orient the unit so it is standing upright
                    Matrix.rotateM(model, Constants.BEGINNING_OF_BUFFER, -Z_AXIS_VIEWING_ANGLE, 0.0f, 0.0f, 1.0f);
                    Matrix.rotateM(model, Constants.BEGINNING_OF_BUFFER, -X_AXIS_VIEWING_ANGLE, 1.0f, 0.0f, 0.0f);
                    mvp.push(MVP.Type.MODEL, model);

                    // If the tile has an occupant
                    if (current.hasOccupant()) {
                        // Render the unit
                        current.getOccupant().render(mvp);
                    }
                    
                    for (Object effect : current.getEffects()) {
                        if (effect instanceof RenderableMVP) {
                            RenderableMVP renderable = (RenderableMVP) effect;
                            renderable.render(mvp);
                        }
                    }

                    model = mvp.pop(MVP.Type.MODEL);
                }
                
                model = mvp.pop(MVP.Type.MODEL);
            }
            
            model = mvp.pop(MVP.Type.MODEL);
        }
        
        shader.activate();
        temp = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(temp, Constants.NO_OFFSET, -0.639f, -10.36f, 20.0f);
        Matrix.scaleM(temp, Constants.NO_OFFSET, 0.755f, 0.755f, 1.0f);
        Matrix.scaleM(temp, Constants.NO_OFFSET, 2.3f, 1.95f, 1.0f);
        Matrix.scaleM(temp, Constants.NO_OFFSET, 10.0f, 10.0f, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(temp));
        shader.setTexture(islandFront);
        shader.draw();
    }
    
	/**
	 * Adds a tile to the battlefield. The battlefield will automatically resize
	 * to include the specified location
	 */
	public final void addTile(final int rowPos, //!< Row position of tile
			                  final int colPos, //!< Column position of tile
			                  Tile      tile //!< Tile object to be added
			            ) {
		// Add additional rows to the tile container if necessary
		for (int resizeIter = mLayout.size(); resizeIter <= rowPos; resizeIter++) {
			mLayout.add(new Vector<Tile>());
		}

		// Add additional columns to the row if necessary
		for (int resizeIter = mLayout.get(rowPos).size(); resizeIter <= colPos; resizeIter++) {
			mLayout.get(rowPos).add(tileFactory.create(TileFactory.Type.GRASS));
		}

		mLayout.get(rowPos).setElementAt(tile, colPos);
	}

	/**
	 * Adds a unit to the battlefield.
	 * 
	 * @param rowPos Row position of unit stack
	 * @param colPos Column position of unit stack
	 * @param unit Unit stack to be added
	 */
	public final void addUnit(final int rowPos, final int colPos, final Unit unit) {
		mLayout.get(rowPos).get(colPos).setOccupant(unit);
	}
	
	public Vector<Vector<Tile>> getLayout() { return mLayout; }
	
	public final Optional<Tile> getTile(final GridPoint2D tilePoint) {
	    // If the indicated point falls within the realm of the tiles, return it
	    if ((tilePoint.getRow() >= 0) && 
	        (tilePoint.getRow() < mLayout.size()) &&
	        (tilePoint.getColumn() >= 0) && 
	        (tilePoint.getColumn() < mLayout.get(tilePoint.getRow()).size())) {
	        return Optional.<Tile>of(mLayout.get(tilePoint.getRow()).get(tilePoint.getColumn()));
	    } 
	    // The indicated point was outside of the battlefield, so return a null
	    else {
	        return Optional.<Tile>absent();
	    }
	}
	
	/** Move the model position the specified number of rows and columns. */
    public void moveInTileSpace(final float[] model, final float numRows, final float numColumns) {
        checkArgument(model.length == Constants.MATRIX_SIZE, "Model must be float[16]!");
        
        // Rotate to the viewing angle
        Matrix.rotateM(model, Constants.NO_OFFSET, X_AXIS_VIEWING_ANGLE, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(model, Constants.NO_OFFSET, Z_AXIS_VIEWING_ANGLE, 0.0f, 0.0f, 1.0f);
        
        // Move the distance
        Matrix.translateM(model, Constants.NO_OFFSET, -1.0f * numColumns, -1.0f * numRows, 0.0f);
        
        // Unrotate the viewing angle
        Matrix.rotateM(model, Constants.NO_OFFSET, -Z_AXIS_VIEWING_ANGLE, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(model, Constants.NO_OFFSET, -X_AXIS_VIEWING_ANGLE, 1.0f, 0.0f, 0.0f);
    }
    
    /** Move the model position the specified number of rows and columns. */
    public void moveInTileSpace(final Point3D model, final float numRows, final float numColumns) {
        // Put the point into a model matrix
        final float[] modelMatrix = new float[Constants.MATRIX_SIZE];
        Matrix.setIdentityM(modelMatrix, Constants.NO_OFFSET);
        Matrix.translateM(modelMatrix, Constants.NO_OFFSET, model.getX(), model.getY(), model.getZ());
        
        // Apply the movement
        moveInTileSpace(modelMatrix, numRows, numColumns);
        
        model.setX(modelMatrix[3]);
        model.setY(modelMatrix[7]);
        model.setZ(modelMatrix[11]);
    }
    
    public void resetUnits() {
        for (Vector<Tile> row : mLayout) {
            for (Tile tile : row) {
                if (tile.hasOccupant()) {
                    tile.getOccupant().setHasActed(false);
                    tile.getOccupant().setHasMoved(false);
                }
            }
        }
    }
    
    public void rotateToTileSpace(final float[] model) {
        Matrix.rotateM(model, Constants.NO_OFFSET, X_AXIS_VIEWING_ANGLE, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(model, Constants.NO_OFFSET, Z_AXIS_VIEWING_ANGLE, 0.0f, 0.0f, 1.0f);
    }
	
	public boolean pickTile(final GridPoint2D gridLocation) {
	    boolean isValidTile = (gridLocation.getRow() >= 0) && 
	                          (gridLocation.getRow() < mLayout.size()) &&
	                          (gridLocation.getColumn() >= 0) && 
	                          (gridLocation.getColumn() < mLayout.get(gridLocation.getRow()).size());
	    // If the user has clicked on a valid tile
	    if (isValidTile) {
	        // Allow the active action, if there is one, to handle the picking. If it handles the
	        // picking, end the function
	        if (activeAction.isPresent() && activeAction.get().handleTileSelection(gridLocation)) {
	            return selectedTile.isPresent();
	        }
	        
	        Tile pickedTile = mLayout.get(gridLocation.getRow()).get(gridLocation.getColumn());
	        
	        // If the user has clicked the currently selected tile for a second time, deselect it
	        if (selectedTile.isPresent() && (selectedTile.get().equals(gridLocation))) {

	        }
	        // Otherwise this is the first time the tile has been clicked, so select it
	        else {
	            // De-select the currently selected tile
	            if (selectedTile.isPresent()) {
	                mLayout.get(selectedTile.get().getRow())
	                       .get(selectedTile.get().getColumn()).setIsSelected(false);
	            }
	            
	            // Select the picked tile
	            pickedTile.setIsSelected(true);
	            selectedTile = Optional.<GridPoint2D>of(gridLocation);
	            
	            // Update the HUD if the tile has an occupant
	            if (pickedTile.hasOccupant()) {
	                unitHUD.setActiveUnit(pickedTile.getOccupant());
	            }
	            // The tile doesn't have an occupant so turn off the HUD
	            else {
	                unitHUD.setActiveUnit(null);
	            }
	        }
	        
            return true;
	    } 
	    // The user has clicked off the battlefield so deselect the currently selected tile
	    else if (selectedTile.isPresent()) {
	        mLayout.get(selectedTile.get().getRow())
	               .get(selectedTile.get().getColumn()).setIsSelected(false);
	        selectedTile = Optional.<GridPoint2D>absent();
	        unitHUD.setActiveUnit(null);
	    }
	    
	    return false;
	}
	
	public final Optional<TileSelectionHandler> getActiveAction() { return activeAction; }
	public final Optional<GridPoint2D> getSelectedTile() { return selectedTile; }
	
	public final void setActiveAction(final Optional<TileSelectionHandler> activeAction) { 
	    this.activeAction = activeAction; }
	//public final void setIslandBase(final Model islandBase) { this.islandBase = islandBase; }
	public final void setSelectedTile(final Optional<GridPoint2D> selectedTile) {
	    this.selectedTile = selectedTile;
	}
	
	private Vector<Vector<Tile>> mLayout; //!< Container of tiles that makes up the field
	public static final float    X_AXIS_VIEWING_ANGLE = -50.0f; //!< The angle at which the field is viewed, X-axis 
	public static final float    Z_AXIS_VIEWING_ANGLE = 45.0f; //!< The angle at which the field is viewed, Z-axis
}
