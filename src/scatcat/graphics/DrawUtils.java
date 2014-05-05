package scatcat.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Stack;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.FloatMath;

import scatcat.general.Constants;

/**
 * Provides a collection of generic drawing functionalities
 * and definitions.
 * 
 * @author R. Matt McCann
 */
public final class DrawUtils {
    /** Hidden constructor. */
    private DrawUtils() { }

    /**
     * Returns the handle to a VBO packed with unit square vertices,
     * the provided vertex color, and texture coordinates.
     * @param color Uniform color for the vertices
     * @return Handle to packed VBO.
     */
    public static int buildUnitSquarePctVbo(final Color color) {
        final int stride = POS_COLOR_TEX_STRIDE;
        final float[] data = new float[stride * NUM_SQUARE_VERTICES];
        
        // Populate the data array
        for (int iter = 0; iter < NUM_SQUARE_VERTICES; iter++) {
            data[iter * stride] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE];
            data[iter * stride + 1] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE + 1];
            data[iter * stride + 2] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE + 2];
            data[iter * stride + 3] = color.getRed();
            data[iter * stride + 4] = color.getGreen();
            data[iter * stride + 5] = color.getBlue();
            data[iter * stride + 6] = color.getAlpha();
            data[iter * stride + 7] = UNIT_TEX_COORDS[iter * COLOR_DATA_SIZE];
            data[iter * stride + 8] = UNIT_TEX_COORDS[iter * COLOR_DATA_SIZE + 1];
        }
        
        // Pack the array into a buffer
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(data);
        
        // Generate a VBO, load it, and get the handle 
        final int[] handle = new int[1];
        GLES20.glGenBuffers(1, handle, Constants.BEGINNING_OF_BUFFER);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 
                buffer.capacity() * Constants.BYTES_PER_FLOAT, buffer, GLES20.GL_STATIC_DRAW);
        
        return handle[0];
    }
    
    /**
     * Returns the handle of a VBO packed with the unit circle vertices
     * of the specified color.
     * 
     * @param color Color of the vertices.
     * @return Handle of the VBO.
     */
    public static int buildUnitCirclePcVbo(final Color color) {
        final int stride = POS_COLOR_STRIDE;
        final float[] data = new float[stride * NUM_CIRCLE_VERTICES];
        final float radius = 0.5f;
        
        data[0] = 0.0f;
        data[1] = 0.0f;
        data[2] = 0.0f;
        data[3] = color.getRed();
        data[4] = color.getGreen();
        data[5] = color.getBlue();
        data[6] = color.getAlpha();
        
        for (int vertexIter = 1; vertexIter < NUM_CIRCLE_VERTICES; vertexIter++) {
            float theta = 2.0f * (float) Math.PI * (vertexIter - 1) / (NUM_CIRCLE_VERTICES - 2);
            
            data[vertexIter * stride] = FloatMath.cos(theta) * radius;
            data[vertexIter * stride + 1] = FloatMath.sin(theta) * radius;
            data[vertexIter * stride + 2] = 0.0f;
            data[vertexIter * stride + 3] = color.getRed();
            data[vertexIter * stride + 4] = color.getGreen();
            data[vertexIter * stride + 5] = color.getBlue();
            data[vertexIter * stride + 6] = color.getAlpha();
        }
        
        /*for (int vertexIter = stride; vertexIter < NUM_CIRCLE_VERTICES * stride; vertexIter += stride) {
            data[vertexIter]     = FloatMath.sin(3.0f * vertexIter * Constants.DEG_2_RAD) * radius;
            data[vertexIter + 1] = FloatMath.cos(3.0f * vertexIter * Constants.DEG_2_RAD) * radius;
            data[vertexIter + 2] = 0.0f;
            data[vertexIter + 3] = color.getRed();
            data[vertexIter + 4] = color.getGreen();
            data[vertexIter + 5] = color.getBlue();
            data[vertexIter + 6] = color.getAlpha();
        }*/
        
        return packVerticesIntoVbo(data);
    }
    
    /**
     * Returns the handle to a VBO packed with unit square vertices
     * and the provided vertex color.
     * 
     * @param color Color of the vertices
     * @return Handle to the VBO
     */
    public static int buildUnitSquarePcVbo(final Color color) {
        final int stride = POS_COLOR_STRIDE;
        final float[] data = new float[stride * NUM_SQUARE_VERTICES];
        
        // Populate the data array
        for (int iter = 0; iter < NUM_SQUARE_VERTICES; iter++) {
            data[iter * stride] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE];
            data[iter * stride + 1] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE + 1];
            data[iter * stride + 2] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE + 2];
            data[iter * stride + 3] = color.getRed();
            data[iter * stride + 4] = color.getGreen();
            data[iter * stride + 5] = color.getBlue();
            data[iter * stride + 6] = color.getAlpha();
        }
        
        return packVerticesIntoVbo(data);
    }
    
    public static float[] collapseMVP(float[] modelViewMatrix, float[] projectionMatrix) {
        final float[] mvpMatrix = new float[16];
        
        Matrix.multiplyMM(mvpMatrix, Constants.BEGINNING_OF_BUFFER, 
                projectionMatrix, Constants.BEGINNING_OF_BUFFER, 
                modelViewMatrix, Constants.BEGINNING_OF_BUFFER);
        
        return mvpMatrix;
    }
    
    /**
     * Returns the handle to a VBO packed with unit square vertices
     * and texture coordinates.
     * 
     * @return Handle to packed VBO.
     */
    public static int getUnitSquarePtVbo() {
        if (mUnitSquareVBOHandle == null) {
            final int stride = POS_TEX_STRIDE;
            final float[] data = new float[stride * NUM_SQUARE_VERTICES];
            
            // Populate the data array
            for (int iter = 0; iter < NUM_SQUARE_VERTICES; iter++) {
                data[iter * stride] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE];
                data[iter * stride + 1] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE + 1];
                data[iter * stride + 2] = UNIT_POSITIONS[iter * POSITION_DATA_SIZE + 2];
                data[iter * stride + 3] = UNIT_TEX_COORDS[iter * TEX_COORD_DATA_SIZE];
                data[iter * stride + 4] = UNIT_TEX_COORDS[iter * TEX_COORD_DATA_SIZE + 1];
            }

            // Generate a VBO, load it, and get the handle 
            mUnitSquareVBOHandle = packVerticesIntoVbo(data);
        }
        
        return mUnitSquareVBOHandle;
    }

    /**
     * Sets up a 2D orthographic projection.
     * 
     * @return Projection matrix
     */
    public static float[] gen2DOrthoProjection() {
        final float[] matrix = new float[Constants.MATRIX_SIZE];
        
        // Get the screen resolution
        final float near = -1.0f;
        final float far = 1.0f;
        
        final float left = -0.5f; //-Device.getWidth() / 2.0f;
        final float right = 0.5f; //Device.getWidth() / 2.0f;
        final float top = 0.5f; //Device.getHeight() / 2.0f;
        final float bottom = -0.5f; //-Device.getHeight() / 2.0f;
        
        Matrix.orthoM(matrix, Constants.BEGINNING_OF_BUFFER, left, right, bottom, top, near, far);
        return matrix;
    }
    
    /**
     * Packs the vertex data into a VBO.
     * 
     * @param data Data to pack.
     * @return Handle of the VBO.
     */
    public static int packVerticesIntoVbo(final float[] data) {
        // Pack the array into a buffer
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(data);
        buffer.position(Constants.BEGINNING_OF_BUFFER);
        
        // Generate a VBO, load it, and get the handle 
        final int[] handle = new int[1];
        GLES20.glGenBuffers(1, handle, Constants.BEGINNING_OF_BUFFER);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handle[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 
                buffer.capacity() * Constants.BYTES_PER_FLOAT, buffer, GLES20.GL_STATIC_DRAW);
        
        return handle[0];
    }
    
    public static float[] popMatrix() {
        return mMatrixStack.pop();
    }
    
    public static void pushMatrix(final float[] matrix) {
        final float[] copy = new float[16];
        System.arraycopy(matrix, Constants.BEGINNING_OF_BUFFER, 
                copy, Constants.BEGINNING_OF_BUFFER, matrix.length);
        mMatrixStack.push(copy);
    }
    
    public static float[] copyOfCurrentMatrix() {
        final float[] copy = new float[16];
        System.arraycopy(mMatrixStack.peek(), Constants.BEGINNING_OF_BUFFER, 
                copy, Constants.BEGINNING_OF_BUFFER, copy.length);
        return copy;
    }
    
    public static float[] currentMatrix() {
        return mMatrixStack.peek();
    }
    
    private static final int COLOR_DATA_SIZE = 4;
    private static final int POSITION_DATA_SIZE = 3;
    public static final int  NUM_CIRCLE_VERTICES = 38;
    private static final int NUM_SQUARE_VERTICES = 6;
    private static final int TEX_COORD_DATA_SIZE = 2;
    
    public static final int POS_COLOR_STRIDE = POSITION_DATA_SIZE 
            + COLOR_DATA_SIZE;
    public static final int POS_COLOR_TEX_STRIDE = POSITION_DATA_SIZE
            + COLOR_DATA_SIZE + TEX_COORD_DATA_SIZE;
    public static final int POS_TEX_STRIDE = POSITION_DATA_SIZE
            + TEX_COORD_DATA_SIZE;
    
    private static final float[] UNIT_POSITIONS = new float[] {
        -0.5f, 0.5f, 0.0f, // Top left
        -0.5f, -0.5f, 0.0f, // Bottom left
        0.5f, 0.5f, 0.0f, // Top Right
        -0.5f, -0.5f, 0.0f, // Bottom left
        0.5f, -0.5f, 0.0f, // Bottom right
        0.5f, 0.5f, 0.0f // Top right
    };
    private static final float[] UNIT_TEX_COORDS = new float[] {
        0.0f, 0.0f,                 
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    };

    private static Integer mUnitSquareVBOHandle;
    private static Stack<float[]> mMatrixStack = new Stack<float[]>();
}
