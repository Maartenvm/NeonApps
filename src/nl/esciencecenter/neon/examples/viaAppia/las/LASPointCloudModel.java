package nl.esciencecenter.neon.examples.viaAppia.las;

import javax.media.opengl.GL3;

import nl.esciencecenter.neon.exceptions.UninitializedException;
import nl.esciencecenter.neon.models.Model;
import nl.esciencecenter.neon.shaders.ShaderProgram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LASPointCloudModel extends Model {
    private final static Logger LOGGER = LoggerFactory.getLogger(LASPointCloudModel.class);
    private boolean initialized = false;

    private final LASFile associatedFile;
    private LASPublicHeader associatedHeader;
    private LASPointDataRecord associatedRecord;

    private final BoundingBox overallBoundingBox;

    public LASPointCloudModel(LASFile associatedFile, BoundingBox overallBoundingBox) {
        super(VertexFormat.POINTS);

        this.associatedFile = associatedFile;
        this.overallBoundingBox = overallBoundingBox;
    }

    @Override
    public synchronized void init(GL3 gl) {
        if (!initialized) {
            delete(gl);

            setVbo(associatedFile.readPoints(gl, overallBoundingBox));

            this.associatedHeader = associatedFile.getPublicHeader();
            this.associatedRecord = associatedFile.getPointDataRecord();
            setNumVertices(associatedRecord.getNumPoints());

            initialized = true;
        }
    }

    @Override
    public void draw(GL3 gl, ShaderProgram program) throws UninitializedException {
        if (initialized) {
            double scaleFactorX = associatedHeader.getXscalefactor();
            double scaleFactorY = associatedHeader.getYscalefactor();
            double scaleFactorZ = associatedHeader.getZscalefactor();

            double offsetX = associatedHeader.getXoffset();
            double offsetY = associatedHeader.getYoffset();
            double offsetZ = associatedHeader.getZoffset();

            double minX = associatedHeader.getMinX();
            double minY = associatedHeader.getMinY();
            double minZ = associatedHeader.getMinZ();

            double maxX = associatedHeader.getMaxX();

            double diffX = maxX - minX;

            program.setUniform("scaleFactorX", scaleFactorX);
            program.setUniform("scaleFactorY", scaleFactorY);
            program.setUniform("scaleFactorZ", scaleFactorZ);

            program.setUniform("offsetX", offsetX);
            program.setUniform("offsetY", offsetY);
            program.setUniform("offsetZ", offsetZ);

            program.setUniform("minX", minX);
            program.setUniform("minY", minY);
            program.setUniform("minZ", minZ);

            program.setUniform("diffX", diffX);

            try {
                program.use(gl);
            } catch (UninitializedException e) {
                LOGGER.error(e.getMessage());
            }

            getVbo().bind(gl);

            program.linkAttribs(gl, getVbo().getAttribs());

            gl.glDrawArrays(GL3.GL_POINTS, 0, getNumVertices());

            // System.out.println("drew " + getNumVertices() + " points.");
        } else {
            throw new UninitializedException();
        }
    }
}
