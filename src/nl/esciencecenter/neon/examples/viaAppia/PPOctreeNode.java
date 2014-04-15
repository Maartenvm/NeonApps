package nl.esciencecenter.neon.examples.viaAppia;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.Float4Vector;
import nl.esciencecenter.neon.util.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PPOctreeNode {
    private final static Logger        logger      = LoggerFactory.getLogger(PPOctreeNode.class);
    /**
     * The maximum number of elements this node may contain before subdivision
     * occurs.
     */
    protected static final int         maxElements = 100000;
    protected static final int         minDivision = 3;
    /** The center location for this node. */
    protected final Float3Vector       center;
    /** The size of the ribs of the cube this node represents. */
    protected final float              ribSize;
    /** The depth of this node in the octree. */
    protected final int                depth;
    /** The scale for this node's graphical representation. */
    protected final float              scale;

    /** The stored elements. */
    protected ArrayList<OctreeElement> elements;
    /** The (potential) child nodes. */
    protected PPOctreeNode             ppp, ppn, pnp, pnn, npp, npn, nnp, nnn;
    /** Subdivision state holder. */
    protected boolean                  subdivided  = false;
    /** Number of points held by the final object. */
    protected int                      numPoints;

    /** The color for the drawable model. */
    protected Float4Vector             color;

    protected float                    minX, maxX;
    protected float                    minY, maxY;
    protected float                    minZ, maxZ;

    public PPOctreeNode(int depth, Float3Vector corner, float ribSize) {
        this.depth = depth;
        this.center = corner.add(new Float3Vector(.5f * ribSize, .5f * ribSize, .5f * ribSize));
        this.ribSize = ribSize;
        this.scale = ribSize / 2f;
        this.elements = new ArrayList<OctreeElement>();

    }

    /**
     * Subdivides this octree node, relocating its {@link OctreeElement}s into
     * the proper children. After this method is called, this Node only acts as
     * a throughput, delegating all calls to its children.
     */
    protected void subdivide() {
        for (OctreeElement element : elements) {
            addElementSubdivided(element);
        }

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
        if ((location.getX() > (center.getX() - scale)) && (location.getY() > (center.getY() - scale))
                && (location.getZ() > (center.getZ() - scale)) && (location.getX() < (center.getX() + scale))
                && (location.getY() < (center.getY() + scale)) && (location.getZ() < (center.getZ() + scale))) {
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
                        nnn = new PPOctreeNode(depth + 1, center.add(new Float3Vector(-scale, -scale, -scale)), scale);

                    }
                    nnn.addElement(element);
                } else {
                    if (nnp == null) {
                        nnp = new PPOctreeNode(depth + 1, center.add(new Float3Vector(-scale, -scale, 0f)), scale);
                    }
                    nnp.addElement(element);
                }
            } else {
                if (location.getZ() < center.getZ()) {
                    if (npn == null) {
                        npn = new PPOctreeNode(depth + 1, center.add(new Float3Vector(-scale, 0f, -scale)), scale);
                    }
                    npn.addElement(element);
                } else {
                    if (npp == null) {
                        npp = new PPOctreeNode(depth + 1, center.add(new Float3Vector(-scale, 0f, 0f)), scale);
                    }
                    npp.addElement(element);
                }
            }
        } else {
            if (location.getY() < center.getY()) {
                if (location.getZ() < center.getZ()) {
                    if (pnn == null) {
                        pnn = new PPOctreeNode(depth + 1, center.add(new Float3Vector(0f, -scale, -scale)), scale);
                    }
                    pnn.addElement(element);
                } else {
                    if (pnp == null) {
                        pnp = new PPOctreeNode(depth + 1, center.add(new Float3Vector(0f, -scale, 0f)), scale);
                    }
                    pnp.addElement(element);
                }
            } else {
                if (location.getZ() < center.getZ()) {
                    if (ppn == null) {
                        ppn = new PPOctreeNode(depth + 1, center.add(new Float3Vector(0f, 0f, -scale)), scale);
                    }
                    ppn.addElement(element);
                } else {
                    if (ppp == null) {
                        ppp = new PPOctreeNode(depth + 1, center.add(new Float3Vector(0f, 0f, 0f)), scale);
                    }
                    ppp.addElement(element);
                }
            }
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
        } else {
            Float3Vector protoColor = new Float3Vector(0f, 0f, 0f);
            for (OctreeElement element : elements) {
                protoColor = protoColor.add(element.getColor());
            }
            protoColor = protoColor.div(elements.size());

            color = new Float4Vector(protoColor.getX(), protoColor.getY(), protoColor.getZ(), 1f);

            numPoints = elements.size();
        }

        elements = null;
    }

}
