uniform mat4     u_MVPMatrix; 	// Model-View-Projection matrix to transform the vertex by

attribute vec4   a_Position; 	// Input Position of the vertex
attribute vec4   a_Color;		// Input color of the vertex

varying vec4	 v_Color;		// Output vertex color

void main() {
	v_Color = a_Color;

	gl_Position = u_MVPMatrix * a_Position;	// Final vertex position
}