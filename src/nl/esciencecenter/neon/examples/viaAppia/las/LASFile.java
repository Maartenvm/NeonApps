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

    public LASFile(File dataFile) {
        this.dataFile = dataFile;

        try (FileChannel fc = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ)) {
            ByteBuffer headerBlock = ByteBuffer.allocate(LASPublicHeader.HEADER_SIZE);
            fc.read(headerBlock);
            publicHeader = new LASPublicHeader(headerBlock);

            System.out.println(publicHeader);

            int numPointRecords = publicHeader.getNumberofpointrecords();
            int pointRecordType = publicHeader.getPointDataFormatID();

            if (pointRecordType == 0) {
                pointDataRecord = new LASPointDataRecord0(numPointRecords, publicHeader);
            } else if (pointRecordType == 1) {
                pointDataRecord = new LASPointDataRecord1(numPointRecords, publicHeader);
            } else if (pointRecordType == 2) {
                pointDataRecord = new LASPointDataRecord2(numPointRecords, publicHeader);
            } else if (pointRecordType == 3) {
                pointDataRecord = new LASPointDataRecord3(numPointRecords, publicHeader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VertexBufferObject readPoints(GL3 gl, BoundingBox overallBoundingBox, int skip) {
        VertexBufferObject result = null;

        try (FileChannel fc = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ)) {
            result = pointDataRecord.readPoints(gl, fc, publicHeader.getOffsettopointdata(), skip, overallBoundingBox);
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
