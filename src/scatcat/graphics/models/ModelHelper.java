package scatcat.graphics.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sneaky.stratagem.R;

import scatcat.general.Constants;
import scatcat.general.points.Point2D;
import scatcat.general.points.Point3D;

public class ModelHelper {
    private final Context context;
    
    private final Injector injector;

    public enum Type {
        ISLAND
    }
    
    @Inject
    protected ModelHelper(final Context context,
                          final Injector injector) {
        this.context = context;
        this.injector = injector;
    }
    
    public Model buildModel(final Type type) {
        switch (type) {
            case ISLAND:
                Model model = loadModel(R.raw.island_base);
                //model.setTextureHandle(TextureHelper.loadTexture(R.drawable.island_base));
                return model;
            default:
                throw new RuntimeException("Unsupported model type!");
        }
    }
    
    /**
     * Loads a model from an OBJ file.
     * 
     * @param objFile Resource ID of a file containing the OBJ data.
     * @return Model reconstituted from the OBJ data.
     */
    private Model loadModel(final int objFile) {
        final List<Integer> indices = new ArrayList<Integer>();
        final List<Point2D> textureCoords = new ArrayList<Point2D>();
        final List<Point3D> positions = new ArrayList<Point3D>();
        
        loadRawDataOBJ(objFile, indices, textureCoords, positions);
        
        // Pack the vertices and texture coordinates into a buffer
        FloatBuffer dataBuffer = ByteBuffer.allocateDirect(indices.size() * 5 / 2 * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int indexIter = 0; indexIter < indices.size(); indexIter+=2) { // Indices are arranged as vertex index, texture coordinates
            int positionPos = indices.get(indexIter);
            int textureCoordPos = indices.get(indexIter + 1);
            
            // Add the vertex positions to the buffer
            Point3D position = positions.get(positionPos);
            if ((Math.abs(position.getX()) >= 20.0f) ||
                (Math.abs(position.getY()) >= 20.0f) ||
                (Math.abs(position.getZ()) >= 20.0f)) {
                Log.e("MODEL", "Vertex (" + position.getX() + "," + position.getY() + "," + position.getZ() + ")");
            }
            dataBuffer.put(position.getX());
            dataBuffer.put(position.getY());
            dataBuffer.put(position.getZ());
            
            // Add the texture coordinates to the buffer
            Point2D textureCoord = textureCoords.get(textureCoordPos);
            if ((textureCoord.getX() < 0.0f) && (textureCoord.getX() > 1.0f) &&
                (textureCoord.getY() < 0.0f) && (textureCoord.getY() > 1.0f)) {
                Log.e("MODEL", "TexCoord (" + textureCoord.getX() + ", " + textureCoord.getY() + ")");
            }
            dataBuffer.put(textureCoord.getX());
            dataBuffer.put(textureCoord.getY());
        }
        dataBuffer.position(Constants.BEGINNING_OF_BUFFER);
        
        // Generate a VBO, load it, and get the handle 
        final int[] vboHandle = new int[1];
        GLES20.glGenBuffers(1, vboHandle, Constants.BEGINNING_OF_BUFFER);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 
                dataBuffer.capacity() * Constants.BYTES_PER_FLOAT, dataBuffer, GLES20.GL_STATIC_DRAW);
                
        // Construct the model
        Model model = injector.getInstance(Model.class);
        model.setDataVBOHandle(vboHandle[0]);
        model.setNumVertices(indices.size() / 2);
        
        return model;
    }
    
    /**
     * Loads the raw data from the OBJ file.
     * 
     * @param objFile Resource ID of a file containing the OBJ data.
     * @param indices [Output] Ordered list of the defined indices
     * @param textureCoords [Output] Ordered list of the texture coordinates
     * @param positions [Output] Ordered list of the vertex positions
     */
    private void loadRawDataOBJ(final int objFile,
                                final List<Integer> indices,
                                final List<Point2D> textureCoords,
                                final List<Point3D> positions) {
        BufferedReader input = null;
        float largestVertex = 0.0f;
        float minX = 999999.0f;
        float maxX = -999999.9f;
        float minY = 999999.0f;
        float maxY = -999999.9f;
        float minZ = 999999.0f;
        float maxZ = -999999.9f;
        
        try {
            // Open the file
            input = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(objFile)));
            
            // Parse the file
            String line = null;
            while ((line = input.readLine()) != null) {
                String[] tokens = line.split(" |/");
                
                // Inspect the identifier token
                final int identifierPos = 0;
                String identifier = tokens[identifierPos];
                
                // If the line contains a vertex definition
                if (identifier.equalsIgnoreCase("V")) {
                    Point3D vertex = new Point3D();
                    
                    // Parse the X value
                    final int xPos = 1;
                    vertex.setX(Float.parseFloat(tokens[xPos]));
                    if (Math.abs(vertex.getX()) > largestVertex) {
                        largestVertex = Math.abs(vertex.getX());
                    }
                    if (vertex.getX() < minX) {
                        minX = vertex.getX();
                    }
                    if (vertex.getX() > maxX) {
                        maxX = vertex.getX();
                    }
                    
                    // Parse the Y value
                    final int yPos = 2;
                    vertex.setY(Float.parseFloat(tokens[yPos]));
                    if (Math.abs(vertex.getY()) > largestVertex) {
                        largestVertex = Math.abs(vertex.getY());
                    }
                    if (vertex.getY() < minY) {
                        minY = vertex.getY();
                    }
                    if (vertex.getY() > maxY) {
                        maxY = vertex.getY();
                    }
                    
                    // Parse the Z value
                    final int zPos = 3;
                    vertex.setZ(Float.parseFloat(tokens[zPos]));
                    if (Math.abs(vertex.getZ()) > largestVertex) {
                        largestVertex = Math.abs(vertex.getZ());
                    }
                    if (vertex.getZ() < minZ) {
                        minZ = vertex.getZ();
                    }
                    if (vertex.getZ() > maxZ) {
                        maxZ = vertex.getZ();
                    }
                    
                    // Save the vertex
                    positions.add(vertex);
                }
                // If the line contains a texture coordinate definition
                else if (identifier.equalsIgnoreCase("VT")) {
                    Point2D textureCoord = new Point2D();
                    
                    
                    // Parse the U value
                    final int uPos = 1;
                    textureCoord.setX(Float.parseFloat(tokens[uPos]));
                    
                    // Parse teh V value
                    final int vPos = 2;
                    textureCoord.setY(Float.parseFloat(tokens[vPos]));
                    
                    // Save the coordinate
                    textureCoords.add(textureCoord);
                }
                // If the line contains a face definition
                else if (identifier.equalsIgnoreCase("F")) {
                    final int triangleFaceSize = 7;
                    final int squareFaceSize = 9;
                    final int vertex1Pos = 1;
                    final int texCoord1Pos = 2;
                    final int vertex2Pos = 3;
                    final int texCoord2Pos = 4;
                    final int vertex3Pos = 5;
                    final int texCoord3Pos = 6;
                    final int vertex4Pos = 7;
                    final int texCoord4Pos = 8;
                    
                    // If the face is a triangle, nothing extra needs to be done so save it
                    if (tokens.length == triangleFaceSize) {
                        // Parse the first point
                        indices.add(Integer.parseInt(tokens[vertex1Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord1Pos]) - 1);
                        
                        // Parse the second point
                        indices.add(Integer.parseInt(tokens[vertex2Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord2Pos]) - 1);
                        
                        //TEMP // Parse the second point
                        indices.add(Integer.parseInt(tokens[vertex2Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord2Pos]) - 1);
                        
                        // Parse the third point
                        indices.add(Integer.parseInt(tokens[vertex3Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord3Pos]) - 1);
                        
                        //TEMP // Parse the third point
                        indices.add(Integer.parseInt(tokens[vertex3Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord3Pos]) - 1);
                        
                        //TEMP // Parse the first point
                        indices.add(Integer.parseInt(tokens[vertex1Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord1Pos]) - 1);
                    }
                    // If the face is a square, break it down into two triangles
                    else if (tokens.length == squareFaceSize) {
                        // Parse the first point of the first triangle
                        indices.add(Integer.parseInt(tokens[vertex1Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord1Pos]) - 1);
                        
                        // Parse the second point of the first triangle
                        indices.add(Integer.parseInt(tokens[vertex2Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord2Pos]) - 1);
                        
                        //TEMP // Parse the second point of the first triangle
                        indices.add(Integer.parseInt(tokens[vertex2Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord2Pos]) - 1);
                        
                        // Parse the third point of the first triangle
                        indices.add(Integer.parseInt(tokens[vertex3Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord3Pos]) - 1);
                        
                        //TEMP // Parse the third point of the first triangle
                        indices.add(Integer.parseInt(tokens[vertex3Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord3Pos]) - 1);
                        
                        //TEMP // Parse the first point of the first triangle
                        indices.add(Integer.parseInt(tokens[vertex1Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord1Pos]) - 1);
                        
                        // Parse the first point of the second triangle
                        indices.add(Integer.parseInt(tokens[vertex1Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord1Pos]) - 1);
                        
                        // Parse the second point of the second triangle
                        indices.add(Integer.parseInt(tokens[vertex3Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord3Pos]) - 1);
                        
                        //TEMP // Parse the second point of the second triangle
                        indices.add(Integer.parseInt(tokens[vertex3Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord3Pos]) - 1);
                        
                        // Parse the third point of the second triangle
                        indices.add(Integer.parseInt(tokens[vertex4Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord4Pos]) - 1);
                        
                        //TEMP // Parse the third point of the second triangle
                        indices.add(Integer.parseInt(tokens[vertex4Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord4Pos]) - 1);
                        
                        //TEMP // Parse the first point of the second triangle
                        indices.add(Integer.parseInt(tokens[vertex1Pos]) - 1);
                        indices.add(Integer.parseInt(tokens[texCoord1Pos]) - 1);
                    } else {
                        throw new RuntimeException("Unsupported face type!");
                    }
                }
            }
        } catch (IOException ex) {
            Log.e("MODELS", "Failed to load an OBJ model!" + ex.toString());
        } finally {
            // Close the file
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Log.e("MODELS", "Failed to close the OBJ file!" + ex.toString());
                }
            }
        }
        
        // Center the vertices in the XY plane
        //final float xShift = -(minX + maxX);
        //final float yShift = -(minY + maxY);
        //float scaleFactor = 0.0f;
        //for (Point3D vertex : positions) {
        //    vertex.addToX(xShift);
        //    vertex.addToY(yShift);
        //    
        //    if (vertex.getX() > scaleFactor) {
        //        scaleFactor = vertex.getX();
        //    }
        //    if (vertex.getY() > scaleFactor) {
        //        scaleFactor = vertex.getY();
        //    }
        //    if (vertex.getZ() > scaleFactor) {
        //        scaleFactor = vertex.getZ();
        //    }
       // }
        
        // Normalize the vertices
        for (Point3D vertex : positions) {
            vertex.normalize(largestVertex * 2.0f); // Want the texture coordinates to range from -0.5f to 0.5f
            
            if ((Math.abs(vertex.getX()) >= 20.0f) ||
                (Math.abs(vertex.getY()) >= 20.0f) ||
                (Math.abs(vertex.getZ()) >= 20.0f)) {
                Log.e("MODEL", "Vertex (" + vertex.getX() + "," + vertex.getY() + "," + vertex.getZ() + ")");
            }
        }
        Log.e("MODEL", "Done with vertices");
    }
}
