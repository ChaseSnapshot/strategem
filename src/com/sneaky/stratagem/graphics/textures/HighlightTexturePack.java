package com.sneaky.stratagem.graphics.textures;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import scatcat.general.Constants;
import scatcat.general.points.GridPoint2D;
import scatcat.map.Battlefield;
import scatcat.map.Tile;
import scatcat.map.Tile.HighlightOrientation;
import android.opengl.GLES20;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

/**
 * Texture package for the highlighting of tiles threatened by an action.
 * 
 * @author R. Matt McCann
 */
public class HighlightTexturePack {
    /** Texture that represents a highlight square with two sides exposed to non-highlighted areas. */
    private final int bridgeTexture;
    
    /** Texture that represents a highlight square completely surrounded by other highlighted areas. */
    protected final int centerTexture;
    
    /** Texture that represents a highlight square with two adjacent sides exposed to non-highlighted areas. */
    protected final int cornerTexture;
    
    /** Texture that represents a highlight square with adjacency except two consecutive sides and the opposite corner. */
    protected final int cornerMissing1CornerTexture;
    
    /** Used for avoid circular dependencies. */
    private final Injector injector;
    
    /** Texture that represents a lone highlighted square. */
    protected final int islandTexture;
    
    /** Texture that represents a highlight square with total adjacency except for one corner. */
    protected final int missing1CornerTexture;
    
    /** Texture that represents a highlight square with total adjacency except for two adjacent corners. */
    protected final int missing2CornersTexture;
    
    /** Texture that represents a highlight square with total adjacency except for two opposite corners. */
    protected final int missing2CornersOppositeTexture;
    
    /** Texture that represents a highlight square with total adjacency except for three corners. */
    protected final int missing3CornersTexture;
    
    /** Texture that represents a highlight square with all sides adjacent but no corners. */
    protected final int missing4CornersTexture;
    
    /** Texture that represents a highlight square with three sides exposed to non-highlighted areas. */
    protected final int peninsulaTexture;
    
    /** Texture that represents a highlight square with total adjacency except one side. */
    protected final int shoreTexture;
    
    /** Texture that represents a highlight square with adjacency except one side and a corner. */
    protected final int shoreMissing1CornerTexture;
    
    /** Texture that represents a higlight square with adjacency except one side and two corners. */
    protected final int shoreMissing2CornersTexture;

    public HighlightTexturePack(final Injector injector,
                                final int bridgeTexture,
                                final int centerTexture,
                                final int cornerTexture,
                                final int cornerMissing1CornerTexture,
                                final int islandTexture,
                                final int missing1CornerTexture,
                                final int missing2CornersTexture,
                                final int missing2CornersOppositeTexture,
                                final int missing3CornersTexture,
                                final int missing4CornersTexture,
                                final int peninsulaTexture,
                                final int shoreTexture,
                                final int shoreMissing1CornerTexture,
                                final int shoreMissing2CornersTexture) { 
        this.injector = injector;
        this.bridgeTexture = bridgeTexture;
        this.centerTexture = centerTexture;
        this.cornerTexture = cornerTexture;
        this.cornerMissing1CornerTexture = cornerMissing1CornerTexture;
        this.islandTexture = islandTexture;
        this.missing1CornerTexture = missing1CornerTexture;
        this.missing2CornersTexture = missing2CornersTexture;
        this.missing2CornersOppositeTexture = missing2CornersOppositeTexture;
        this.missing3CornersTexture = missing3CornersTexture;
        this.missing4CornersTexture = missing4CornersTexture;
        this.peninsulaTexture = peninsulaTexture;
        this.shoreTexture = shoreTexture;
        this.shoreMissing1CornerTexture = shoreMissing1CornerTexture;
        this.shoreMissing2CornersTexture = shoreMissing2CornersTexture;
    }
    
    /** Releases the textures. */
    public final void cleanUp() {
        final int numTextures = 14;
        
        final int[] toBeDeleted = new int[numTextures];
        toBeDeleted[0] = bridgeTexture;
        toBeDeleted[1] = centerTexture;
        toBeDeleted[2] = cornerTexture;
        toBeDeleted[3] = cornerMissing1CornerTexture;
        toBeDeleted[4] = islandTexture;
        toBeDeleted[5] = missing1CornerTexture;
        toBeDeleted[6] = missing2CornersTexture;
        toBeDeleted[7] = missing2CornersOppositeTexture;
        toBeDeleted[8] = missing3CornersTexture;
        toBeDeleted[9] = missing4CornersTexture;
        toBeDeleted[10] = peninsulaTexture;
        toBeDeleted[11] = shoreTexture;
        toBeDeleted[12] = shoreMissing1CornerTexture;
        toBeDeleted[13] = shoreMissing2CornersTexture;
        
        GLES20.glDeleteTextures(numTextures, toBeDeleted, Constants.NO_OFFSET);
    }
    
    /**
     * Picks the appropriate highlight texture for the tile given the surrounding highlighted textures.
     * 
     * @param target Tile to be highlighted.
     * @param highlighted Other tiles that will be highlighted.
     */
    public final void setTexture(final GridPoint2D target, final Set<GridPoint2D> highlighted) {
        // Retrieve the tile
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        Optional<Tile> refTile = battlefield.getTile(target);
        if (!refTile.isPresent()) {
            return;
        }
        Tile tile = refTile.get();
        
        // Get the adjacent tiles
        GridPoint2D topLeft = new GridPoint2D(target.getRow() - 1, target.getColumn() - 1);
        GridPoint2D topCenter = new GridPoint2D(target.getRow(), target.getColumn() - 1);
        GridPoint2D topRight = new GridPoint2D(target.getRow() + 1, target.getColumn() - 1);
        GridPoint2D midLeft = new GridPoint2D(target.getRow() - 1, target.getColumn());
        GridPoint2D midRight = new GridPoint2D(target.getRow() + 1, target.getColumn());
        GridPoint2D bottomLeft = new GridPoint2D(target.getRow() - 1,target.getColumn() + 1);
        GridPoint2D bottomCenter = new GridPoint2D(target.getRow(), target.getColumn() + 1);
        GridPoint2D bottomRight = new GridPoint2D(target.getRow() + 1, target.getColumn() + 1);
        Set<GridPoint2D> all = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter, topRight, 
                midLeft, midRight, bottomLeft, bottomCenter, bottomRight));
        
        // If the tile is surrounded on all sides and corners [Center]
        if (highlighted.containsAll(Arrays.asList(all.toArray()))) {
            tile.setHighlightTexture(Optional.of(centerTexture));
            return;
        }
        
        // If the tile is completely surrounded except one side [North-Facing Shore]
        Set<GridPoint2D> contains = new HashSet<GridPoint2D>(Arrays.asList(midLeft,                  midRight,
                                                                            bottomLeft, bottomCenter, bottomRight));
        Set<GridPoint2D> missing = new HashSet<GridPoint2D>(Arrays.asList(topCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile is completely surrounded except one side [East-Facing Shore]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft,    topCenter, 
                                                           midLeft, 
                                                           bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile is completely surrounded except one side [South-Facing Shore]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter, topRight,
                                                           midLeft,            midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile is completely surrounded except on side [West-Facing Shore]
        contains = new HashSet<GridPoint2D>(Arrays.asList(      topCenter,    topRight,
                                                                               midRight,
                                                                 bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
        
        // If the tile is surrounded by two sides [North-East-Facing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(midLeft, 
                                                           bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topCenter, midRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile is surrounded by two sides [East-South-Facing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter,
                                                           midLeft));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midRight, bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile is surrounded by two sides [South-West-Facing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topCenter, topRight,
                                                                      midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(bottomCenter, midLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile is surrounded by two sides [West-North-Facing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(              midRight,
                                                           bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midLeft, topCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
        
        // If the tile is surrounded on opposite sides [North-South Bridge]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topCenter, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midLeft, midRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(bridgeTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile is surrounded on opposite sides [East-West Bridge]
        contains = new HashSet<GridPoint2D>(Arrays.asList(midLeft, midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topCenter, bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(bridgeTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has one adjacent side [North Facing Peninsula]
        contains = new HashSet<GridPoint2D>(Arrays.asList(bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(        topCenter,
                                                         midLeft,           midRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(peninsulaTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile has one adjacent side [East Facing Peninsula]
        contains = new HashSet<GridPoint2D>(Arrays.asList(midLeft));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topCenter,
                                                                     midRight,
                                                         bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(peninsulaTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has one adjacent side [South Facing Peninsula]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midLeft,            midRight,
                                                                 bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(peninsulaTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile has one adjacent side [West Facing Peninsula]
        contains = new HashSet<GridPoint2D>(Arrays.asList(midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(        topCenter,
                                                         midLeft,
                                                                 bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(peninsulaTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
        
        // If the tile has no adjacent highlighted tiles [Island]
        missing = new HashSet<GridPoint2D>(Arrays.asList(            topCenter,
                                                         midLeft,                  midRight,
                                                                     bottomCenter));
        if (Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(islandTexture));
            return;
        }
        
        // If the tile has total adjacency except for one corner [North-East Missing One Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft,    topCenter, 
                                                          midLeft,                  midRight,
                                                          bottomLeft, bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile has total adjacency except for one corner [East-South Missing One Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft,    topCenter,    topRight,
                                                          midLeft,                  midRight,
                                                          bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has total adjacency except for one corner [South-West Missing One Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter,    topRight,
                                                          midLeft,               midRight,
                                                                   bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile has total adjacency except for one corner [West-North Missing One Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(            topCenter,    topRight,
                                                          midLeft,               midRight,
                                                          bottomLeft, bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
        
        // If the tile has total adjacency except for two adjacent corners [North Missing Two Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(            topCenter,
                                                          midLeft,                  midRight,
                                                          bottomLeft, bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile has total adjacency except for two adjacent corners [East Missing Two Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft,    topCenter,
                                                          midLeft,                  midRight,
                                                          bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topRight, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has total adjacency except for two adjacent corners [South Missing Two Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter,   topRight,
                                                          midLeft,              midRight,
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(bottomLeft, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile has total adjacency except for two adjacent corners [West Missing Two Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,    topRight,
                                                          midLeft,               midRight,
                                                                   bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
        
        // If the tile has total adjacency except for two opposite corners [North-East-South-West Missing Two Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter,
                                                          midLeft,               midRight,
                                                                   bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topRight, bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing2CornersOppositeTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile has total adjacency except for two opposite corners [North-West-South-East Missing Two Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(            topCenter,    topRight,
                                                          midLeft,                  midRight,
                                                          bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing2CornersOppositeTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has total adjacency except for three corners [North-East Missing Three Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,   topRight,
                                                          midLeft,              midRight,
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, bottomLeft, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing3CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile has total adjacency except for three corners [East-South Missing Three Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,
                                                          midLeft,               midRight,
                                                                   bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topRight, bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing3CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has total adjacency except for three corners [South-West Missing Three Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(            topCenter,
                                                          midLeft,                 midRight,
                                                          bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topRight, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing3CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile has total adjacency except for three corners [West-North Missing Three Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter,
                                                          midLeft,              midRight,
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topRight, bottomLeft, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing3CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
        
        // If the tile has all sides adjacent but no corners [Missing Four Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,
                                                          midLeft,              midRight,
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft,               topRight,
                 
                                                         bottomLeft,            bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(missing4CornersTexture));
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [North Untransposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(midLeft,                 midRight,
                                                          bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topCenter, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            tile.setTransposeHighlight(false);
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [North Transposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(midLeft,               midRight,
                                                                   bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topCenter, bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            tile.setTransposeHighlight(true);
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [East Untransposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter,
                                                          midLeft,               
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midRight, bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            tile.setTransposeHighlight(false);
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [East Transposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(            topCenter,
                                                          midLeft,               
                                                          bottomLeft, bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, midRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            tile.setTransposeHighlight(true);
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [South Untransposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(        topCenter, topRight,
                                                          midLeft,           midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            tile.setTransposeHighlight(false);
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [South Transposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topLeft, topCenter,
                                                          midLeft,           midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topRight, bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            tile.setTransposeHighlight(true);
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [West Untransposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topCenter,
                                                                        midRight,
                                                          bottomCenter, bottomRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topRight, midLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            tile.setTransposeHighlight(false);
            return;
        }
        
        // If the tile has adjacencies except a side and one corner [West Transposed Shore Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(topCenter,   topRight,
                                                                       midRight,
                                                          bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(midLeft, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            tile.setTransposeHighlight(true);
            return;
        }
        
        // If the tile has adjacencies except a side and two corner [North Shore Missing 2 Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(
                                                          midLeft,              midRight,
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topCenter, bottomLeft, bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile has adjacencies except a side and two corner [East Shore Missing 2 Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,
                                                          midLeft,             
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft, midRight, bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has adjacencies except a side and two corner [South Shore Missing 2 Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,
                                                          midLeft,              midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft,               topRight, 
                
                                                                  bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile has adjacencies except a side and two corner [West Shore Missing 2 Corners]
        contains = new HashSet<GridPoint2D>(Arrays.asList(        topCenter,
                                                                               midRight,
                                                                  bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(                      topRight, 
                                                          midLeft,
                                                                               bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(shoreMissing2CornersTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
        
        // If the tile has adjacencies except two consecutive sides and the opposite corner [North Corner Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,
                                                                             midRight));
        missing = new HashSet<GridPoint2D>(Arrays.asList(                    topRight,
                                                           midLeft,
                                                                   bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.NORTH);
            return;
        }
        
        // If the tile has adjacencies except two consecutive sides and the opposite corner [East Corner Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(                      midRight,
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(          topCenter,
                                                         midLeft,
                                                                                bottomRight));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.EAST);
            return;
        }
        
        // If the tile has adjacencies except two consecutive sides and the opposite corner [South Corner Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(midLeft,
                                                                   bottomCenter));
        missing = new HashSet<GridPoint2D>(Arrays.asList(          topCenter,
                                                                              midRight,
                                                         bottomLeft));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.SOUTH);
            return;
        }
        
        // If the tile has adjacencies except two consecutive sides and the opposite corner [West Corner Missing Corner]
        contains = new HashSet<GridPoint2D>(Arrays.asList(         topCenter,
                                                          midLeft));
        missing = new HashSet<GridPoint2D>(Arrays.asList(topLeft,
                                                                             midRight,
                                                                 bottomCenter));
        if (highlighted.containsAll(Arrays.asList(contains.toArray())) &&
            Sets.difference(all, highlighted).containsAll(Arrays.asList(missing.toArray()))) {
            tile.setHighlightTexture(Optional.of(cornerMissing1CornerTexture));
            tile.setHighlightOrientation(HighlightOrientation.WEST);
            return;
        }
    }
}



















