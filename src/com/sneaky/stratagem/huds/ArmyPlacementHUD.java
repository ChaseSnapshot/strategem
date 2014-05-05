package com.sneaky.stratagem.huds;

import java.util.List;
import java.util.Map.Entry;

import android.graphics.Paint.Align;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.battle.BattleRenderer;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.match.Army;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

import scatcat.exceptions.UnprojectionException;
import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.GridPoint2D;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.InputHelper;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

public class ArmyPlacementHUD extends HUD {
    private final float arrowHeight = 0.05f;
    private final float arrowSpacing = 0.005f;
    private final int   arrowTexture;
    private final float arrowWidth;
    
    private final int backgroundOrbTexture;
    
    private final float commanderBackgroundHeight = 0.25f;
    private final float commanderBackgroundWidth;
    private boolean isCommanderPickedUp = false;
    private boolean isCommanderPlaced = false;
    
    private int currentPlayer = 0;
    
    private int currentUnitPage = 0;
    
    private final Device device;
    
    private Optional<Tile> droppedOnTile = Optional.<Tile>absent();
    
    private final Injector injector;
    
    private final Match match;
    
    private float playerNameHeight = 0.05f;
    private int   playerNameTexture;
    private float playerNameWidth;
    
    private Optional<NormalizedPoint2D> pickedUpPos = Optional.<NormalizedPoint2D>absent();
    private Optional<Unit> pickedUpUnit = Optional.<Unit>absent();
    
    private final float       readyOrbHeight = 0.1f;
    private final int         readyText;
    private final float[]     readyTextAspectRatio = new float[1];
    private final float       readyOrbWidth;
    
    private final SimpleTexturedShader shader;
    
    private final float unitBackgroundHeight = 0.20f;
    private final float unitBackgroundSpacing = 0.01f;
    private final float unitBackgroundWidth;
    
    private final int unitsPerPage = 4;
    
    @Inject
    protected ArmyPlacementHUD(@Named("SimpleArrowTexture") final int arrowTexture,
                               @Named("BackgroundOrbTexture") final int backgroundOrbTexture,
                               final Device device,
                               final Injector injector,
                               final Match match,
                               final SimpleTexturedShader shader,
                               final TextureFactory textureFactory) {
        this.arrowTexture = arrowTexture;
        this.arrowWidth = arrowHeight / device.getAspectRatio(); 
        this.backgroundOrbTexture = backgroundOrbTexture;
        this.commanderBackgroundWidth = commanderBackgroundHeight / device.getAspectRatio();
        this.device = device;
        this.injector = injector;
        this.match = match;
        this.shader = shader;
        this.unitBackgroundWidth = unitBackgroundHeight / device.getAspectRatio();
        this.readyOrbWidth = this.readyOrbHeight / device.getAspectRatio();
        this.readyText = textureFactory.texturizeText("READY", Color.WHITE, Align.CENTER, 60.0f, readyTextAspectRatio);
        
        final float[] aspectRatio = new float[1];
        playerNameTexture = textureFactory.texturizeText(match.getCurrentPlayer().getName() + " - Place Your Army", 
                Color.WHITE, Align.RIGHT, 60.0f, aspectRatio);
        playerNameWidth = playerNameHeight * aspectRatio[0] / device.getAspectRatio();
    }
    
    @Override
    public synchronized boolean handleClick(NormalizedPoint2D clickLocation) {
        final List<Entry<Unit, Integer>> currentArmy = match.getArmy(currentPlayer).getSortedMinions();
        
        // Check if the left page arrow was clicked
        final Point2D leftArrowPosition = new Point2D();
        leftArrowPosition.setX(-0.5f + commanderBackgroundWidth + arrowSpacing + arrowWidth / 2.0f);
        leftArrowPosition.setY(0.5f - unitBackgroundHeight / 2.0f);
        if (InputHelper.isTouched(leftArrowPosition, arrowWidth, arrowHeight, clickLocation) &&
            (currentUnitPage != 0)) {
            currentUnitPage -= unitsPerPage;
            return true;
        }

        // Check if the right page arrow was clicked
        final Point2D rightArrowPosition = new Point2D(leftArrowPosition);
        rightArrowPosition.addToX(arrowSpacing * 2.0f + arrowWidth);
        rightArrowPosition.addToX(unitBackgroundWidth * unitsPerPage);
        rightArrowPosition.addToX(unitBackgroundSpacing * (unitsPerPage - 1));
        if (InputHelper.isTouched(rightArrowPosition, arrowWidth, arrowHeight, clickLocation) &&
            (currentUnitPage + unitsPerPage < currentArmy.size())) {
            currentUnitPage += unitsPerPage;
            return true;
        }
        
        // Check if the ready orb was clicked
        final Point2D readyOrbPosition = new Point2D();
        readyOrbPosition.setX(-0.5f + readyOrbWidth / 2.0f);
        readyOrbPosition.setY(0.5f - commanderBackgroundHeight - readyOrbHeight / 2.0f);
        if (InputHelper.isTouched(readyOrbPosition, readyOrbWidth, readyOrbHeight, clickLocation)) {
            Match match = injector.getInstance(Match.class);
            BattleRenderer renderer = injector.getInstance(BattleRenderer.class);
            
            currentPlayer++;
            currentUnitPage = 0;
            isCommanderPlaced = false;
            if (currentPlayer == match.getNumPlayers()) {
                renderer.setIsPlacing(false);
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public synchronized boolean handleDrag(NormalizedPoint2D moveVector) {
        if (pickedUpUnit.isPresent()) {
            Battlefield battlefield = injector.getInstance(Battlefield.class);
            BattleRenderer converter = injector.getInstance(BattleRenderer.class);
            NormalizedPoint2D position = pickedUpPos.get();
            
            position.addToX(moveVector.getX());
            position.addToY(moveVector.getY());
            
            try {
                GridPoint2D gridPoint = converter.convert(position);
                Optional<Tile> tile = battlefield.getTile(gridPoint);
                if (tile.isPresent()) {
                    if (droppedOnTile.isPresent()) {
                        droppedOnTile.get().setOccupant(null);
                    }
                    
                    if (!tile.get().hasOccupant()) {
                        tile.get().setOccupant(pickedUpUnit.get());
                        droppedOnTile = Optional.<Tile>of(tile.get());
                    }
                } else if (droppedOnTile.isPresent()) {
                    droppedOnTile.get().setOccupant(null);
                    droppedOnTile = Optional.<Tile>absent();
                }
 
                return true;
            } catch (UnprojectionException ex) {
                
            }
                
            return true;
        }
        
        return false;
    }
    
    @Override
    public synchronized boolean handleDrop(NormalizedPoint2D dropLocation) {
        if (pickedUpUnit.isPresent()) {
            if (droppedOnTile.isPresent()) {
                // Clone the dropped unit
                Tile tile = droppedOnTile.get();
                tile.setOccupant(pickedUpUnit.get().clone());
                
                if (isCommanderPickedUp) {
                    isCommanderPlaced = true;
                }
                
                droppedOnTile = Optional.<Tile>absent();
            } else if (!isCommanderPickedUp) {
                Army army = match.getArmy(currentPlayer);
                int numMinion = army.getMinions().get(pickedUpUnit.get());
                army.getMinions().put(pickedUpUnit.get(), numMinion + 1);
            }
            
            pickedUpPos = Optional.<NormalizedPoint2D>absent();
            pickedUpUnit = Optional.absent();
            isCommanderPickedUp = false;
        
            return true;
        }
        
        return false;
    }
    
    @Override
    public synchronized boolean handlePickUp(NormalizedPoint2D touchLocation) {
        final List<Entry<Unit, Integer>> currentArmy = match.getArmy(currentPlayer).getSortedMinions();
        
        // Check the commander is picked up
        final NormalizedPoint2D commanderPos = new NormalizedPoint2D();
        commanderPos.setX(-0.5f + commanderBackgroundWidth / 2.0f);
        commanderPos.setY(0.5f - commanderBackgroundHeight / 2.0f);
        if (InputHelper.isTouched(commanderPos, commanderBackgroundWidth, commanderBackgroundHeight, 
                touchLocation) && !isCommanderPlaced) {
            pickedUpPos = Optional.<NormalizedPoint2D>of(commanderPos);
            pickedUpUnit = Optional.<Unit>of(match.getArmy(currentPlayer).getCommander());
            isCommanderPickedUp = true;
        }
        
        // Determine the starting position of the non-commander unit orbs
        final NormalizedPoint2D position = new NormalizedPoint2D();
        position.addToX(-0.5f + commanderBackgroundWidth + arrowWidth + arrowSpacing * 2);
        position.addToX(unitBackgroundWidth / 2.0f);
        position.addToY(0.5f - unitBackgroundHeight / 2.0f);
        
        for (int pageIter = 0; pageIter < unitsPerPage; pageIter++) {
            final int unitPos = currentUnitPage + pageIter;
            
            if (InputHelper.isTouched(position, unitBackgroundWidth, unitBackgroundHeight, touchLocation)
                && (unitPos < currentArmy.size()) && (currentArmy.get(unitPos).getValue() > 0)) {
                Entry<Unit, Integer> entry = currentArmy.get(unitPos);
                
                // Pick up the selected unit
                pickedUpPos = Optional.<NormalizedPoint2D>of(position);
                pickedUpUnit = Optional.<Unit>of(entry.getKey());
                
                entry.setValue(entry.getValue() - 1);
                
                return true;
            }
            
            position.addToX(unitBackgroundSpacing + unitBackgroundWidth);
        }

        return false;
    }
    
    @Override
    public synchronized void render() {
        // Disable depth testing for the flat "on-screen" HUD
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        shader.activate();
        
        final MVP mvp  = new MVP();
        mvp.push(MVP.Type.PROJECTION, getProjection());
        
        // Render the player's name
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.5f - playerNameWidth / 2.0f - 0.025f / device.getAspectRatio(), 
                -0.475f + playerNameHeight / 2.0f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, playerNameWidth, playerNameHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(playerNameTexture);
        shader.draw();
        
        // Render the commander
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, -0.5f + commanderBackgroundWidth / 2.0f, 
                0.5f - commanderBackgroundHeight / 2.0f, 1.0f);
        mvp.push(MVP.Type.MODEL, model);
        renderCommander(mvp);
        model = mvp.pop(MVP.Type.MODEL);
        
        Matrix.translateM(model, Constants.NO_OFFSET, (commanderBackgroundWidth + arrowWidth) / 2.0f 
                + arrowSpacing, (commanderBackgroundHeight - unitBackgroundHeight) / 2.0f, 0.0f);
        
        if (currentUnitPage != 0) {
            mvp.push(MVP.Type.MODEL, model);
            renderArrow(mvp, -90.0f);
            model = mvp.pop(MVP.Type.MODEL);
        }
        
        Matrix.translateM(model, Constants.NO_OFFSET, arrowWidth / 2.0f + arrowSpacing, 0.0f, 0.0f);
        
        final List<Entry<Unit, Integer>> currentArmy = match.getArmy(currentPlayer).getSortedMinions();
        for (int pageIter = 0; pageIter < unitsPerPage; pageIter++) {
            final int minionPos = currentUnitPage + pageIter;
            Optional<Entry<Unit, Integer>> minionEntry = Optional.<Entry<Unit, Integer>>absent();
            if (minionPos < currentArmy.size()) {
                Unit minion = currentArmy.get(minionPos).getKey();
                
                if (!pickedUpUnit.isPresent() || (pickedUpUnit.get() != minion)) {
                    minionEntry = Optional.<Entry<Unit, Integer>>of(currentArmy.get(minionPos));
                }
            }
            
            if (pageIter != 0) { 
                Matrix.translateM(model, Constants.NO_OFFSET, unitBackgroundSpacing, 0.0f, 0.0f); 
            }
            Matrix.translateM(model, Constants.NO_OFFSET, unitBackgroundWidth / 2.0f, 0.0f, 0.0f);
            
            mvp.push(MVP.Type.MODEL, model);
            renderMinion(mvp, minionEntry);
            model = mvp.pop(MVP.Type.MODEL);
            
            Matrix.translateM(model, Constants.NO_OFFSET, unitBackgroundWidth / 2.0f, 0.0f, 0.0f);
        }
        
        if (currentUnitPage + unitsPerPage < currentArmy.size()) {
            Matrix.translateM(model, Constants.NO_OFFSET, arrowWidth / 2.0f + arrowSpacing, 0.0f, 0.0f);
            mvp.push(MVP.Type.MODEL, model);
            renderArrow(mvp, 90.0f);
            model = mvp.pop(MVP.Type.MODEL);
        }
        
        if (pickedUpUnit.isPresent()) {
            renderPickedUpUnit(mvp);
        }
        
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, -0.5f + readyOrbWidth / 2.0f, 0.5f - 
                commanderBackgroundHeight - readyOrbHeight / 2.0f, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        renderReadyOrb(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Disable depth testing for the flat "on-screen" HUD
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    
    private void renderArrow(final MVP mvp, final float angle) {
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, arrowWidth, arrowHeight, 1.0f);
        Matrix.rotateM(model, Constants.NO_OFFSET, angle, 0.0f, 0.0f, -1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(arrowTexture);
        shader.draw();
    }
    
    private void renderCommander(final MVP mvp) {       
        // Render the background orb of the commander
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, commanderBackgroundWidth, commanderBackgroundHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(backgroundOrbTexture);
        shader.draw();
       
        if (!isCommanderPickedUp && !isCommanderPlaced) {
            // Render the commander
            final float foregroundRatio = 0.8f;
            Matrix.scaleM(model, Constants.NO_OFFSET, foregroundRatio, foregroundRatio, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(match.getArmy(currentPlayer).getCommander().getProfileTexture());
            shader.draw();
        }
    }
    
    private void renderMinion(final MVP mvp, final Optional<Entry<Unit, Integer>> minion) {
        // Render the background orb of the unit
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, unitBackgroundWidth, unitBackgroundHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(backgroundOrbTexture);
        shader.draw();
        
        // Render the unit
        if (minion.isPresent()) {
            final float foregroundRatio = 0.8f;
            Matrix.scaleM(model, Constants.NO_OFFSET, foregroundRatio, foregroundRatio, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(minion.get().getKey().getProfileTexture());
            shader.draw();
            
            final float[] aspectRatio = new float[1];
            final int numAvailableTexture = injector.getInstance(TextureFactory.class)
                    .texturizeText(Integer.toString(minion.get().getValue()), Color.WHITE, 
                    Align.CENTER, 60.0f, aspectRatio);
            
            final float numAvailableHeight = 0.05f;
            final float xTranslate = (unitBackgroundWidth - numAvailableHeight * aspectRatio[0]) / 2.0f;
            final float yTranslate = (unitBackgroundHeight - numAvailableHeight) / 2.0f;
            model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, xTranslate, yTranslate, 0.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, numAvailableHeight * aspectRatio[0],
                    numAvailableHeight, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(numAvailableTexture);
            shader.draw();
        }
    }
    
    private void renderPickedUpUnit(final MVP mvp) {
        if (!droppedOnTile.isPresent()) {
            final float[] model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, pickedUpPos.get().getX(), pickedUpPos.get().getY(), 0.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, unitBackgroundWidth, unitBackgroundHeight, 1.0f);
            
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(pickedUpUnit.get().getProfileTexture());
            shader.draw();
        }
    }
    
    private void renderReadyOrb(final MVP mvp) {
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, readyOrbWidth, readyOrbHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(backgroundOrbTexture);
        shader.draw();
        
        final float readyTextHeight = 0.35f;
        Matrix.scaleM(model, Constants.NO_OFFSET, readyTextHeight * readyTextAspectRatio[0], readyTextHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(readyText);
        shader.draw();
    }

    @Override
    public boolean handleLongPress(NormalizedPoint2D pressLocation) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean handleZoom(float zoomFactor) {
        // TODO Auto-generated method stub
        return false;
    }
}
