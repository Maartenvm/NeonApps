package nl.esciencecenter.neon.examples.viaAppia;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.exceptions.UninitializedException;
import nl.esciencecenter.neon.input.InputHandler;
import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.Float4Matrix;
import nl.esciencecenter.neon.math.Float4Vector;
import nl.esciencecenter.neon.math.FloatMatrixMath;
import nl.esciencecenter.neon.models.Model;
import nl.esciencecenter.neon.shaders.ShaderProgram;
import nl.esciencecenter.neon.util.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for an Octree datastructure. Think of a cube in space in which you feed
 * elements with a location. When the cube overflows with elements, it
 * subdivides into 8 children Octreenodes. The Octree can be drawn directly.
 * 
 * ATTENTION: After adding elements, the finalize() function should be called to
 * clean up and prepare for drawing. The init() function should also be called
 * before drawing.
 * 
 * @author Maarten van Meersbergen <m.vanmeersbergen@esciencecenter.nl>
 */
public class OctreeNode {
    private final static Logger        logger      = LoggerFactory.getLogger(OctreeNode.class);
    /**
     * The maximum number of elements this node may contain before subdivision
     * occurs.
     */
    protected static final int         maxElements = 1000;
    protected static final int         minDivision = 7;
    /** The center location for this node. */
    protected final Float3Vector       center;
    /** The size of the ribs of the cube this node represents. */
    protected final float              ribSize;
    protected final float              childRibSize;
    /** The depth of this node in the octree. */
    protected final int                depth;
    /** The scale for this node's graphical representation. */
    protected final float              scale;

    /** The stored elements. */
    protected ArrayList<OctreeElement> elements;
    /** The model to be used if this node is drawn. */
    protected Model                    model;
    /** The translation matrix for this node. */
    protected Float4Matrix             TMatrix;
    /** The (potential) child nodes. */
    protected OctreeNode               ppp, ppn, pnp, pnn, npp, npn, nnp, nnn;
    /** Subdivision state holder. */
    protected boolean                  subdivided  = false;
    /** State holder for finalization step. */
    protected boolean                  drawable    = false;
    /** Number of points held by the final object. */
    protected int                      numPoints;

    /** The color for the drawable model. */
    protected Float4Vector             color;

    protected float                    minX, maxX;
    protected float                    minY, maxY;
    protected float                    minZ, maxZ;

    /**
     * Basic constructor for OctreeNode
     * 
     * @param baseModel
     *            The model to use for graphic representations of this node.
     * @param maxElements
     *            The maximum amount of {@link OctreeElement}s for this node.
     * @param depth
     *            The depth for this node in the octree (root = 0).
     * @param corner
     *            The corner location for the lower X, Y, Z values of the cube
     *            represented by this node.
     * @param ribSize
     *            The rib sizes for the cube represented by this node.
     */
    public OctreeNode(Model baseModel, int depth, Float3Vector corner, float ribSize) {
        // super(VertexFormat.POINTS);
        this.model = baseModel;
        this.depth = depth;
        this.center = corner.add(new Float3Vector(.5f * ribSize, .5f * ribSize, .5f * ribSize));
        this.ribSize = ribSize;
        this.childRibSize = ribSize / 2f;
        this.TMatrix = FloatMatrixMath.translate(center);
        this.scale = ribSize / 2f;
        this.elements = new ArrayList<OctreeElement>();
    }

    /**
     * Subdivides this octree node, relocating its {@link OctreeElement}s into
     * the proper children. After this method is called, this Node only acts as
     * a throughput, delegating all calls to its children.
     */
    protected void subdivide() {
        // float childRibSize = ribSize / 2f;
        // int newDepth = depth + 1;
        //
        // ppp = new OctreeNode(model, newDepth, center.add(new Float3Vector(0f,
        // 0f, 0f)), childRibSize);
        // ppn = new OctreeNode(model, newDepth, center.add(new Float3Vector(0f,
        // 0f, -childRibSize)), childRibSize);
        // pnp = new OctreeNode(model, newDepth, center.add(new Float3Vector(0f,
        // -childRibSize, 0f)), childRibSize);
        // pnn = new OctreeNode(model, newDepth, center.add(new Float3Vector(0f,
        // -childRibSize, -childRibSize)),
        // childRibSize);
        // npp = new OctreeNode(model, newDepth, center.add(new
        // Float3Vector(-childRibSize, 0f, 0f)), childRibSize);
        // npn = new OctreeNode(model, newDepth, center.add(new
        // Float3Vector(-childRibSize, 0f, -childRibSize)),
        // childRibSize);
        // nnp = new OctreeNode(model, newDepth, center.add(new
        // Float3Vector(-childRibSize, -childRibSize, 0f)),
        // childRibSize);
        // nnn = new OctreeNode(model, newDepth,
        // center.add(new Float3Vector(-childRibSize, -childRibSize,
        // -childRibSize)), childRibSize);

        for (OctreeElement element : elements) {
            addElementSubdivided(element);
        }

        // this.model = null;
        this.TMatrix = null;
        this.elements = null;

        subdivided = true;
    }

    /**
     * Helper method to determine if the given location falls within the domain
     * of this node. Only needed for the root, to make sure no outside elements
     * are caught.
     * 
     * @param location
     *            The location to check.
     * @return true if the location falls inside the domain of this node.
     */
    private boolean isInThisNodesSpace(Float3Vector location) {
        float halfRibSize = ribSize * 0.5f;

        if ((location.getX() > (center.getX() - halfRibSize)) && (location.getY() > (center.getY() - halfRibSize))
                && (location.getZ() > (center.getZ() - halfRibSize))
                && (location.getX() < (center.getX() + halfRibSize))
                && (location.getY() < (center.getY() + halfRibSize))
                && (location.getZ() < (center.getZ() + halfRibSize))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add an element to this node.
     * 
     * @param element
     *            The {@link OctreeElement} to add to this node.
     */
    public void addElement(OctreeElement element) {
        Float3Vector location = element.getCenter();

        // If this is the root, check if the location of the element is within
        // the domain governed by this octree.
        if (depth > 0 || isInThisNodesSpace(location)) {
            // Check if we are full yet.
            if (!subdivided) {
                if (depth < minDivision) {
                    subdivide();
                } else if (elements.size() > maxElements) {
                    if (depth < Settings.getInstance().getMaxOctreeDepth()) {
                        // If so, subdivide this node.
                        subdivide();
                    } else {
                        // Or generate a warning if there is something wrong.
                        // (insane total number of elements added f.e.)
                        logger.warn("Octree max division reached.");
                    }
                }
            }

            // If this node was already divided, we delegate.
            if (subdivided) {
                addElementSubdivided(element);
            } else {
                elements.add(element);
            }
        } else {
            logger.warn("OctreeElement added that is not within governed domain of this OctreeNode.");
            logger.warn("OctreeElement center at:" + location);
            logger.warn("OctreeNode center at:" + center);
        }
    }

    /**
     * Finalize the addition of new elements to this node. Calculate things we
     * have to calculate for drawing (f.e. color), and clear datastructures.
     * ATTENTION: This is a placeholder, and currently sets the color to
     * transparent white based on population density compared to
     * {@link #maxElements}. Override this.
     */
    public void finalizeAdding(GL3 gl) {
        if (subdivided) {
            if (ppp != null) {
                ppp.finalizeAdding(gl);
            }
            if (ppn != null) {
                ppn.finalizeAdding(gl);
            }
            if (pnp != null) {
                pnp.finalizeAdding(gl);
            }
            if (pnn != null) {
                pnn.finalizeAdding(gl);
            }
            if (npp != null) {
                npp.finalizeAdding(gl);
            }
            if (npn != null) {
                npn.finalizeAdding(gl);
            }
            if (nnp != null) {
                nnp.finalizeAdding(gl);
            }
            if (nnn != null) {
                nnn.finalizeAdding(gl);
            }

            // numPoints += ppp.getNumPoints();
            // numPoints += ppn.getNumPoints();
            // numPoints += pnp.getNumPoints();
            // numPoints += pnn.getNumPoints();
            // numPoints += npp.getNumPoints();
            // numPoints += npn.getNumPoints();
            // numPoints += nnp.getNumPoints();
            // numPoints += nnn.getNumPoints();
            //
            // Float4Vector protoColor = new Float4Vector(0f, 0f, 0f, 0f);
            //
            // protoColor = protoColor.add(ppp.getColor());
            // protoColor = protoColor.add(ppn.getColor());
            // protoColor = protoColor.add(pnp.getColor());
            // protoColor = protoColor.add(pnn.getColor());
            // protoColor = protoColor.add(npp.getColor());
            // protoColor = protoColor.add(npn.getColor());
            // protoColor = protoColor.add(nnp.getColor());
            // protoColor = protoColor.add(nnn.getColor());
            //
            // protoColor = protoColor.div(numPoints);
            //
            // color = new Float4Vector(protoColor.getX(), protoColor.getY(),
            // protoColor.getZ(), 1f);

            drawable = true;
        } else {
            if (elements.size() > 0) {
                // if (elements.size() > 2 *
                // (Settings.getInstance().getMaxOctreeDepth() - depth)) {
                Float3Vector protoColor = new Float3Vector(0f, 0f, 0f);
                for (OctreeElement element : elements) {
                    protoColor = protoColor.add(element.getColor());
                }
                protoColor = protoColor.div(elements.size());

                color = new Float4Vector(protoColor.getX(), protoColor.getY(), protoColor.getZ(), 1f);

                numPoints = elements.size();

                // FloatBuffer verticesBuffer =
                // FloatBuffer.allocate(numPoints * 3);
                // FloatBuffer vertexColorsBuffer =
                // FloatBuffer.allocate(numPoints * 3);
                //
                // for (OctreeElement element : elements) {
                // Float3Vector location = element.getCenter();
                // Float3Vector color = element.getColor();
                // // WRITE DATA XYZ
                // verticesBuffer.put(location.getX());
                // verticesBuffer.put(location.getY());
                // verticesBuffer.put(location.getZ());
                //
                // // RGB
                // vertexColorsBuffer.put(color.getX());
                // vertexColorsBuffer.put(color.getY());
                // vertexColorsBuffer.put(color.getZ());
                // }
                //
                // verticesBuffer.flip();
                // vertexColorsBuffer.flip();
                //
                // GLSLAttribute vertices = new
                // GLSLAttribute(verticesBuffer, "MCvertex",
                // GLSLAttribute.SIZE_FLOAT, 3);
                // GLSLAttribute vertexColors = new
                // GLSLAttribute(vertexColorsBuffer, "MCvertexColor",
                // GLSLAttribute.SIZE_FLOAT, 3);
                //
                // VertexBufferObject vbo = new VertexBufferObject(gl,
                // vertices, vertexColors);
                // setVbo(vbo);
                // setNumVertices(numPoints);

                drawable = true;
            }
            // }
        }

        elements = null;
    }

    protected Float4Vector getColor() {
        if (drawable) {
            return color;
        } else {
            return new Float4Vector();
        }
    }

    protected int getNumPoints() {
        return numPoints;
    }

    /**
     * Add an element to the proper child.
     * 
     * @param element
     *            The element to add.
     */
    public void addElementSubdivided(OctreeElement element) {
        Float3Vector location = element.getCenter();
        if (location.getX() < center.getX()) {
            if (location.getY() < center.getY()) {
                if (location.getZ() < center.getZ()) {
                    if (nnn == null) {
                        nnn = new OctreeNode(model, depth + 1, center.add(new Float3Vector(-childRibSize,
                                -childRibSize, -childRibSize)), childRibSize);

                    }
                    nnn.addElement(element);
                } else {
                    if (nnp == null) {
                        nnp = new OctreeNode(model, depth + 1, center.add(new Float3Vector(-childRibSize,
                                -childRibSize, 0f)), childRibSize);
                    }
                    nnp.addElement(element);
                }
            } else {
                if (location.getZ() < center.getZ()) {
                    if (npn == null) {
                        npn = new OctreeNode(model, depth + 1, center.add(new Float3Vector(-childRibSize, 0f,
                                -childRibSize)), childRibSize);
                    }
                    npn.addElement(element);
                } else {
                    if (npp == null) {
                        npp = new OctreeNode(model, depth + 1, center.add(new Float3Vector(-childRibSize, 0f, 0f)),
                                childRibSize);
                    }
                    npp.addElement(element);
                }
            }
        } else {
            if (location.getY() < center.getY()) {
                if (location.getZ() < center.getZ()) {
                    if (pnn == null) {
                        pnn = new OctreeNode(model, depth + 1, center.add(new Float3Vector(0f, -childRibSize,
                                -childRibSize)), childRibSize);
                    }
                    pnn.addElement(element);
                } else {
                    if (pnp == null) {
                        pnp = new OctreeNode(model, depth + 1, center.add(new Float3Vector(0f, -childRibSize, 0f)),
                                childRibSize);
                    }
                    pnp.addElement(element);
                }
            } else {
                if (location.getZ() < center.getZ()) {
                    if (ppn == null) {
                        ppn = new OctreeNode(model, depth + 1, center.add(new Float3Vector(0f, 0f, -childRibSize)),
                                childRibSize);
                    }
                    ppn.addElement(element);
                } else {
                    if (ppp == null) {
                        ppp = new OctreeNode(model, depth + 1, center.add(new Float3Vector(0f, 0f, 0f)), childRibSize);
                    }
                    ppp.addElement(element);
                }
            }
        }
    }

    /**
     * OpenGL draw method.
     * 
     * @param gl
     *            the current GL instance.
     * @param program
     *            The ShaderProgram to use in the drawing process.
     * @param cameraPosition
     * @param MVMatrix
     *            The global Modelview Matrix.
     * @throws UninitializedException
     *             if this method was called before calling the
     *             {@link #init(GL3)} method.
     */
    public void draw(GL3 gl, ShaderProgram program, Float3Vector cameraPosition) throws UninitializedException {
        // if (initialized) {
        if (subdivided) {
            draw_sorted(gl, program, cameraPosition);
        } else {
            if (drawable) {
                program.setUniformMatrix("TMatrix", TMatrix);
                program.setUniformMatrix("SMatrix", FloatMatrixMath.scale(scale));
                program.setUniformVector("Color", color);

                program.use(gl);

                model.draw(gl, program);

                // try {
                // program.use(gl);
                // } catch (UninitializedException e) {
                // logger.error(e.getMessage());
                // }
                //
                // getVbo().bind(gl);
                //
                // program.linkAttribs(gl, getVbo().getAttribs());
                //
                // gl.glDrawArrays(GL3.GL_POINTS, 0, getNumVertices());
            }
        }
        // } else {
        // throw new UninitializedException();
        // }
    }

    /**
     * Draws this node in the proper sorted order based on the current octant in
     * the {@link InputHandler}
     * 
     * @param gl
     *            the current opengl instance.
     * @param program
     *            the shaderprogram to use in drawing.
     * @param MVMatrix
     *            the global Modelview Matrix.
     */
    protected void draw_sorted(GL3 gl, ShaderProgram program, Float3Vector cameraPosition) {
        // InputHandler inputHandler = InputHandler.getInstance();

        try {
            // if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.NNN) {

            if (ppp != null) {
                ppp.draw(gl, program, cameraPosition);
            }
            if (ppn != null) {
                ppn.draw(gl, program, cameraPosition);
            }
            if (pnp != null) {
                pnp.draw(gl, program, cameraPosition);
            }
            if (pnn != null) {
                pnn.draw(gl, program, cameraPosition);
            }
            if (npp != null) {
                npp.draw(gl, program, cameraPosition);
            }
            if (npn != null) {
                npn.draw(gl, program, cameraPosition);
            }
            if (nnp != null) {
                nnp.draw(gl, program, cameraPosition);
            }
            if (nnn != null) {
                nnn.draw(gl, program, cameraPosition);
            }

            // } else if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.NNP) {
            // ppn.draw(gl, program, MVMatrix);
            //
            // npn.draw(gl, program, MVMatrix);
            // pnn.draw(gl, program, MVMatrix);
            // ppp.draw(gl, program, MVMatrix);
            //
            // nnn.draw(gl, program, MVMatrix);
            // pnp.draw(gl, program, MVMatrix);
            // npp.draw(gl, program, MVMatrix);
            //
            // nnp.draw(gl, program, MVMatrix);
            // } else if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.NPN) {
            // pnp.draw(gl, program, MVMatrix);
            //
            // nnp.draw(gl, program, MVMatrix);
            // ppp.draw(gl, program, MVMatrix);
            // pnn.draw(gl, program, MVMatrix);
            //
            // npp.draw(gl, program, MVMatrix);
            // ppn.draw(gl, program, MVMatrix);
            // nnn.draw(gl, program, MVMatrix);
            //
            // npn.draw(gl, program, MVMatrix);
            // } else if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.NPP) {
            // pnn.draw(gl, program, MVMatrix);
            //
            // nnn.draw(gl, program, MVMatrix);
            // ppn.draw(gl, program, MVMatrix);
            // pnp.draw(gl, program, MVMatrix);
            //
            // npn.draw(gl, program, MVMatrix);
            // ppp.draw(gl, program, MVMatrix);
            // nnp.draw(gl, program, MVMatrix);
            //
            // npp.draw(gl, program, MVMatrix);
            // } else if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.PNN) {
            // npp.draw(gl, program, MVMatrix);
            //
            // ppp.draw(gl, program, MVMatrix);
            // nnp.draw(gl, program, MVMatrix);
            // npn.draw(gl, program, MVMatrix);
            //
            // pnp.draw(gl, program, MVMatrix);
            // nnn.draw(gl, program, MVMatrix);
            // ppn.draw(gl, program, MVMatrix);
            //
            // pnn.draw(gl, program, MVMatrix);
            // } else if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.PNP) {
            // npn.draw(gl, program, MVMatrix);
            //
            // ppn.draw(gl, program, MVMatrix);
            // nnn.draw(gl, program, MVMatrix);
            // npp.draw(gl, program, MVMatrix);
            //
            // pnn.draw(gl, program, MVMatrix);
            // nnp.draw(gl, program, MVMatrix);
            // ppp.draw(gl, program, MVMatrix);
            //
            // pnp.draw(gl, program, MVMatrix);
            // } else if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.PPN) {
            // nnp.draw(gl, program, MVMatrix);
            //
            // pnp.draw(gl, program, MVMatrix);
            // npp.draw(gl, program, MVMatrix);
            // nnn.draw(gl, program, MVMatrix);
            //
            // ppp.draw(gl, program, MVMatrix);
            // npn.draw(gl, program, MVMatrix);
            // pnn.draw(gl, program, MVMatrix);
            //
            // ppn.draw(gl, program, MVMatrix);
            // } else if (inputHandler.getCurrentViewOctant() ==
            // InputHandler.octants.PPP) {
            // nnn.draw(gl, program, MVMatrix);
            //
            // pnn.draw(gl, program, MVMatrix);
            // npn.draw(gl, program, MVMatrix);
            // nnp.draw(gl, program, MVMatrix);
            //
            // ppn.draw(gl, program, MVMatrix);
            // npp.draw(gl, program, MVMatrix);
            // pnp.draw(gl, program, MVMatrix);
            //
            // ppp.draw(gl, program, MVMatrix);
            // }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }
}
