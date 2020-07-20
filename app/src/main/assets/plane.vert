uniform mat4 u_ModelViewProjection;
attribute vec3 a_XZPositionAlpha;// (x, z, alpha)
varying vec4 color;

void main() {
    vec4 local_pos = vec4(a_XZPositionAlpha.x, 0.0, a_XZPositionAlpha.y, 1.0);
    gl_Position = u_ModelViewProjection * local_pos;

    color = vec4(0, 0.807843, 0.819608, 0.9);
}
