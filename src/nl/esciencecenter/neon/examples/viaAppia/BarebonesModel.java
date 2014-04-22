package nl.esciencecenter.neon.examples.viaAppia;

import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.datastructures.GLSLAttribute;
import nl.esciencecenter.neon.datastructures.VertexBufferObject;
import nl.esciencecenter.neon.exceptions.UninitializedException;
import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.FloatVectorMath;
import nl.esciencecenter.neon.models.Model;
import nl.esciencecenter.neon.shaders.ShaderProgram;

public class BarebonesModel extends Model {
    boolean            initialized = false;

    int                numVertices = 0;
    FloatBuffer        vertices;
    VertexBufferObject vbo;

    public BarebonesModel(List<Float3Vector> triangles) {
        super(VertexFormat.LINE_STRIP);
        numVertices = triangles.size();

        vertices = FloatVectorMath.vec3ListToBuffer(triangles);
    }

    /**
     * Initializes the model by constructing the {@link VertexBufferObject} out
     * of the vertices, normals and texCoords buffers.
     * 
     * @param gl
     *            The global openGL instance.
     */
    @Override
    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttribute vAttrib = new GLSLAttribute(vertices, "MCvertex", GLSLAttribute.SIZE_FLOAT, 3);

            vbo = new VertexBufferObject(gl, vAttrib);

            initialized = true;
        }
    }

    /**
     * Draw method for this model. Links its VertexBufferObject attributes and
     * calls OpenGL DrawArrays.
     * 
     * @param gl
     *            The global openGL instance.
     * @param program
     *            The shader program to be used for this drawing instance.
     * @throws UninitializedException
     */
    @Override
    public void draw(GL3 gl, ShaderProgram program) throws UninitializedException {
        if (initialized) {
            vbo.bind(gl);

            program.linkAttribs(gl, vbo.getAttribs());

            if (getFormat() == VertexFormat.TRIANGLES) {
                gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
            } else if (getFormat() == VertexFormat.POINTS) {
                gl.glDrawArrays(GL3.GL_POINTS, 0, numVertices);
            } else if (getFormat() == VertexFormat.LINES) {
                gl.glDrawArrays(GL3.GL_LINES, 0, numVertices);
            } else if (getFormat() == VertexFormat.LINE_STRIP) {
                gl.glDrawArrays(GL3.GL_LINE_STRIP, 0, numVertices);
            }
        } else {
            throw new UninitializedException();
        }
    }

}
