uniform mat4 u_MVPMatrix;	// Model-View-Projection matrix to transform the vertex by

attribute vec4 a_Position;	// Position of the vertex
attribute vec2 a_TexCoord;	// Input color of the vertex

// Vertex shader output variables (Thus input for fragment shader
varying vec2 v_TexCoord;		// Output vertex color

void main() {
	v_TexCoord = a_TexCoord; // Pass through the texture coordinate
	gl_Position = u_MVPMatrix * a_Position;	// Final vertex position
}