package scatcat.general.points;

import com.google.common.base.Objects;

/**
 * Represents a point in a grid i.e. a tile in the battlefield
 * 
 * @author R. Matt McCann
 */
public class GridPoint2D {
    private int column = 0;
    private int row = 0;
    
    /** Empty constructor. */
    public GridPoint2D() { }
    public GridPoint2D(int row, int column) {
        this.row = row;
        this.column = column;
    }

    /**
     * Adds to the row and column pointed to by this grid point.
     * 
     * @param rowOffset Row offset to add.
     * @param columnOffset Column offset to add.
     * @return New grid point pointing to the adjusted row and column.
     */
    public final GridPoint2D add(final int rowOffset, final int columnOffset) {
        GridPoint2D result = new GridPoint2D();
        
        result.row = this.row + rowOffset;
        result.column = this.column + columnOffset;
        
        return result;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GridPoint2D)) {
            return false;
        }
        
        GridPoint2D comparee = (GridPoint2D) object;
        return ((this.column == comparee.column) && (this.row == comparee.row));
    }

    public final int getColumn() { return column; }
    public final int getRow() { return row; }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(column, row);
    }
    
    public GridPoint2D minus(final GridPoint2D subtractee) {
        GridPoint2D sum = new GridPoint2D();
        
        sum.setColumn(column - subtractee.getColumn());
        sum.setRow(row - subtractee.getRow());
        
        return sum;
    }
    
    public GridPoint2D plus(final GridPoint2D addee) {
        GridPoint2D sum = new GridPoint2D();
        
        sum.setColumn(column + addee.getColumn());
        sum.setRow(row + addee.getRow());
    
        return sum;
    }
    
    public final void setColumn(int column) { this.column = column; }
    public final void setRow(int row) { this.row = row; }
}
