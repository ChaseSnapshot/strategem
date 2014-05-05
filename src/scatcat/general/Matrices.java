package scatcat.general;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

/**
 * Utility class for handling OpenGL matrices.
 * 
 * @author R. Matt McCann
 */
public final class Matrices {
    /** Hidden constructor. */
    private Matrices() { }
    
    /**
     * Checks whether or not the provided matrix is an identity matrix.
     * 
     * @param matrix Must not be null. Must have a size of 16.
     * @return Whether or not matrix is an identity.
     */
    public static boolean isIdentity(final float[] matrix) {
        // Check the argument is well-formed.
        checkNotNull(matrix);
        checkArgument(matrix.length == Constants.MATRIX_SIZE, 
                "Matrix must have a length of 16, got %s!", matrix.length);
        
        return Objects.equal(matrix,
                new float[] {1.0f, 0.0f, 0.0f, 0.0f,
                             0.0f, 1.0f, 0.0f, 0.0f,
                             0.0f, 0.0f, 1.0f, 0.0f,
                             0.0f, 0.0f, 0.0f, 1.0f});
    }
}
