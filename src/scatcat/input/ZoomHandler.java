package scatcat.input;

/**
 * Handles zoom gestures.
 * 
 * @author R. Matt McCann
 */
public interface ZoomHandler {
    /**
     * @param zoomFactor Ratio of post-gesture zoom levels to pre-gesture zoom levels.
     * @return Whether or not the zoom was handled.
     */
    boolean handleZoom(float zoomFactor);
}
