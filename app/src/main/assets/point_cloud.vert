uniform mat4 u_ModelViewProjection;
uniform vec4 u_Color;
uniform float u_PointSize;
uniform float tmp;


attribute vec4 a_Position;

varying vec4 v_Color;

void main() {
    v_Color = u_Color;

    if (tmp == -1.0) { gl_Position = a_Position; }
    if (tmp != -1.0) { gl_Position = u_ModelViewProjection * vec4(a_Position.xyz, 1.0); }

    if (u_PointSize != -1.0) {
        gl_PointSize = u_PointSize;
    }
}
