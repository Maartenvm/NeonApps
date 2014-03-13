package nl.esciencecenter.neon.examples.viaAppia.las;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.datastructures.GLSLAttribute;
import nl.esciencecenter.neon.datastructures.VertexBufferObject;

public class LASPointDataRecord0 implements LASPointDataRecord {
    public static int RECORD_SIZE = 20;

    /*
     * X, Y, and Z: The X, Y, and Z values are stored as long integers. The X,
     * Y, and Z values are used in conjunction with the scale values and the
     * offset values to determine the coordinate for each point as described in
     * the Public Header Block section.
     */
    private int X;
    private int Y;
    private int Z;
    /*
     * Intensity: The intensity value is the integer representation of the pulse
     * return magnitude. This value is optional and system specific. However, it
     * should always be included if available.
     */
    private short Intensity;

    /*
     * NOTE: The following four fields (Return Number, Number of Returns, Scan
     * Direction Flag and Edge of Flight Line) are bit fields within a single
     * byte.
     * 
     * Return Number: The Return Number is the pulse return number for a given
     * output pulse. A given output laser pulse can have many returns, and they
     * must be marked in sequence of return. The first return will have a Return
     * Number of one, the second a Return Number of two, and so on up to five
     * returns.
     * 
     * Number of Returns (for this emitted pulse): The Number of Returns is the
     * total number of returns for a given pulse. For example, a laser data
     * point may be return two (Return Number) within a total number of five
     * returns.
     * 
     * Scan Direction Flag: The Scan Direction Flag denotes the direction at
     * which the scanner mirror was traveling at the time of the output pulse. A
     * bit value of 1 is a positive scan direction, and a bit value of 0 is a
     * negative scan direction (where positive scan direction is a scan moving
     * from the left side of the in-track direction to the right side and
     * negative the opposite).
     * 
     * Edge of Flight Line: The Edge of Flight Line data bit has a value of 1
     * only when the point is at the end of a scan. It is the last point on a
     * given scan line before it changes direction.
     */
    private byte misc;

    /*
     * Classification: Classification in LAS 1.0 was essentially user defined
     * and optional. LAS 1.1 defines a standard set of ASPRS classifications. In
     * addition, the field is now mandatory. If a point has never been
     * classified, this byte must be set to zero. There are no user defined
     * classes since both point format 0 and point format 1 supply 8 bits per
     * point for user defined operations.
     */
    private byte Classification;

    /*
     * Scan Angle Rank: The Scan Angle Rank is a signed one-byte number with a
     * valid range from -90 to +90. The Scan Angle Rank is the angle (rounded to
     * the nearest integer in the absolute value sense) at which the laser point
     * was output from the laser system including the roll of the aircraft. The
     * scan angle is within 1 degree of accuracy from +90 to –90 degrees. The
     * scan angle is an angle based on 0 degrees being nadir, and –90 degrees to
     * the left side of the aircraft in the direction of flight.
     */

    private byte ScanAngleRank;
    /*
     * User Data: This field may be used at the user’s discretion.
     */

    private byte UserData;
    /*
     * Point Source ID: This value indicates the file from which this point
     * originated.
     */
    private short PointSource;

    private final int numrecords;
    private int numPoints;

    public LASPointDataRecord0(int numrecords) {
        this.numrecords = numrecords;
    }

    @Override
    public VertexBufferObject readPoints(GL3 gl, ByteBuffer recordsBlock, int skip, BoundingBox bbox) {
        recordsBlock.order(ByteOrder.LITTLE_ENDIAN);

        numPoints = (int) Math.ceil(numrecords / skip) + 1;

        FloatBuffer verticesBuffer = FloatBuffer.allocate(numPoints * 3);

        int count = 0;
        int i = 0;
        for (i = 0; i < numrecords; i++) {
            float x = recordsBlock.getInt();
            float y = recordsBlock.getInt();
            float z = recordsBlock.getInt();
            // Skip all unneeded values in input buffer
            recordsBlock.position(recordsBlock.position() + 8);

            if (count == skip) {
                // X
                verticesBuffer.put(x);
                // Y
                verticesBuffer.put(y);
                // Z
                verticesBuffer.put(z);

                count = 0;
            }
            count++;
        }

        GLSLAttribute vertices = new GLSLAttribute(verticesBuffer, "MCvertex", GLSLAttribute.SIZE_FLOAT, 3);

        return new VertexBufferObject(gl, vertices);
    }

    @Override
    public int getSizePerRecord() {
        return RECORD_SIZE;
    }

    @Override
    public int getNumPoints() {
        return numPoints;
    }

}
