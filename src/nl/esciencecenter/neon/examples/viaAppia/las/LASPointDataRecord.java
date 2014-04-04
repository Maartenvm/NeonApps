package nl.esciencecenter.neon.examples.viaAppia.las;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.datastructures.VertexBufferObject;

public interface LASPointDataRecord {
    public VertexBufferObject readPoints(GL3 gl, ByteBuffer recordsBlock, int skip, BoundingBox overallBoundingBox);

    public int getSizePerRecord();

    public int getNumPoints();

    public VertexBufferObject readPoints(GL3 gl, FileChannel recordsBlock, long offset, int skip,
            BoundingBox overallBoundingBox);
}
