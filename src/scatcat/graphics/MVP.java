package scatcat.graphics;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Stack;

import scatcat.general.Constants;
import android.opengl.Matrix;

/**
 * Model-View-Projection matrices manager. This class manages 
 * the transformations applied to each of these matrices using 
 * stacks.
 * 
 * @author R. Matt McCann
 * @since 1.0
 */
public final class MVP {
    /** Hidden constructor. This is a utility class. */
    public MVP() {
        float[] identityMatrix = new float[Constants.MATRIX_SIZE];
        Matrix.setIdentityM(identityMatrix, Constants.NO_OFFSET);
        mModelMatrix.push(identityMatrix);
        mProjectionMatrix.push(identityMatrix);
        mViewMatrix.push(identityMatrix);
    }
    
    /** Transformation history of the model matrix. */
    private Stack<float[]> mModelMatrix = new Stack<float[]>();
    
    /** Transformation history of the projection matrix. */
    private Stack<float[]> mProjectionMatrix = new Stack<float[]>();
    
    /** Transformation history of the view matrix. */
    private Stack<float[]> mViewMatrix = new Stack<float[]>();
    
    /** Model, View, or Projection types. */
    public enum Type {
        MODEL,
        PROJECTION,
        VIEW
    };
    
    /**
     * Collapses the model, view, and projection matrices into a MVP
     * matrix for direct use with OpenGL.
     * 
     * @return Collapsed MVP matrix.
     */
    public float[] collapse() {
        return collapse(mModelMatrix.peek(), mProjectionMatrix.peek(), mViewMatrix.peek());
    }
    
    /**
     * Collapses the model, view, and projection matrices into a MVP
     * matrix for direct use with OpenGL.
     * 
     * @param model Must not be null. Must have a length of 16.
     * @param projection Must not be null. Must have a length of 16.
     * @param view Must not be null. Must have a length of 16.
     * 
     * @return Collapsed MVP matrix.
     */
    public float[] collapse(final float[] model,
                            final float[] projection,
                            final float[] view) {
        // Verify the function arguments are valid
        checkArgument(model != null, "Model matrix must not be null!");
        checkArgument(model.length == Constants.MATRIX_SIZE,
                "Model matrix must have a length of 16, got %s", model.length);
        checkArgument(projection != null, "Projection matrix must not be null!");
        checkArgument(projection.length == Constants.MATRIX_SIZE,
                "Projection matrix must have a length of 16, got %s", projection.length);
        checkArgument(view != null, "View matrix must not be null!");
        checkArgument(view.length == Constants.MATRIX_SIZE,
                "View matrix must must have a length of 16, got %s", view.length);
        
        float[] mvp = new float[Constants.MATRIX_SIZE];
       
        // Factor in the view and model matrices
        //Matrix.multiplyMM(mvp, Constants.NO_OFFSET, 
        //        view, Constants.NO_OFFSET,
        //        model, Constants.NO_OFFSET);
        //Matrix.multiplyMM(mvp, 0, projection, 0, view, 0);
        Matrix.multiplyMM(mvp, 0, projection, 0, model, 0);
        
        // Factor in the projection matrix
        //Matrix.multiplyMM(mvp, Constants.NO_OFFSET, 
        //        mvp, Constants.NO_OFFSET,
        //        projection, Constants.NO_OFFSET);
        //Matrix.multiplyMM(mvp, 0, mvp, 0, model, 0);
        
        return mvp;
    }
    
    /**
     * Collapses the provided model matrix with the projection and view matrices
     * on top of their respective stacks.
     * 
     * @param modelMatrix Must not be null. Must have a length of 16.
     * @return Collapsed MVP matrix.
     */
    public float[] collapseM(final float[] modelMatrix) {
        return collapse(modelMatrix, mProjectionMatrix.peek(), mViewMatrix.peek());
    }
    
    /**
     * Returns the stack of correct type.
     * 
     * @param matrixType Type of stack to get. Must not be null.
     * @return Matrix stack.
     */
    private Stack<float[]> getStack(final Type matrixType) {
        checkNotNull(matrixType);
        
        switch (matrixType) {
            case MODEL:
                return mModelMatrix;
            case PROJECTION:
                return mProjectionMatrix;
            case VIEW:
                return mViewMatrix;
            default:
                throw new UnsupportedOperationException("Unsupported matrix type!");    
        }
    }
    
    /**
     * Removes the matrix on the top of the stack for the specified type.
     * 
     * @param matrixType Model, View, or Projection. Must not be null.
     * @return The matrix on top of the stack.
     */
    public float[] pop(final Type matrixType) {
        return getStack(matrixType).pop();
    }
    
    /**
     * Pushes a matrix on to the stack of the specified type.
     * 
     * @param matrixType Model, View, or Projection. Must not be null.
     * @param matrix Matrix to push on the stack. Must not be null, 
     *                must have a length of 16.
     */
    public void push(final Type matrixType,
                     final float[] matrix) {
        checkArgument(matrix.length == Constants.MATRIX_SIZE, 
                "Matrix must have a length of 16, got %s", matrix.length);
        
        // Copy the matrix
        final float[] copy = new float[Constants.MATRIX_SIZE];
        System.arraycopy(matrix, Constants.NO_OFFSET, 
                         copy, Constants.NO_OFFSET, matrix.length);
        
        // Push the matrix
        getStack(matrixType).push(copy);
    }
    
    /**
     * Returns a copy of the matrix on top of the specified stack.
     * 
     * @param matrixType Model, View, or Projection. Must not be null.
     * @return Copy of the peeked matrix.
     */
    public float[] peekCopy(final Type matrixType) {
        final float[] copy = new float[Constants.MATRIX_SIZE];    
        System.arraycopy(getStack(matrixType).peek(), Constants.NO_OFFSET, 
                         copy, Constants.NO_OFFSET, copy.length);
        return copy;
    }
    
    /**
     * Returns a reference to the matrix on top of the specified stack.
     * 
     * @param matrixType Model, View, or Projection. Must not be null.
     * @return Matrix on top of the stack.
     */
    public float[] peek(final Type matrixType) {
        return getStack(matrixType).peek();
    }

}