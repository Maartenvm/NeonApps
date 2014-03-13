package nl.esciencecenter.neon.examples.viaAppia.las;

import java.nio.ByteBuffer;

public class LASVariableLengthRecord {
    private final short Reserved;
    /*
     * User ID: The User ID field is ASCII character data that identifies the
     * user which created the variable length record.
     */
    private final String UserID;

    /*
     * Record ID: The Record ID is dependent upon the User ID. There can be 0 to
     * 65535 Record IDs for every User ID
     */
    private final short RecordID;

    /*
     * Record Length after Header: The record length is the number of bytes for
     * the record after the end of the standard part of the header. Thus the
     * entire record length is 54 bytes (the header size in version 1.2) plus
     * the number of bytes in the variable length portion of the record.
     */
    private final short RecordLengthAfterHeader;
    /*
     * Description: Optional, null terminated text description of the data. Any
     * remaining characters not used must be null.
     */
    private final String Description;

    public LASVariableLengthRecord(ByteBuffer variableLenghtHeaderBlock) {
        Reserved = variableLenghtHeaderBlock.getShort();
        UserID = readStringfromByteBuffer(variableLenghtHeaderBlock, 16);
        RecordID = variableLenghtHeaderBlock.getShort();
        RecordLengthAfterHeader = variableLenghtHeaderBlock.getShort();
        Description = readStringfromByteBuffer(variableLenghtHeaderBlock, 32);

    }

    private String readStringfromByteBuffer(ByteBuffer buffer, int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result += (char) buffer.get();
        }
        return result;
    }

    public short getReserved() {
        return Reserved;
    }

    public String getUserID() {
        return UserID;
    }

    public short getRecordID() {
        return RecordID;
    }

    public short getRecordLengthAfterHeader() {
        return RecordLengthAfterHeader;
    }

    public String getDescription() {
        return Description;
    }

}
