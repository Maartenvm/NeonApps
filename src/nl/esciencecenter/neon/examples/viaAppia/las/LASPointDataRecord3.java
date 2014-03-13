package nl.esciencecenter.neon.examples.viaAppia.las;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.datastructures.GLSLAttribute;
import nl.esciencecenter.neon.datastructures.VertexBufferObject;

public class LASPointDataRecord3 implements LASPointDataRecord {
    public static int RECORD_SIZE = 34;
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

    /*
     * GPS Time: The GPS Time is the double floating point time tag value at
     * which the point was acquired. It is GPS Week Time if the Global Encoding
     * low bit is clear and POSIX Time if the Global Encoding low bit is set
     * (see Global Encoding in the Public Header Block description)
     */
    private double GPSTime;

    /*
     * Red: The Red image channel value associated with this point
     */
    private short Red;

    /*
     * Green: The Green image channel value associated with this point
     */
    private short Green;

    /*
     * Blue: The Blue image channel value associated with this point
     */
    private short Blue;

    private final int numrecords;
    private int numPoints;

    private final LASPublicHeader publicHeader;

    public LASPointDataRecord3(int numrecords, LASPublicHeader publicheader) {
        this.numrecords = numrecords;
        this.publicHeader = publicheader;
    }

    @Override
    public VertexBufferObject readPoints(GL3 gl, ByteBuffer recordsBlock, int skip, BoundingBox overallBoundingBox) {
        recordsBlock.order(ByteOrder.LITTLE_ENDIAN);
        recordsBlock.flip();

        numPoints = (int) Math.ceil(numrecords / skip) + 1;
        // System.out.println("numPoints: " + numPoints + " : " + numPoints *
        // 3);

        FloatBuffer verticesBuffer = FloatBuffer.allocate(numPoints * 3);
        FloatBuffer vertexColorsBuffer = FloatBuffer.allocate(numPoints * 3);

        double scaleFactorX = publicHeader.getXscalefactor();
        double scaleFactorY = publicHeader.getYscalefactor();
        double scaleFactorZ = publicHeader.getZscalefactor();

        double offsetX = publicHeader.getXoffset();
        double offsetY = publicHeader.getYoffset();
        double offsetZ = publicHeader.getZoffset();

        double minX = overallBoundingBox.getMinX();
        double minY = overallBoundingBox.getMinY();
        double minZ = overallBoundingBox.getMinZ();

        double maxX = overallBoundingBox.getMaxX();
        double maxY = overallBoundingBox.getMaxY();
        double maxZ = overallBoundingBox.getMaxZ();

        double diffX = maxX - minX;
        double diffY = maxY - minY;
        double diffZ = maxZ - minZ;

        double offset2X = offsetX - minX;
        double offset2Y = offsetY - minY;
        double offset2Z = offsetZ - minZ;

        double offsetdivX = (offset2X / diffX) - 0.5;
        double offsetdivY = (offset2Y / diffX) - 0.5;
        double offsetdivZ = (offset2Z / diffX);

        int count = 0;
        int i = 0;

        double avgX = minX + (0.5 * diffX);
        double graphicOffsetX = avgX;

        // System.out.println("minX : " + minX);
        // System.out.println("diffX: " + diffX);
        // System.out.println("avgX : " + avgX);
        // System.out.println("graphicOffset: " + graphicOffsetX);

        try {
            for (i = 0; i < numrecords; i++) {
                // READ DATA
                int rawX = recordsBlock.getInt();
                int rawY = recordsBlock.getInt();
                int rawZ = recordsBlock.getInt();

                // Skip all unneeded values in input buffer
                recordsBlock.position(recordsBlock.position() + 16);

                byte rlow = recordsBlock.get();
                byte rhigh = recordsBlock.get();
                short rint16 = (short) (((rlow & 0xFF) << 8) | (rhigh & 0xFF));

                byte glow = recordsBlock.get();
                byte ghigh = recordsBlock.get();
                short gint16 = (short) (((glow & 0xFF) << 8) | (ghigh & 0xFF));

                byte blow = recordsBlock.get();
                byte bhigh = recordsBlock.get();
                short bint16 = (short) (((blow & 0xFF) << 8) | (bhigh & 0xFF));

                if (count == skip) {
                    double processedX = (((((rawX * scaleFactorX) + offsetX) - minX) / diffX) - 0.5) * 2.0;
                    double processedY = (((((rawY * scaleFactorY) + offsetY) - minY) / diffX) - 0.5) * 2.0;
                    double processedZ = ((((rawZ * scaleFactorZ) + offsetZ) - minZ) / diffX) * 2.0;

                    // WRITE DATA
                    verticesBuffer.put((float) processedX);
                    verticesBuffer.put((float) processedY);
                    verticesBuffer.put((float) processedZ);

                    // RGB
                    vertexColorsBuffer.put(rint16);
                    vertexColorsBuffer.put(gint16);
                    vertexColorsBuffer.put(bint16);

                    count = 0;
                }
                count++;
            }
        } catch (BufferOverflowException e) {
            System.out.println("vertices pos: " + verticesBuffer.position() + "/" + verticesBuffer.capacity());
            System.out.println("colors   pos: " + vertexColorsBuffer.position() + "/" + vertexColorsBuffer.capacity());
            System.out.println("numPoints: " + i);
        }

        verticesBuffer.flip();
        vertexColorsBuffer.flip();

        GLSLAttribute vertices = new GLSLAttribute(verticesBuffer, "MCvertex", GLSLAttribute.SIZE_FLOAT, 3);
        GLSLAttribute vertexColors = new GLSLAttribute(vertexColorsBuffer, "MCvertexColor", GLSLAttribute.SIZE_FLOAT, 3);

        return new VertexBufferObject(gl, vertices, vertexColors);
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