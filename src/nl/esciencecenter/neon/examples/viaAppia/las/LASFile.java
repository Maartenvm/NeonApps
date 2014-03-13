package nl.esciencecenter.neon.examples.viaAppia.las;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.datastructures.VertexBufferObject;
import nl.esciencecenter.neon.shaders.ShaderProgram;

public class LASFile {
    private LASPublicHeader publicHeader;
    private LASVariableLengthRecord[] variableLengthRecords;

    private LASPointDataRecord pointDataRecord;

    private final File dataFile;

    int skip;

    public LASFile(File dataFile, int skip) {
        this.dataFile = dataFile;
        this.skip = skip;

        try (FileChannel fc = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ)) {
            ByteBuffer headerBlock = ByteBuffer.allocate(LASPublicHeader.HEADER_SIZE);
            fc.read(headerBlock);
            publicHeader = new LASPublicHeader(headerBlock);

            int numPointRecords = publicHeader.getNumberofpointrecords();
            int pointRecordType = publicHeader.getPointDataFormatID();

            if (pointRecordType == 3) {
                pointDataRecord = new LASPointDataRecord3(numPointRecords, publicHeader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VertexBufferObject readPoints(GL3 gl, BoundingBox overallBoundingBox) {
        VertexBufferObject result = null;

        try (FileChannel fc = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ)) {
            int dataSize = pointDataRecord.getSizePerRecord() * publicHeader.getNumberofpointrecords();

            ByteBuffer recordsBlock = ByteBuffer.allocate(dataSize);
            fc.position(publicHeader.getOffsettopointdata());
            fc.read(recordsBlock);

            result = pointDataRecord.readPoints(gl, recordsBlock, skip, overallBoundingBox);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void setUniforms(GL3 gl, ShaderProgram program) {
    }

    public LASPointDataRecord getPointDataRecord() {
        return pointDataRecord;
    }

    public LASPublicHeader getPublicHeader() {
        return publicHeader;
    }
}
