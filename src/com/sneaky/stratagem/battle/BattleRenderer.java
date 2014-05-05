package com.sneaky.stratagem.battle;

import javax.microedition.khronos.opengles.GL10;

import scatcat.exceptions.UnprojectionException;
import scatcat.general.Constants;
import scatcat.general.points.GridPoint2D;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.Point2D;
import scatcat.general.points.Point3D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.BattlefieldFactory;
import scatcat.map.BattlefieldFactory.Type;
import scatcat.map.Tile;

import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sneaky.stratagem.StratagemRenderer3D;
import com.sneaky.stratagem.huds.ArmyPlacementHUD;
import com.sneaky.stratagem.huds.GameHUD;
import com.sneaky.stratagem.huds.UnitHUD;
import com.sneaky.stratagem.map.background.Background;
import com.sneaky.stratagem.map.background.BackgroundFactory;
import com.sneaky.stratagem.map.background.BackgroundFactory.WeatherType;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;
import com.sneaky.stratagem.units.UnitModule;

public class BattleRenderer extends StratagemRenderer3D {
    /** Heads up display that shows the available units to place on the battlefield. */
    private final ArmyPlacementHUD armyPlacementHUD;
    
    /** Background of the battlefield. */
    private final Background background;
    
    /** Map where the battle takes place. */
    private final Battlefield battlefield;
    
    /** Where the camera is positioned. */
    private final Point3D camera = new Point3D(-8.33341275e-4f, 0.20702237f, 0.75328046f);
    
    /** How zoomed in or out the camera is. */
    private float cameraZoom = 0.073679f;
    
    /** Heads up display that displays game details. */
    private final GameHUD gameHUD;
    
    private final Injector injector;
    
    /** Whether or not the armies are being placed. */
    private boolean isPlacing = true;
    
    /** Heads up display that displays unit details. */
    private final UnitHUD unitHUD;
    
    @Inject
    protected BattleRenderer(final ArmyPlacementHUD armyPlacementHUD,
                             final BackgroundFactory backgroundFactory,
                             final BattlefieldFactory battlefieldFactory,
                             final GameHUD gameHUD,
                             final Injector injector,
                             final UnitHUD unitHUD) {
        super(injector);
        
        this.armyPlacementHUD = armyPlacementHUD;
        this.background = backgroundFactory.create(WeatherType.FAIR_WEATHER);
        this.battlefield = battlefieldFactory.create(Type.PROTOTYPE);
        this.gameHUD = gameHUD;
        this.injector = injector;
        this.unitHUD = unitHUD;
        
        initMatch();
    }
    
    public void applyWorldView(final float[] model) {
        Matrix.translateM(model, Constants.NO_OFFSET, camera.getX(), camera.getY(), camera.getZ());
        Matrix.scaleM(model, Constants.NO_OFFSET, cameraZoom, cameraZoom, 1.0f);
    }
    
    @Override
    public void close() {
        super.close();
        
        BattleModule battleModule = injector.getInstance(BattleModule.class);
        battleModule.close(injector);
        
        UnitModule unitModule = injector.getInstance(UnitModule.class);
        unitModule.close(injector);
    }
    
    public GridPoint2D convert(final NormalizedPoint2D toBeConverted) throws UnprojectionException {
        checkNotNull(toBeConverted);
        
        final Point2D screenPoint = new Point2D(toBeConverted.getX() + 0.5f, toBeConverted.getY() + 0.5f);
        final float[] touchPoint = new float[4];
        final int[]   viewport   = new int[]{0, 0, 1, 1};
        final float[] model      = new float[Constants.MATRIX_SIZE];
        Matrix.setIdentityM(model, Constants.NO_OFFSET);
        applyWorldView(model);
        
        // Calculate the touch point
        int nearResult = GLU.gluUnProject(screenPoint.getX(), screenPoint.getY(), -1.0f, 
                model, Constants.NO_OFFSET, getProjectionMatrix(), Constants.NO_OFFSET, viewport,
                Constants.NO_OFFSET, touchPoint, Constants.NO_OFFSET);
        if (nearResult != GL10.GL_TRUE) {
            throw new UnprojectionException();
        }
        
        // Orient the touch point into the battlefield
        final float[] tile = new float[2];
        final float x = touchPoint[0];
        float y = touchPoint[1] / (float) Math.cos(Battlefield.X_AXIS_VIEWING_ANGLE * Constants.DEG_2_RAD);
        float angle = -Battlefield.Z_AXIS_VIEWING_ANGLE * Constants.DEG_2_RAD;
        tile[0] = x * (float) Math.cos(angle) - y * (float) Math.sin(angle);
        tile[1] = x * (float) Math.sin(angle) + y * (float) Math.cos(angle);
        
        // Select the unit
        return new GridPoint2D((int) -tile[1], (int) -tile[0]);
    }
    
    @Override
    public void drawFrame(final MVP mvp) {
        super.drawFrame(mvp);
        
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        
        background.render(mvp);
        
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        applyWorldView(model);
        mvp.push(MVP.Type.MODEL, model);
        battlefield.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        if (isPlacing) {
            armyPlacementHUD.render();
        } else {
            unitHUD.render();
            gameHUD.render();
        }
    }
    
    public float getCameraZoom() { return cameraZoom; }
    
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {
        try {
            if (isPlacing && armyPlacementHUD.handleClick(clickLocation)) { return true; }
            if (unitHUD.handleClick(clickLocation)) { return true; }
            if (gameHUD.handleClick(clickLocation)) { return true; }
            
            battlefield.pickTile(convert(clickLocation));
            return true;
        } catch (UnprojectionException ex) {
            return false;
        }
    }
    
    @Override
    public boolean handleDrag(NormalizedPoint2D moveVector) {
        if (isPlacing && armyPlacementHUD.handleDrag(moveVector)) { return true; }
        
        final float panSensitivity = 1.0f;
        final float x = moveVector.getX() * panSensitivity;
        final float y = moveVector.getY() * panSensitivity;
        final float z = moveVector.getY() * panSensitivity * 
                        (float) Math.tan(Battlefield.X_AXIS_VIEWING_ANGLE * Constants.DEG_2_RAD);
        
        camera.addToX(x);
        camera.addToY(y);
        camera.addToZ(z);
        
        return true;
    }
    
    @Override
    public boolean handleDrop(NormalizedPoint2D dropLocation) {
        if (isPlacing && armyPlacementHUD.handleDrop(dropLocation)) { return true; }
        
        return false;
    }
    
    @Override
    public boolean handleLongPress(NormalizedPoint2D pressLocation) {
        try {
            final Match match = injector.getInstance(Match.class);
            final GridPoint2D pressedArea = convert(pressLocation);
            
            // If the pressed tile is not part of the battlefield
            final Optional<Tile> pressedTile = battlefield.getTile(pressedArea);
            if (!pressedTile.isPresent()) { return false; }
            
            // If there is no unit on the pressed tile
            final Unit pressedUnit = pressedTile.get().getOccupant();
            if (pressedUnit == null) { return false; }
            
            if (!battlefield.pickTile(pressedArea)) { return false; }
            
            unitHUD.setActiveUnit(pressedUnit);
            if (pressedUnit.getOwner() == match.getCurrentPlayer()) {
                unitHUD.setActingUnit(pressedUnit);
            }
            
            return true;
        } catch (UnprojectionException ex) {
            return false;
        }        
    }

    @Override
    public boolean handlePickUp(NormalizedPoint2D touchLocation) {
        if (isPlacing && armyPlacementHUD.handlePickUp(touchLocation)) { return true; }
        
        return true;
    }

    @Override
    public boolean handleZoom(float zoomFactor) {
        cameraZoom *= zoomFactor;
        
        return true;
    }
    
    private void initMatch() {
        Match match = injector.getInstance(Match.class);
        
        match.getPlayer(0).setTeamColorTexture(injector.getInstance(Key.get(Integer.class, 
                Names.named("RedTeamOverlay"))));
    }
    
    public void setIsPlacing(final boolean isPlacing) { this.isPlacing = isPlacing; }
}
