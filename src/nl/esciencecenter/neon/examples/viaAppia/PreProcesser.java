package nl.esciencecenter.neon.examples.viaAppia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.neon.examples.viaAppia.las.BoundingBox;
import nl.esciencecenter.neon.examples.viaAppia.las.LASFile;
import nl.esciencecenter.neon.examples.viaAppia.las.LASPublicHeader;
import nl.esciencecenter.neon.math.Float3Vector;

public class PreProcesser {
    // Global (singleton) settings instance.
    private final static ViaAppiaSettings settings = ViaAppiaSettings.getInstance();

    public static void main(String[] args) {
        double minMinX = Double.MAX_VALUE;
        double minMinY = Double.MAX_VALUE;
        double minMinZ = Double.MAX_VALUE;

        double maxMaxX = Double.MIN_VALUE;
        double maxMaxY = Double.MIN_VALUE;
        double maxMaxZ = Double.MIN_VALUE;

        BoundingBox overallBoundingBox = new BoundingBox(minMinX, maxMaxX, minMinY, maxMaxY, minMinZ, maxMaxZ);

        boolean colorDataIncluded = true;

        List<LASFile> lasFiles = new ArrayList<LASFile>();
        long totalRecords = 0;

        for (File dataFile : settings.getFiles()) {
            if (dataFile != null && dataFile.exists()) {
                LASFile lasFile = new LASFile(dataFile);

                LASPublicHeader header = lasFile.getPublicHeader();
                double minX = header.getMinX();
                if (minX < minMinX) {
                    minMinX = minX;
                }
                double minY = header.getMinY();
                if (minY < minMinY) {
                    minMinY = minY;
                }
                double minZ = header.getMinZ();
                if (minZ < minMinZ) {
                    minMinZ = minZ;
                }
                double maxX = header.getMaxX();
                if (maxX > maxMaxX) {
                    maxMaxX = maxX;
                }
                double maxY = header.getMaxY();
                if (maxY > maxMaxY) {
                    maxMaxY = maxY;
                }
                double maxZ = header.getMaxZ();
                if (maxZ > maxMaxZ) {
                    maxMaxZ = maxZ;
                }

                if (header.getPointDataFormatID() < 2) {
                    colorDataIncluded = false;
                }

                totalRecords += header.getNumberofpointrecords();

                lasFiles.add(lasFile);
            }
        }

        overallBoundingBox.set(minMinX, maxMaxX, minMinY, maxMaxY, minMinZ, maxMaxZ);

        System.out.println("Number of records: " + totalRecords);

        PPOctreeNode root = new PPOctreeNode(0, new Float3Vector(-1f, -1f, -1f), 2f);
        for (LASFile lasFile : lasFiles) {
            lasFile.readPointsToOctree(root, overallBoundingBox);
        }

    }

}
