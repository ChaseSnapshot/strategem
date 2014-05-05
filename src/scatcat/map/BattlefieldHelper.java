package scatcat.map;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Vector;
import scatcat.general.points.GridPoint2D;

import com.google.common.base.Optional;
import com.sneaky.stratagem.units.Unit;

/**
 * A variety of helpful value-added functions for interacting with the battlefield.
 * 
 * @author R. Matt McCann
 */
public abstract class BattlefieldHelper {
    /**
     * Locates the tile the provided unit is occupying.
     * 
     * @param unit Must not be null.
     * @return Tile unit is currently occupying if unit exists on the battlefield
     */
    public static Optional<GridPoint2D> getTile(Battlefield battlefield, Unit unit) {
        checkArgument(battlefield != null, "Battlefield must not be null!");
        checkArgument(unit != null, "Unit must not be null!");
        
        // For each row
        for (int rowIter = 0; rowIter < battlefield.getLayout().size(); rowIter++) {
            Vector<Tile> row = battlefield.getLayout().get(rowIter);
            
            // For each column
            for (int colIter = 0; colIter < row.size(); colIter++) {
                Tile tile = row.get(colIter);
                
                if (tile.hasOccupant() && tile.getOccupant() == unit) {
                    return Optional.of(new GridPoint2D(rowIter, colIter));
                }
            }
        }
        
        // Unit wasn't found!
        return Optional.absent();
    }
}
