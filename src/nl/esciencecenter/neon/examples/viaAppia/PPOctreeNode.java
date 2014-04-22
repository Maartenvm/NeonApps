package nl.esciencecenter.neon.examples.viaAppia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.Float4Vector;
import nl.esciencecenter.neon.util.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PPOctreeNode {
    private final static Logger  logger          = LoggerFactory.getLogger(PPOctreeNode.class);
    /**
     * The maximum number of elements this node may contain before subdivision
     * occurs.
     */
    protected static final int   maxElements     = 1000;
    protected static final int   minDivision     = 7;
    /** The center location for this node. */
    protected final Float3Vector center;
    /** The size of the ribs of the cube this node represents. */
    protected final float        ribSize;
    /** The depth of this node in the octree. */
    protected final int          depth;
    /** The scale for this node's graphical representation. */
    protected final float        scale;

    /** The stored elements. */
    protected RandomAccessFile   elements;
    protected int                elementsWritten = 0;
    protected String             path, filename;
    protected FileChannel        inChannel;

    /** The (potential) child nodes. */
    protected PPOctreeNode       ppp, ppn, pnp, pnn, npp, npn, nnp, nnn;
    /** Subdivision state holder. */
    protected boolean            subdivided      = false;
    /** Number of points held by the final object. */
    protected int                numPoints;

    /** The color for the drawable model. */
    protected Float4Vector       color;

    protected float              minX, maxX;
    protected float              minY, maxY;
    protected float              minZ, maxZ;

    public PPOctreeNode(String path, int depth, Float3Vector corner, float ribSize) {
        this.path = path;
        this.filename = path + "data/" + depth + "-" + corner + ".oct";
        this.depth = depth;
        this.center = corner.add(new Float3Vector(.5f * ribSize, .5f * ribSize, .5f * ribSize));
        this.ribSize = ribSize;
        this.scale = ribSize / 2f;
        try {
            new File(new File(filename).getParent()).mkdirs();
            File f = new File(filename);
            f.createNewFile();
            long length = f.length();
            this.elements = new RandomAccessFile(filename, "rw");
            elements.seek(length);

            this.inChannel = elements.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Subdivides this octree node, relocating its {@link OctreeElement}s into
     * the proper children. After this method is called, this Node only acts as
     * a throughput, delegating all calls to its children.
     * 
     * @throws IOException
     */
    protected void subdivide() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(6 * Float.SIZE);
        inChannel.position(0);

        int numread = 0;
        while (numread > 0) {
            numread = inChannel.read(buf);
            if (numread != buf.capacity()) {
                logger.error("read weird number of bytes");
                System.exit(1);
            }
            buf.flip();
            OctreeElement element = new OctreeElement(new Float3Vector(buf.getFloat(), buf.getFloat(), buf.getFloat()),
                    new Float3Vector(buf.getFloat(), buf.getFloat(), buf.getFloat()));
            addElementSubdivided(element);
        }

        inChannel.close();
        elements.close();

        Files.deleteIfExists(new File(filename).toPath());

        subdivided = true;
    }

    protected int getNumLeaves() {
        int result = 0;
        if (subdivided) {
            if (ppp != null) {
                result += ppp.getNumLeaves();
            }
            if (ppn != null) {
                result += ppn.getNumLeaves();
            }
            if (pnp != null) {
                result += pnp.getNumLeaves();
            }
            if (pnn != null) {
                result += pnn.getNumLeaves();
            }
            if (npp != null) {
                result += npp.getNumLeaves();
            }
            if (npn != null) {
                result += npn.getNumLeaves();
            }
            if (nnp != null) {
                result += nnp.getNumLeaves();
            }
            if (nnn != null) {
                result += nnn.getNumLeaves();
            }
        } else {
            result = 1;
        }
        return result;
    }

    /**
     * Finalize the addition of new elements to this node. Calculate things we
     * have to calculate for drawing (f.e. color), and clear datastructures.
     * ATTENTION: This is a placeholder, and currently sets the color to
     * transparent white based on population density compared to
     * {@link #maxElements}. Override this.
     * 
     * @throws IOException
     */
    public void finalizeAdding(GL3 gl) throws IOException {
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
            inChannel.close();
            elements.close();
        }
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
     * @throws IOException
     */
    public void addElement(OctreeElement element) throws IOException {
        Float3Vector location = element.getCenter();

        // If this is the root, check if the location of the element is within
        // the domain governed by this octree.
        if (depth > 0 || isInThisNodesSpace(location)) {
            // Check if we are full yet.
            if (!subdivided) {
                if (depth < minDivision) {
                    subdivide();
                } else if (elementsWritten > maxElements) {
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
                ByteBuffer buf = ByteBuffer.allocate(6 * Float.SIZE);
                buf.putFloat(element.getCenter().getX());
                buf.putFloat(element.getCenter().getY());
                buf.putFloat(element.getCenter().getZ());
                buf.putFloat(element.getColor().getX());
                buf.putFloat(element.getColor().getY());
                buf.putFloat(element.getColor().getZ());
                buf.flip();

                while (buf.hasRemaining()) {
                    inChannel.write(buf);
                }
                elementsWritten++;
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
     * @throws IOException
     */
    public void addElementSubdivided(OctreeElement element) throws IOException {
        Float3Vector location = element.getCenter();
        if (location.getX() < center.getX()) {
            if (location.getY() < center.getY()) {
                if (location.getZ() < center.getZ()) {
                    if (nnn == null) {
                        nnn = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(-scale, -scale, -scale)),
                                scale);

                    }
                    nnn.addElement(element);
                } else {
                    if (nnp == null) {
                        nnp = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(-scale, -scale, 0f)), scale);
                    }
                    nnp.addElement(element);
                }
            } else {
                if (location.getZ() < center.getZ()) {
                    if (npn == null) {
                        npn = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(-scale, 0f, -scale)), scale);
                    }
                    npn.addElement(element);
                } else {
                    if (npp == null) {
                        npp = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(-scale, 0f, 0f)), scale);
                    }
                    npp.addElement(element);
                }
            }
        } else {
            if (location.getY() < center.getY()) {
                if (location.getZ() < center.getZ()) {
                    if (pnn == null) {
                        pnn = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(0f, -scale, -scale)), scale);
                    }
                    pnn.addElement(element);
                } else {
                    if (pnp == null) {
                        pnp = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(0f, -scale, 0f)), scale);
                    }
                    pnp.addElement(element);
                }
            } else {
                if (location.getZ() < center.getZ()) {
                    if (ppn == null) {
                        ppn = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(0f, 0f, -scale)), scale);
                    }
                    ppn.addElement(element);
                } else {
                    if (ppp == null) {
                        ppp = new PPOctreeNode(path, depth + 1, center.add(new Float3Vector(0f, 0f, 0f)), scale);
                    }
                    ppp.addElement(element);
                }
            }
        }
    }

}
