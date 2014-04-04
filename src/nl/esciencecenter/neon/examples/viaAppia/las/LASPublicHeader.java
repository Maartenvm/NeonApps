package nl.esciencecenter.neon.examples.viaAppia.las;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LASPublicHeader {
    public static int HEADER_SIZE = 227;

    // http://www.asprs.org/a/society/committees/standards/asprs_las_format_v12.pdf
    // Any field in the Public Header Block that is not required and is not used
    // must be zero filled

    /*
     * File Signature: The file signature must contain the four characters
     * “LASF”, and it is required by the LAS specification. These four
     * characters can be checked by user software as a quick look initial
     * determination of file type.
     */
    private final String FileSignature = "LASF";

    /*
     * File Source ID (Flight Line Number if this file was derived from an
     * original flight line): This field should be set to a value between 1 and
     * 65,535, inclusive. A value of zero (0) is interpreted to mean that an ID
     * has not been assigned. In this case, processing software is free to
     * assign any valid number.
     */
    private final short FileSourceID;

    // Global Encoding: This is a bit field used to indicate certain global
    // properties about the file
    private final short GlobalEncoding;

    /*
     * Project ID (GUID data): The four fields that comprise a complete Globally
     * Unique Identifier (GUID) are now reserved for use as a Project Identifier
     * (Project ID). The field remains optional.
     */
    private final int ProjectIDGUIDdata1;
    private final short ProjectIDGUIDdata2;
    private final short ProjectIDGUIDdata3;
    private final String ProjectIDGUIDdata4;

    /*
     * Version Number: The version number consists of a major and minor field.
     * The major and minor fields combine to form the number that indicates the
     * format number of the current specification itself. For example,
     * specification number 1.2 (this version) would contain 1 in the major
     * field and 2 in the minor field.
     */
    private final byte VersionMajor;
    private final byte VersionMinor;

    /*
     * System Identifier: The version 1.0 specification assumes that LAS files
     * are exclusively generated as a result of collection by a hardware sensor.
     */
    private final String SystemIdentifier;

    /*
     * Generating Software: This information is ASCII data describing the
     * generating software itself.
     */
    private final String GeneratingSoftware;

    /*
     * File Creation Day of Year: Day, expressed as an unsigned short, on which
     * this file was created. Day is computed as the Greenwich Mean Time (GMT)
     * day. January 1 is considered day 1.
     */
    private final short FileCreationDayofYear;
    /*
     * File Creation Year: The year, expressed as a four digit number, in which
     * the file was created.
     */
    private final short FileCreationYear;

    /*
     * Header Size: The size, in bytes, of the Public Header Block itself.
     */
    private final short HeaderSize;

    /*
     * Offset to point data: The actual number of bytes from the beginning of
     * the file to the first field of the first point record data field.
     */
    private final int Offsettopointdata;

    /*
     * Number of Variable Length Records: This field contains the current number
     * of Variable Length Records.
     */
    private final int NumberofVariableLengthRecords;

    /*
     * Point Data Format ID: The point data format ID corresponds to the point
     * data record format type. LAS 1.2 defines types 0, 1, 2 and 3.
     */
    private final byte PointDataFormatID;

    /*
     * Point Data Record Length: The size, in bytes, of the Point Data Record
     */
    private final short PointDataRecordLength;

    /*
     * Number of point records: This field contains the total number of point
     * records within the file
     */
    private final int Numberofpointrecords;

    /*
     * Number of points by return: This field contains an array of the total
     * point records per return. The first unsigned long value will be the total
     * number of records from the first return, and the second contains the
     * total number for return two, and so forth up to five returns
     */
    private final int[] Numberofpointsbyreturn;

    /*
     * X, Y, and Z scale factors: The scale factor fields contain a double
     * floating point value that is used to scale the corresponding X, Y, and Z
     * long values within the point records. The corresponding X, Y, and Z scale
     * factor must be multiplied by the X, Y, or Z point record value to get the
     * actual X, Y, or Z coordinate. For example, if the X, Y, and Z coordinates
     * are intended to have two decimal point values, then each scale factor
     * will contain the number 0.01.
     */
    private final double Xscalefactor;
    private final double Yscalefactor;
    private final double Zscalefactor;

    /*
     * X, Y, and Z offset: The offset fields should be used to set the overall
     * offset for the point records. In general these numbers will be zero, but
     * for certain cases the resolution of the point data may not be large
     * enough for a given projection system. However, it should always be
     * assumed that these numbers are used. So to scale a given X from the point
     * record, take the point record X multiplied by the X scale factor, and
     * then add the X offset.
     * 
     * Xcoordinate = (Xrecord * Xscale) + Xoffset;
     * 
     * Ycoordinate = (Yrecord * Yscale) + Yoffset;
     * 
     * Zcoordinate = (Zrecord * Zscale) + Zoffset;
     */
    private final double Xoffset;
    private final double Yoffset;
    private final double Zoffset;

    /*
     * Max and Min X, Y, Z: The max and min data fields are the actual unscaled
     * extents of the LAS point file data, specified in the coordinate system of
     * the LAS data.
     */
    private final double MaxX;
    private final double MinX;
    private final double MaxY;
    private final double MinY;
    private final double MaxZ;
    private final double MinZ;

    /*
     * The projection information for the point data is required for all data.
     * The projection information will be placed in the Variable Length Records.
     * Placing the projection information within the Variable Length Records
     * allows for any projection to be defined including custom projections. The
     * GeoTIff specification http://www.remotesensing.org/geotiff/geotiff.html
     * is the model for representing the projection information, and the format
     * is explicitly defined by this specification.
     */

    boolean acceptableHeader = false;

    public LASPublicHeader(ByteBuffer headerBlock) {
        headerBlock.order(ByteOrder.LITTLE_ENDIAN);
        headerBlock.flip();

        String readFileSignature = readStringfromByteBuffer(headerBlock, 4);

        FileSourceID = headerBlock.getShort();

        GlobalEncoding = headerBlock.getShort();

        ProjectIDGUIDdata1 = headerBlock.getInt();
        ProjectIDGUIDdata2 = headerBlock.getShort();
        ProjectIDGUIDdata3 = headerBlock.getShort();
        ProjectIDGUIDdata4 = readStringfromByteBuffer(headerBlock, 8);

        VersionMajor = headerBlock.get();
        VersionMinor = headerBlock.get();

        SystemIdentifier = readStringfromByteBuffer(headerBlock, 32);

        GeneratingSoftware = readStringfromByteBuffer(headerBlock, 32);

        FileCreationDayofYear = headerBlock.getShort();

        FileCreationYear = headerBlock.getShort();

        HeaderSize = headerBlock.getShort();

        Offsettopointdata = headerBlock.getInt();

        NumberofVariableLengthRecords = headerBlock.getInt();

        PointDataFormatID = headerBlock.get();

        PointDataRecordLength = headerBlock.getShort();

        Numberofpointrecords = headerBlock.getInt();

        Numberofpointsbyreturn = new int[] { headerBlock.getInt(), headerBlock.getInt(), headerBlock.getInt(),
                headerBlock.getInt(), headerBlock.getInt() };

        Xscalefactor = headerBlock.getDouble();
        Yscalefactor = headerBlock.getDouble();
        Zscalefactor = headerBlock.getDouble();

        Xoffset = headerBlock.getDouble();
        Yoffset = headerBlock.getDouble();
        Zoffset = headerBlock.getDouble();

        MaxX = headerBlock.getDouble();
        MinX = headerBlock.getDouble();
        MaxY = headerBlock.getDouble();
        MinY = headerBlock.getDouble();
        MaxZ = headerBlock.getDouble();
        MinZ = headerBlock.getDouble();

        if (readFileSignature.compareTo(FileSignature) == 0) {
            acceptableHeader = true;
        }
    }

    private String readStringfromByteBuffer(ByteBuffer buffer, int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result += (char) buffer.get();
        }
        return result;
    }

    public String getFileSignature() {
        return FileSignature;
    }

    public short getFileSourceID() {
        return FileSourceID;
    }

    public short getGlobalEncoding() {
        return GlobalEncoding;
    }

    public int getProjectIDGUIDdata1() {
        return ProjectIDGUIDdata1;
    }

    public short getProjectIDGUIDdata2() {
        return ProjectIDGUIDdata2;
    }

    public short getProjectIDGUIDdata3() {
        return ProjectIDGUIDdata3;
    }

    public String getProjectIDGUIDdata4() {
        return ProjectIDGUIDdata4;
    }

    public byte getVersionMajor() {
        return VersionMajor;
    }

    public byte getVersionMinor() {
        return VersionMinor;
    }

    public String getSystemIdentifier() {
        return SystemIdentifier;
    }

    public String getGeneratingSoftware() {
        return GeneratingSoftware;
    }

    public short getFileCreationDayofYear() {
        return FileCreationDayofYear;
    }

    public short getFileCreationYear() {
        return FileCreationYear;
    }

    public short getHeaderSize() {
        return HeaderSize;
    }

    public int getOffsettopointdata() {
        return Offsettopointdata;
    }

    public int getNumberofVariableLengthRecords() {
        return NumberofVariableLengthRecords;
    }

    public byte getPointDataFormatID() {
        return PointDataFormatID;
    }

    public short getPointDataRecordLength() {
        return PointDataRecordLength;
    }

    public int getNumberofpointrecords() {
        return Numberofpointrecords;
    }

    public int[] getNumberofpointsbyreturn() {
        return Numberofpointsbyreturn;
    }

    public double getXscalefactor() {
        return Xscalefactor;
    }

    public double getYscalefactor() {
        return Yscalefactor;
    }

    public double getZscalefactor() {
        return Zscalefactor;
    }

    public double getXoffset() {
        return Xoffset;
    }

    public double getYoffset() {
        return Yoffset;
    }

    public double getZoffset() {
        return Zoffset;
    }

    public double getMaxX() {
        return MaxX;
    }

    public double getMinX() {
        return MinX;
    }

    public double getMaxY() {
        return MaxY;
    }

    public double getMinY() {
        return MinY;
    }

    public double getMaxZ() {
        return MaxZ;
    }

    public double getMinZ() {
        return MinZ;
    }

    public boolean isAcceptableHeader() {
        return acceptableHeader;
    }

    @Override
    public String toString() {
        String result = "";
        result += "file signature:             " + FileSignature + "\n";
        result += "file source ID:             " + FileSourceID + "\n";
        result += "global_encoding:            " + GlobalEncoding + "\n";
        result += "project ID GUID data 1-4:   " + ProjectIDGUIDdata1 + "-" + ProjectIDGUIDdata2 + "-"
                + ProjectIDGUIDdata3 + "-" + ProjectIDGUIDdata4 + "\n";
        result += "version major.minor:        " + VersionMajor + "." + VersionMinor + "\n";
        result += "system identifier:          " + SystemIdentifier + "\n";
        result += "generating software:        " + GeneratingSoftware + "\n";
        result += "file creation day/year:     " + FileCreationDayofYear + "/" + FileCreationYear + "\n";
        result += "header size:                " + HeaderSize + "\n";
        result += "offset to point data:       " + Offsettopointdata + "\n";
        result += "number var. length records: " + NumberofVariableLengthRecords + "\n";
        result += "point data format:          " + PointDataFormatID + "\n";
        result += "point data record length:   " + PointDataRecordLength + "\n";
        result += "number of point records:    " + Numberofpointrecords + "\n";
        result += "number of points by return: " + Numberofpointsbyreturn + "\n";
        result += "scale factor x y z:         " + Xscalefactor + " " + Yscalefactor + " " + Zscalefactor + "\n";
        result += "offset x y z:               " + Xoffset + " " + Yoffset + " " + Zoffset + "\n";
        result += "min x y z:                  " + MinX + " " + MinY + " " + MinZ + "\n";
        result += "max x y z:                  " + MaxX + " " + MaxY + " " + MaxZ + "\n";
        return result;
    }
}
