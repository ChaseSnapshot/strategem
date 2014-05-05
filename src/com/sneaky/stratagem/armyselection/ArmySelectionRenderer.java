package com.sneaky.stratagem.armyselection;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.InputHelper;
import android.graphics.Paint.Align;
import android.opengl.GLES20;
import android.opengl.Matrix;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sneaky.stratagem.StratagemRenderer2D;
import com.sneaky.stratagem.armyselection.AvailableUnitEntry.AvailableUnitEntryFactory;
import com.sneaky.stratagem.armyselection.EmptyEntry.EmptyEntryFactory;
import com.sneaky.stratagem.armyselection.EnlistedUnitEntry.EnlistedUnitEntryFactory;
import com.sneaky.stratagem.battle.BattleModule;
import com.sneaky.stratagem.battle.BattleRenderer;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.graphics.widgets.Button;
import com.sneaky.stratagem.graphics.widgets.MessageBox;
import com.sneaky.stratagem.graphics.widgets.Button.ButtonFactory;
import com.sneaky.stratagem.graphics.widgets.MessageBox.MessageBoxFactory;
import com.sneaky.stratagem.match.Army;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.match.Player;
import com.sneaky.stratagem.proxy.ProxyRenderer;
import com.sneaky.stratagem.proxy.ProxyView;
import com.sneaky.stratagem.units.Unit;
import com.sneaky.stratagem.units.UnitFactory;
import com.sneaky.stratagem.units.UnitModule;
import com.sneaky.stratagem.units.blue.Adept;
import com.sneaky.stratagem.units.blue.Amplifier;
import com.sneaky.stratagem.units.blue.Armor;
import com.sneaky.stratagem.units.blue.CrazyHomonculus;
import com.sneaky.stratagem.units.blue.Elemental;
import com.sneaky.stratagem.units.blue.HandMage;
import com.sneaky.stratagem.units.blue.Homonculus;
import com.sneaky.stratagem.units.blue.Sage;
import com.sneaky.stratagem.units.commanders.BlueWizard;
import com.sneaky.stratagem.units.commanders.RedKnight;
import com.sneaky.stratagem.units.red.Brute;
import com.sneaky.stratagem.units.red.Catapult;
import com.sneaky.stratagem.units.red.Crossbowman;
import com.sneaky.stratagem.units.red.Drummer;
import com.sneaky.stratagem.units.red.Engie;
import com.sneaky.stratagem.units.red.Mason;
import com.sneaky.stratagem.units.red.ShieldBearer;
import com.sneaky.stratagem.units.red.Squire;

@Singleton
public class ArmySelectionRenderer extends StratagemRenderer2D {
    private final ArmySelectionModule dependencyModule;
    
    private int currentPlayer = 0;
    
    private final Device device;
    
    private final List<List<EnlistedUnitEntry>> enlistedUnits = new ArrayList<List<EnlistedUnitEntry>>();
    private final float enlistedUnitsXOffset = -0.25f;
    
    // Factories for creating various entries
    private final AvailableUnitEntryFactory availableFactory;
    private final EmptyEntryFactory emptyFactory;
    private final EnlistedUnitEntryFactory enlistedFactory;
    
    // Current page settings
    private int currentAvailablePagePos = 0;
    private int currentEnlistedPagePos = 0;
    private final int numEntriesPerPage = 4;
    
    // Available Units settings
    private final List<List<AvailableUnitEntry>> availableUnits = new ArrayList<List<AvailableUnitEntry>>();
    private final float                    availableUnitsXOffset = 0.25f;
    
    // Page Arrow settings
    private final float pageArrowSize           = 0.05f;
    private final int   pageArrowTexture;
    private final float pageDownArrowOffset     = 0.01f;
    private final float pageUpArrowBottomMargin = 0.115f;
    private final float pageUpArrowOffset       = 0.24f;
    
    // Entry settings
    private final float entryHeight = 0.1f;
    private final float entryWidth  = 0.4f;
    
    private final MessageBox introMessage;
    private final float introMessageHeight = 0.35f;
    private final float introMessageWidth= 0.5f;
    private boolean isDisplayIntro = true;
    
    // Recruit points background orb settings
    private final float recruitPointsOrbHeight = 0.15f;
    private final int   recruitPointsOrbTexture;
    private final float recruitPointsOrbOffset = 0.10f;
    private final float recruitPointsOrbWidth;
    
    // Recruit points settings
    private int               recruitPoints;
    private final float       recruitPointsHeight = 0.12f;
    private Optional<Integer> recruitPointsTexture = Optional.<Integer>absent();
    private float             recruitPointsWidth;
    
    // Section label settings
    private final float sectionLabelsHeight = 0.05f;
    private GlyphString availableUnitsLabel;
    private GlyphString enlistedUnitsLabel;
    
    // Input handling members
    private Optional<UnitEntry> pickedUpEntry = Optional.<UnitEntry>absent();
    private Optional<NormalizedPoint2D> pickedUpEntryPos = Optional.<NormalizedPoint2D>absent();
    private Optional<NormalizedPoint2D> touchedPos = Optional.<NormalizedPoint2D>absent();
    
    // Detailed unit view settings
    private Optional<Unit> detailViewedUnit = Optional.absent();
    private DetailedUnitView detailedUnitView;
    
    /** Ready button settings. */
    private Button readyButton;
    private final float readyButtonHeight = 0.1f;
    private final float readyButtonWidth = 0.1f;
    private final float readyButtonXPos = 0.425f;
    private final float readyButtonYPos = 0.425f;
    private final float readyButtonTextHeightRatio = 0.7f;
    
    private final Injector injector;
    
    /** Used for switching to the battle scene after selection is completed. */
    private final ProxyRenderer proxyRenderer;
    
    /** Used for queuing OpenGL operation outside of the OpenGL logic flow. */
    private final ProxyView proxyView;
    
    private SimpleTexturedShader shader;
    private final TextureFactory textureFactory;
    
    /** Injection constructor. */
    @Inject
    protected ArmySelectionRenderer(final ArmySelectionModule dependencyModule,
                                    final AvailableUnitEntryFactory availableFactory,
                                    @Named("SimpleButtonTexture") final int buttonBackgroundTexture,
                                    final ButtonFactory buttonFactory,
                                    final DetailedUnitView detailedUnitView,
                                    final Device device,
                                    final EmptyEntryFactory emptyFactory,
                                    final EnlistedUnitEntryFactory enlistedFactory,
                                    final GlyphStringFactory glyphStringFactory,
                                    final Injector injector,
                                    final MessageBoxFactory messageBoxFactory,
                                    @Named("SimpleArrowTexture") final int pageArrowTexture,
                                    final ProxyRenderer proxyRenderer,
                                    final ProxyView proxyView,
                                    @Named("RecruitPointsOrbTexture") final int recruitPointsOrbTexture,
                                    final SimpleTexturedShader shader,
                                    final TextureFactory textureFactory,
                                    final UnitFactory unitFactory) {
        super(injector);
        
        this.availableFactory = availableFactory;
        this.dependencyModule = dependencyModule;
        this.detailedUnitView = detailedUnitView;
        this.device = device;
        this.emptyFactory = emptyFactory;
        this.enlistedFactory = enlistedFactory;
        this.injector = injector;
        this.pageArrowTexture = pageArrowTexture;
        this.proxyRenderer = proxyRenderer;
        this.proxyView = proxyView;
        this.recruitPointsOrbWidth = this.recruitPointsOrbHeight / device.getAspectRatio();
        this.recruitPointsOrbTexture = recruitPointsOrbTexture;
        this.shader = shader;
        this.textureFactory = textureFactory;
        
        enlistedUnits.add(new ArrayList<EnlistedUnitEntry>());
        enlistedUnits.add(new ArrayList<EnlistedUnitEntry>());
        
        // Populate the red faction minions
        List<AvailableUnitEntry> redFactionUnits = new ArrayList<AvailableUnitEntry>();
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(Brute.class), entryHeight, entryWidth));
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(Catapult.class), entryHeight, entryWidth));
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(Crossbowman.class), entryHeight, entryWidth));
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(Drummer.class), entryHeight, entryWidth));
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(Engie.class), entryHeight, entryWidth));
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(Mason.class), entryHeight, entryWidth));
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(Squire.class), entryHeight, entryWidth));
        redFactionUnits.add(availableFactory.create(
                injector.getInstance(ShieldBearer.class), entryHeight, entryWidth));
        availableUnits.add(redFactionUnits);
        
        // Populate the blue faction minions
        List<AvailableUnitEntry> blueFactionUnits = new ArrayList<AvailableUnitEntry>();
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(Adept.class), entryHeight, entryWidth));
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(Amplifier.class), entryHeight, entryWidth));
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(Armor.class), entryHeight, entryWidth));
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(CrazyHomonculus.class), entryHeight, entryWidth));
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(Elemental.class), entryHeight, entryWidth));
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(HandMage.class), entryHeight, entryWidth));
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(Homonculus.class), entryHeight, entryWidth));
        blueFactionUnits.add(availableFactory.create(
                injector.getInstance(Sage.class), entryHeight, entryWidth));
        availableUnits.add(blueFactionUnits);

        setRecruitPoints(20);
        
        // Set the background clear color to black
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        availableUnitsLabel = glyphStringFactory.create("YOUR MINIONS", sectionLabelsHeight);
        enlistedUnitsLabel = glyphStringFactory.create("Red Knight's Army", sectionLabelsHeight);
        readyButton = buttonFactory.create(buttonBackgroundTexture, readyButtonHeight, 
                "READY!", readyButtonTextHeightRatio, readyButtonWidth);
        
        
        final float aspectRatio = introMessageWidth / introMessageHeight;
        introMessage = messageBoxFactory.create(aspectRatio, buttonBackgroundTexture);
        introMessage.setHeaderText("Red Knight", 0.3f);
        introMessage.setBodyText("Select Your Minions!", 0.35f);
    }
    
    @Override
    public void close() {
        super.close();
        
        dependencyModule.close(injector);
    }
    
    @Override
    public void drawFrame(final MVP mvp) {
        super.drawFrame(mvp);
        
        // Set the background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Render the "Ready!" button
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, readyButtonXPos, readyButtonYPos, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        readyButton.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        renderAvailableUnits(mvp);
        renderEnlistedUnits(mvp);
        renderRecruitPoints(mvp);
        renderPickedUpEntry(mvp);
        renderDetailedUnitView(mvp);
        
        if (isDisplayIntro) {
            model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.scaleM(model, Constants.NO_OFFSET, introMessageWidth, introMessageHeight, 1.0f);
            mvp.push(MVP.Type.MODEL, model);
            introMessage.render(mvp);
            mvp.pop(MVP.Type.MODEL);
        }
    }
    
    public void finishArmySelection() {
        if (currentPlayer == 0) {
            currentPlayer++;
            currentAvailablePagePos = 0;
            currentEnlistedPagePos = 0;
            recruitPoints = 20;
            
            GlyphStringFactory glyphStringFactory = injector.getInstance(GlyphStringFactory.class);
            enlistedUnitsLabel = glyphStringFactory.create("Blue Wizard's Army", sectionLabelsHeight);
            
            introMessage.setHeaderText("Blue Wizard", 0.3f);
            isDisplayIntro = true;
        } else {
            proxyRenderer.getView().queueEvent(new Runnable() {
                @Override
                public void run() {
                    
                    final Injector battleInjector = Guice.createInjector(
                            new BattleModule(proxyRenderer), new UnitModule(proxyRenderer));
                    final Match match = battleInjector.getInstance(Match.class);
                    
                    
                    // Set up player one
                    final Player playerOne = injector.getInstance(Player.class);
                    playerOne.setCurrentActionPoints(10);
                    playerOne.setMaxActionPoints(10);
                    playerOne.setName("Red Knight");
                    playerOne.setTeamColorTexture(battleInjector.getInstance(
                            Key.get(Integer.class, Names.named("RedTeamOverlay"))));
                    
                    // Repackage the player one army instantiated by the battle injector
                    final Army armyOne = new Army();
                    RedKnight redKnight = battleInjector.getInstance(RedKnight.class); //TODO TEMP
                    redKnight.setOwner(playerOne);
                    armyOne.setCommander(redKnight);
                    for (EnlistedUnitEntry entry : enlistedUnits.get(0)) {
                        if (entry.getNumEnlisted() > 0) {
                            Unit minion = battleInjector.getInstance(entry.getUnit().getClass());
                            minion.setOwner(playerOne);
                            
                            armyOne.addMinion(minion, entry.getNumEnlisted());
                        }
                    }
                    match.addPlayer(playerOne, armyOne);
                    
                    // Set up player two
                    final Player playerTwo = injector.getInstance(Player.class);
                    playerTwo.setCurrentActionPoints(10);
                    playerTwo.setMaxActionPoints(10);
                    playerTwo.setName("Blue Wizard");
                    playerTwo.setTeamColorTexture(battleInjector.getInstance(
                            Key.get(Integer.class, Names.named("BlueTeamOverlay"))));
                    
                    // Repackage the player two army
                    final Army armyTwo = new Army();
                    BlueWizard blueWizard = battleInjector.getInstance(BlueWizard.class);
                    blueWizard.setOwner(playerTwo);
                    armyTwo.setCommander(blueWizard);
                    for (EnlistedUnitEntry entry : enlistedUnits.get(1)) {
                        if (entry.getNumEnlisted() > 0) {
                            Unit minion = battleInjector.getInstance(entry.getUnit().getClass());
                            minion.setOwner(playerTwo);
                            
                            armyTwo.addMinion(minion, entry.getNumEnlisted());
                        }
                    }
                    match.addPlayer(playerTwo, armyTwo);
                    
                    BattleRenderer renderer = battleInjector.getInstance(BattleRenderer.class);
                    proxyRenderer.setRenderer(renderer);
                }
            });
        }
    }
    
    public int getRecruitPoints() { return recruitPoints; }
    
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {
        if (isDisplayIntro) {
            isDisplayIntro = false;
            
            return false;
        }
        
        // Check if the ready button was clicked
        Point2D position = new Point2D(readyButtonXPos, readyButtonYPos);
        if (InputHelper.isTouched(position, readyButtonWidth, readyButtonHeight, clickLocation)) {
            finishArmySelection();
            return true;
        }
        
        // If the detailed unit view was open, close it
        if (detailViewedUnit.isPresent()) {
            detailViewedUnit = Optional.absent();
            return true;
        }
        
        // Check if the enlisted units page up arrow was clicked
        position = new Point2D(enlistedUnitsXOffset, 0.5f - pageArrowSize / 2.0f - pageUpArrowOffset);
        if (InputHelper.isTouched(position, pageArrowSize, pageArrowSize, clickLocation) && 
                (currentEnlistedPagePos != 0)) {
            currentEnlistedPagePos -= numEntriesPerPage;
            return true;
        }
        
        // Check if the enlisted units page down arrow was clicked
        position = new Point2D(enlistedUnitsXOffset, -0.5f + pageArrowSize / 2.0f + pageDownArrowOffset);
        if (InputHelper.isTouched(position, pageArrowSize, pageArrowSize, clickLocation) &&
            (currentEnlistedPagePos + numEntriesPerPage < enlistedUnits.get(currentPlayer).size() - 1)) {
            currentEnlistedPagePos += numEntriesPerPage;
            return true;
        }
        
        // Check if the available units page up arrow was clicked
        position = new Point2D(availableUnitsXOffset, 0.5f - pageArrowSize / 2.0f - pageUpArrowOffset);
        if (InputHelper.isTouched(position, pageArrowSize, pageArrowSize, clickLocation) &&
            (currentAvailablePagePos != 0)) {
            currentAvailablePagePos -= numEntriesPerPage;
            return true;
        }
        
        // Check if the available units page down arrow was clicked
        position = new Point2D(availableUnitsXOffset, -0.5f + pageArrowSize / 2.0f + pageDownArrowOffset);
        if (InputHelper.isTouched(position, pageArrowSize, pageArrowSize, clickLocation) &&
            (currentAvailablePagePos + numEntriesPerPage < availableUnits.get(currentPlayer).size() - 1)) {
            currentAvailablePagePos += numEntriesPerPage;
        }
        
        // Check if the enlisted unit entries were clicked
        float yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; ((entryIter < numEntriesPerPage) && 
                (currentEnlistedPagePos + entryIter < enlistedUnits.get(currentPlayer).size())); entryIter++) {
            // Retrieve the appropriate entry
            final int currentPos = currentEnlistedPagePos + entryIter;
            EnlistedUnitEntry entry = enlistedUnits.get(currentPlayer).get(currentPos);
            
            // Check if it was clicked
            if (entry.handleClick(clickLocation, new NormalizedPoint2D(
                    enlistedUnitsXOffset, yPosition))) {
                return true;
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        return false;
    }
    
    @Override
    public boolean handlePickUp(NormalizedPoint2D touchLocation) {
        checkState(pickedUpEntry == Optional.<UnitEntry>absent(), "Drag-and-drop state out of step!");
        
        // Check if the enlist items were picked up
        float yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; ((entryIter < numEntriesPerPage) && 
                (currentEnlistedPagePos + entryIter < enlistedUnits.get(currentPlayer).size())); entryIter++) {
            // Retrieve the appropriate entry
            final int currentPos = currentEnlistedPagePos + entryIter;
            UnitEntry entry = enlistedUnits.get(currentPlayer).get(currentPos);
            
            // Check if it was picked up
            if (InputHelper.isTouched(new Point2D(enlistedUnitsXOffset, yPosition), entry.getWidth(), 
                    entry.getHeight(), touchLocation)) {
                pickedUpEntry = Optional.<UnitEntry>of(entry);
                pickedUpEntryPos = Optional.<NormalizedPoint2D>of(new NormalizedPoint2D(
                        enlistedUnitsXOffset, yPosition));
                touchedPos = Optional.<NormalizedPoint2D>of(touchLocation);
                
                return true;
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        // Check if the available items were picked up
        yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; ((entryIter < numEntriesPerPage) && 
                (currentAvailablePagePos + entryIter < availableUnits.get(currentPlayer).size())); entryIter++) {
            final int currentPos = currentAvailablePagePos + entryIter;
            
            // Retrieve the entry
            UnitEntry entry = availableUnits.get(currentPlayer).get(currentPos);
            
            // Check if it was picked up
            if (InputHelper.isTouched(new Point2D(availableUnitsXOffset, yPosition), entry.getWidth(), 
                    entry.getHeight(), touchLocation)) {
                pickedUpEntry = Optional.<UnitEntry>of(entry);
                pickedUpEntryPos = Optional.<NormalizedPoint2D>of(new NormalizedPoint2D(
                        availableUnitsXOffset, yPosition));
                touchedPos = Optional.<NormalizedPoint2D>of(touchLocation);
                
                return true;
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        return false;
    }

    @Override
    public boolean handleDrag(NormalizedPoint2D moveVector) {
        checkState(pickedUpEntryPos.isPresent(), "Drag-and-drop state out of step, picked up entry position is absent!");
        checkState(touchedPos.isPresent(), "Drag-and-drop state out of step, touched position is absent!");
        
        pickedUpEntryPos = Optional.<NormalizedPoint2D>of(pickedUpEntryPos.get().add(moveVector));
        touchedPos = Optional.<NormalizedPoint2D>of(touchedPos.get().add(moveVector));
        
        return true;
    }

    @Override
    public boolean handleDrop(NormalizedPoint2D dropLocation) {
        checkState(pickedUpEntry.isPresent(), "Drag-and-drop state out of step!");
        
        // Check if the enlist items were picked up
        float yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; entryIter < numEntriesPerPage; entryIter++) {
            final int currentPos = currentEnlistedPagePos + entryIter;
            final Entry entry = pickedUpEntry.get(); // Used to measure the non-existant entries
            
            // Check if it was dropped in the entry slot
            if (InputHelper.isTouched(new Point2D(enlistedUnitsXOffset, yPosition), entry.getWidth(), 
                    entry.getHeight(), pickedUpEntryPos.get()) ||
                InputHelper.isTouched(new Point2D(enlistedUnitsXOffset, yPosition), entry.getWidth(), 
                        entry.getHeight(), touchedPos.get())) {
                if (pickedUpEntry.get() instanceof AvailableUnitEntry) {
                    availableUnits.get(currentPlayer).remove(pickedUpEntry.get());
                    
                    if (currentPos < enlistedUnits.get(currentPlayer).size()) {
                        enlistedUnits.get(currentPlayer).add(currentPos, enlistedFactory.create(
                                pickedUpEntry.get().getUnit(), pickedUpEntry.get().getHeight(), 
                                pickedUpEntry.get().getWidth()));
                    } else {
                        enlistedUnits.get(currentPlayer).add(enlistedUnits.get(currentPlayer).size(), enlistedFactory.create(
                                pickedUpEntry.get().getUnit(), pickedUpEntry.get().getHeight(), 
                                pickedUpEntry.get().getWidth()));
                    }
                }
                
                pickedUpEntry = Optional.<UnitEntry>absent();
                pickedUpEntryPos = Optional.<NormalizedPoint2D>absent();
                touchedPos = Optional.<NormalizedPoint2D>absent();
                
                return true;
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; ((entryIter < numEntriesPerPage) && 
                (currentEnlistedPagePos + entryIter < enlistedUnits.get(currentPlayer).size())); entryIter++) {
            final int currentPos = currentAvailablePagePos + entryIter;
            final Entry entry = pickedUpEntry.get(); // Used to measure the non-existant entries
            
            // Check if it was dropped in the entry slot
            if (InputHelper.isTouched(new Point2D(availableUnitsXOffset, yPosition), entry.getWidth(), 
                    entry.getHeight(), pickedUpEntryPos.get()) ||
                InputHelper.isTouched(new Point2D(enlistedUnitsXOffset, yPosition), entry.getWidth(), 
                        entry.getHeight(), touchedPos.get())) {
                if (pickedUpEntry.get() instanceof EnlistedUnitEntry) {
                    EnlistedUnitEntry enlistedEntry = (EnlistedUnitEntry) pickedUpEntry.get();
                    recoverRecruitPoints(enlistedEntry.getNumEnlisted() * enlistedEntry.getUnit().getEnlistCost());
                    enlistedUnits.get(currentPlayer).remove(pickedUpEntry.get());
                    availableUnits.get(currentPlayer).add(currentPos, availableFactory.create(pickedUpEntry.get().getUnit(),
                            pickedUpEntry.get().getHeight(), pickedUpEntry.get().getWidth()));
                }
                
                pickedUpEntry = Optional.<UnitEntry>absent();
                pickedUpEntryPos = Optional.<NormalizedPoint2D>absent();
                touchedPos = Optional.<NormalizedPoint2D>absent();
                
                return true;
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        pickedUpEntry = Optional.<UnitEntry>absent();
        pickedUpEntryPos = Optional.<NormalizedPoint2D>absent();
        touchedPos = Optional.<NormalizedPoint2D>absent();
        
        return true;
    }

    @Override
    public boolean handleLongPress(NormalizedPoint2D pressLocation) {
        // Check if the enlist items were picked up
        float yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; ((entryIter < numEntriesPerPage) && 
                (currentEnlistedPagePos + entryIter < enlistedUnits.get(currentPlayer).size())); entryIter++) {
            // Retrieve the appropriate entry
            final int currentPos = currentEnlistedPagePos + entryIter;
            UnitEntry entry = enlistedUnits.get(currentPlayer).get(currentPos);
            
            // Check if it was picked up
            if (InputHelper.isTouched(new Point2D(enlistedUnitsXOffset, yPosition), entry.getWidth(), 
                    entry.getHeight(), pressLocation)) {
                detailViewedUnit = Optional.of(entry.getUnit());
                
                return true;
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        // Check if the available items were picked up
        yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; ((entryIter < numEntriesPerPage) && 
                (currentAvailablePagePos + entryIter < availableUnits.get(currentPlayer).size())); entryIter++) {
            final int currentPos = currentAvailablePagePos + entryIter;
            
            // Retrieve the entry
            UnitEntry entry = availableUnits.get(currentPlayer).get(currentPos);
            
            // Check if it was picked up
            if (InputHelper.isTouched(new Point2D(availableUnitsXOffset, yPosition), entry.getWidth(), 
                    entry.getHeight(), pressLocation)) {
                detailViewedUnit = Optional.of(entry.getUnit());
                
                return true;
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        return false;
    }
    
    @Override
    public boolean handleZoom(float zoomFactor) { return false; }
    
    public void recoverRecruitPoints(final int recoveredPoints) {
        setRecruitPoints(recruitPoints + recoveredPoints);
    }
    
    /** Renders the available units section of the interface. */
    private void renderAvailableUnits(final MVP mvp) {
        shader.activate();
        
        // Shift to the right side of the view
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, availableUnitsXOffset, 0.0f, 0.0f);
        mvp.push(MVP.Type.MODEL, model);

        // Render the section label
        final float sectionLabelYOffset = 0.3f;
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, sectionLabelYOffset, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        availableUnitsLabel.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Render the page up and page down arrows
        if (currentAvailablePagePos != 0) {
            renderPageUpArrow(mvp);
        }
        if (currentAvailablePagePos + numEntriesPerPage < availableUnits.get(currentPlayer).size()) {
            renderPageDownArrow(mvp);
        }
        
        // Render the available unit entries
        float yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; entryIter < numEntriesPerPage; entryIter++) {
            final int currentPos = currentAvailablePagePos + entryIter;
            
            // Check if there are any more available unit entries to render
            if (currentPos >= availableUnits.get(currentPlayer).size()) {
                break; // Done rendering the entries
            }
            
            // Retrieve the entry
            Entry entry = availableUnits.get(currentPlayer).get(currentPos);
            
            if (!pickedUpEntry.isPresent() || (entry != pickedUpEntry.get())) {
                renderEntry(entry, mvp, yPosition);
            }
                
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        mvp.pop(MVP.Type.MODEL);
    }
    
    /** Renders the detailed view of the selected unit. */
    private void renderDetailedUnitView(final MVP mvp) {
        if (detailViewedUnit.isPresent()) {
            shader.activate();
            
            detailedUnitView.setTarget(detailViewedUnit.get());
            detailedUnitView.render(mvp);
        }
    }
    
    /** Renders the enlisted units section of the interface. */
    private void renderEnlistedUnits(final MVP mvp) {
        shader.activate();
        
        // Shift to the left side of the view
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, enlistedUnitsXOffset, 0.0f, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        
        // Render the section label
        final float sectionLabelYOffset = 0.3f;
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, sectionLabelYOffset, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        enlistedUnitsLabel.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Render the page up and page down arrows
        if (currentEnlistedPagePos != 0) {
            renderPageUpArrow(mvp);
        }
        if (currentEnlistedPagePos + numEntriesPerPage < enlistedUnits.get(currentPlayer).size()) {
            renderPageDownArrow(mvp);
        }
        
        // Render the enlisted unit entries
        float yPosition = 0.5f - pageUpArrowOffset - pageArrowSize / 2.0f - pageUpArrowBottomMargin;
        for (int entryIter = 0; entryIter < numEntriesPerPage; entryIter++) {
            // Retrieve the appropriate entry
            Entry entry;
            final int currentPos = currentEnlistedPagePos + entryIter;
            if (currentPos < enlistedUnits.get(currentPlayer).size()) {
                entry = enlistedUnits.get(currentPlayer).get(currentPos);
            } else {
                entry = emptyFactory.create(entryHeight, entryWidth);
            }
            
            if (!pickedUpEntry.isPresent() || (entry != pickedUpEntry.get())) {
                renderEntry(entry, mvp, yPosition);
            }
            
            // Move to the next entry's position
            final float spaceBetweenEntries = 0.056f;
            yPosition -= entry.getHeight() + spaceBetweenEntries;
        }
        
        mvp.pop(MVP.Type.MODEL);
    }
    
    /** Renders an entry. */
    private void renderEntry(final Entry entry, final MVP mvp, final float yPosition) {
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, yPosition, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        entry.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
     
    /** Renders the page up arrow. */
    private void renderPageUpArrow(final MVP mvp) {  
        final float yPosition = 0.5f - pageArrowSize / 2.0f - pageUpArrowOffset;
        
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, yPosition, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, pageArrowSize / device.getAspectRatio(), pageArrowSize, 1.0f);
        
        shader.activate();
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(pageArrowTexture);
        shader.draw();
    }
    
    /** Render the page down arrow. */
    private void renderPageDownArrow(final MVP mvp) {
        final float yPosition = -0.5f + pageArrowSize / 2.0f + pageDownArrowOffset;        
        
        // Render the page down arrow
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);

        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, yPosition, 0.0f);
        Matrix.rotateM(model, Constants.NO_OFFSET, 180.0f, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, pageArrowSize / device.getAspectRatio(), pageArrowSize, 1.0f);
        
        shader.activate();
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(pageArrowTexture);
        shader.draw();
    }
    
    /** Renders the picked up entry. */
    private void renderPickedUpEntry(final MVP mvp) {
        if (pickedUpEntry.isPresent()) {
            shader.activate();
            
            Entry entry = pickedUpEntry.get();
            final float[] model = new float[Constants.MATRIX_SIZE];
            Matrix.setIdentityM(model, Constants.NO_OFFSET);
            Matrix.translateM(model, Constants.NO_OFFSET, pickedUpEntryPos.get().getX(), 
                    pickedUpEntryPos.get().getY(), 0.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, 1.2f, 1.2f, 1.0f);
            mvp.push(MVP.Type.MODEL, model);
            entry.render(mvp);
            mvp.pop(MVP.Type.MODEL);
        }
    }

    /** Renders the recruit points display. */
    private void renderRecruitPoints(final MVP mvp) {
        if (recruitPointsTexture.isPresent()) {  
            shader.activate();
            
            // Render the background orb
            float[] model = mvp.peekCopy(MVP.Type.MODEL);
            final float yPosition = 0.5f - recruitPointsOrbOffset;
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, yPosition, 0.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, recruitPointsOrbWidth, recruitPointsOrbHeight, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(recruitPointsOrbTexture);
            shader.draw();
            
            // Render the recruit points count
            model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, yPosition, 0.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, recruitPointsWidth, recruitPointsHeight, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(recruitPointsTexture.get());
            shader.draw();
        }
    }

    public synchronized void setRecruitPoints(final int recruitPoints) {
        this.recruitPoints = recruitPoints;
        
        proxyView.queueEvent(new Runnable() { 
            public void run() {        
                final float[] aspectRatio = new float[1];
                
                recruitPointsTexture = Optional.<Integer>of(textureFactory.texturizeText(
                        Integer.toString(recruitPoints), Color.WHITE, Align.CENTER, 60, aspectRatio));
                recruitPointsWidth = recruitPointsHeight * aspectRatio[0] / device.getAspectRatio();
            }});
    }
    
    public void spendRecruitPoints(final int spentPoints) {
        checkArgument(recruitPoints - spentPoints >= 0, "Spent more points than available!");
        
        setRecruitPoints(recruitPoints - spentPoints);
    }
}
