package nl.esciencecenter.neon.examples.viaAppia;

import nl.esciencecenter.neon.exceptions.InverseNotAvailableException;
import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.Float4Matrix;
import nl.esciencecenter.neon.math.Float4Vector;
import nl.esciencecenter.neon.math.FloatMatrixMath;

public class MatrixFMathExt {
    public MatrixFMathExt() {
    }

    public static Float3Vector unProject(Float4Matrix projection, Float4Matrix modelView, float[] viewPort,
            Float3Vector mouseCoords) throws InverseNotAvailableException {

        // System.out.println("VP   : " + viewPort[0] + ", " + viewPort[1] +
        // ", " + viewPort[2] + ", " + viewPort[3]);
        // System.out.println("Click: " + mouseCoords.getX() + ", " +
        // mouseCoords.getY() + ", " + mouseCoords.getZ());

        Float4Matrix mult = modelView.mul(projection);

        // System.out.println(mult);

        Float4Matrix inv = FloatMatrixMath.inverse(mult);

        // System.out.println(inv);

        float xMapped = (mouseCoords.getX() - viewPort[0]) / viewPort[2];
        float yMapped = (mouseCoords.getY() - viewPort[1]) / viewPort[3];
        float zMapped = mouseCoords.getZ();

        float xBound = xMapped * 2f - 1f;
        float yBound = yMapped * 2f - 1f;
        float zBound = zMapped * 2f - 1f;

        Float4Vector result = inv.mul(new Float4Vector(xBound, yBound, zBound, 1f));

        if (result.getW() == 0f)
            throw new InverseNotAvailableException("result.getW() == 0f");

        return result.stripAlpha().div(result.getW());

        // float calcX = 2f * (windowCoords.getX() - viewPort[0]) / viewPort[2]
        // - 1f;
        // float calcY = 2f * (windowCoords.getY() - viewPort[1]) / viewPort[3]
        // - 1f;
        // float calcZ = 2f * windowCoords.getZ() - 1f;
        //
        // return FloatMatrixMath.inverse(projection.mul(modelView)).mul(new
        // Float4Vector(calcX, calcY, calcZ, 1f));
    }
}
