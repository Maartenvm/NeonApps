package nl.esciencecenter.neon.examples.viaAppia.las;

import java.nio.channels.FileChannel;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.datastructures.VertexBufferObject;
import nl.esciencecenter.neon.examples.viaAppia.OctreeNode;

public interface LASPointDataRecord {

    public int getSizePerRecord();

    public int getNumPoints();

    public VertexBufferObject readPoints(GL3 gl, FileChannel recordsBlock, long offset, int skip,
            BoundingBox overallBoundingBox);

    void addPointsToOctree(FileChannel recordsBlock, long offset, OctreeNode root, BoundingBox overallBoundingBox);
}
