package nl.esciencecenter.neon.examples.viaAppia;

import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.FloatVectorMath;
import nl.esciencecenter.neon.models.BoundingBox;

public class Frustum {
    private static final double DEGREESTORADIANS = Math.PI / 180.0;
    private static int INSIDE = 2345623, OUTSIDE = 254743, INTERSECT = 92352;
    Float3Vector pointTop;
    Float3Vector pointBottom;
    Float3Vector pointLeft;
    Float3Vector pointRight;

    Float3Vector normalTop;
    Float3Vector normalBottom;
    Float3Vector normalLeft;
    Float3Vector normalRight;

    public Frustum(float fovy, float aspect, float zNear, float zFar, Float3Vector cameraPosition,
            Float3Vector cameraDirection, Float3Vector up, Float3Vector right) {
        float hNear = (float) (Math.tan(fovy * DEGREESTORADIANS / 2) * zNear);
        float wNear = hNear * aspect;

        float hFar = (float) (Math.tan(fovy * DEGREESTORADIANS / 2) * zFar);
        float wFar = hFar * aspect;

        Float3Vector nearPlaneNormal = FloatVectorMath.normalize(cameraDirection);
        Float3Vector farPlaneNormal = FloatVectorMath.normalize(cameraDirection.neg());

        Float3Vector nearPlanePoint = cameraPosition.add(cameraDirection.mul(zNear));
        Float3Vector farPlanePoint = cameraPosition.add(cameraDirection.mul(zFar));

        pointTop = pointTop(farPlanePoint, up, hFar);
        pointBottom = pointBottom(farPlanePoint, up, hFar);
        pointLeft = pointLeft(farPlanePoint, right, wFar);
        pointRight = pointRight(farPlanePoint, right, wFar);

        normalTop = normalTop(cameraPosition, nearPlanePoint, up, hNear, right);
        normalBottom = normalBottom(cameraPosition, nearPlanePoint, up, hNear, right);
        normalLeft = normalLeft(cameraPosition, nearPlanePoint, up, right, wNear);
        normalRight = normalRight(cameraPosition, nearPlanePoint, up, right, wNear);

        System.out.println("---");
        System.out.println("pn:" + nearPlanePoint);
        System.out.println("nn:" + nearPlaneNormal);
        System.out.println("pf:" + farPlanePoint);
        System.out.println("nf:" + farPlaneNormal);
        System.out.println();
        System.out.println("pt:" + pointTop);
        System.out.println("nt:" + normalTop);
        System.out.println("pb:" + pointBottom);
        System.out.println("nb:" + normalBottom);
        System.out.println("pl:" + pointLeft);
        System.out.println("nl:" + normalLeft);
        System.out.println("pr:" + pointRight);
        System.out.println("nr:" + normalRight);
    }

    int boxInFrustum(BoundingBox box) {
        int result = INSIDE, out, in;

        // for each plane do ...
        for (int i = 0; i < 6; i++) {
            // reset counters for corners in and out
            out = 0;
            in = 0;
            // for each corner of the box do ...
            // get out of the cycle as soon as a box as corners
            // both inside and out of the frustum
            for (int k = 0; k < 8 && (in == 0 || out == 0); k++) {
                // is the corner outside or inside
                if (pl[i].distance(b.getVertex(k)) < 0)
                    out++;
                else
                    in++;
            }
            // if all corners are out
            if (in == 0) {
                return (OUTSIDE);
            }
            // if some corners are out and others are in
            else if (out > 0) {
                result = INTERSECT;
            }
        }
        return (result);
    }

    private Float3Vector farTopLeft(Float3Vector cameraPosition, Float3Vector cameraDirection, float zFar,
            Float3Vector up, float hFar, Float3Vector right, float wFar) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zFar));
        return (fc.add(up.mul(hFar / 2f))).sub(right.mul(wFar / 2f));
    }

    private Float3Vector farTopRight(Float3Vector cameraPosition, Float3Vector cameraDirection, float zFar,
            Float3Vector up, float hFar, Float3Vector right, float wFar) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zFar));
        return (fc.add(up.mul(hFar / 2f))).add(right.mul(wFar / 2f));
    }

    private Float3Vector farBottomLeft(Float3Vector cameraPosition, Float3Vector cameraDirection, float zFar,
            Float3Vector up, float hFar, Float3Vector right, float wFar) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zFar));
        return (fc.sub(up.mul(hFar / 2f))).sub(right.mul(wFar / 2f));
    }

    private Float3Vector farBottomRight(Float3Vector cameraPosition, Float3Vector cameraDirection, float zFar,
            Float3Vector up, float hFar, Float3Vector right, float wFar) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zFar));
        return (fc.sub(up.mul(hFar / 2f))).add(right.mul(wFar / 2f));
    }

    private Float3Vector nearTopLeft(Float3Vector cameraPosition, Float3Vector cameraDirection, float zNear,
            Float3Vector up, float hNear, Float3Vector right, float wNear) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zNear));
        return (fc.add(up.mul(hNear / 2f))).sub(right.mul(wNear / 2f));
    }

    private Float3Vector nearTopRight(Float3Vector cameraPosition, Float3Vector cameraDirection, float zNear,
            Float3Vector up, float hNear, Float3Vector right, float wNear) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zNear));
        return (fc.add(up.mul(hNear / 2f))).add(right.mul(wNear / 2f));
    }

    private Float3Vector nearBottomLeft(Float3Vector cameraPosition, Float3Vector cameraDirection, float zNear,
            Float3Vector up, float hNear, Float3Vector right, float wNear) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zNear));
        return (fc.sub(up.mul(hNear / 2f))).sub(right.mul(wNear / 2f));
    }

    private Float3Vector nearBottomRight(Float3Vector cameraPosition, Float3Vector cameraDirection, float zNear,
            Float3Vector up, float hNear, Float3Vector right, float wNear) {
        Float3Vector fc = cameraPosition.add(cameraDirection.mul(zNear));
        return (fc.sub(up.mul(hNear / 2f))).add(right.mul(wNear / 2f));
    }

    private Float3Vector normalTop(Float3Vector cameraPosition, Float3Vector nearPlanePoint, Float3Vector up,
            float hNear, Float3Vector right) {
        Float3Vector a = (nearPlanePoint.add(up.mul(hNear / 2f))).sub(cameraPosition);
        return FloatVectorMath.cross(FloatVectorMath.normalize(a), right);
    }

    private Float3Vector normalBottom(Float3Vector cameraPosition, Float3Vector nearPlanePoint, Float3Vector up,
            float hNear, Float3Vector right) {
        Float3Vector a = (nearPlanePoint.sub(up.mul(hNear / 2f))).sub(cameraPosition);
        return FloatVectorMath.cross(right, FloatVectorMath.normalize(a));
    }

    private Float3Vector normalLeft(Float3Vector cameraPosition, Float3Vector nearPlanePoint, Float3Vector up,
            Float3Vector right, float wNear) {
        Float3Vector a = (nearPlanePoint.sub(right.mul(wNear / 2f))).sub(cameraPosition);
        return FloatVectorMath.cross(FloatVectorMath.normalize(a), up);
    }

    private Float3Vector normalRight(Float3Vector cameraPosition, Float3Vector nearPlanePoint, Float3Vector up,
            Float3Vector right, float wNear) {
        Float3Vector a = (nearPlanePoint.add(right.mul(wNear / 2f))).sub(cameraPosition);
        return FloatVectorMath.cross(up, FloatVectorMath.normalize(a));
    }

    private Float3Vector normalNear(Float3Vector cameraDirection) {
        return FloatVectorMath.normalize(cameraDirection);
    }

    private Float3Vector normalFar(Float3Vector cameraDirection) {
        return FloatVectorMath.normalize(cameraDirection.neg());
    }

    private Float3Vector pointTop(Float3Vector nearPlanePoint, Float3Vector up, float hNear) {
        return nearPlanePoint.add(up.mul(hNear / 2f));
    }

    private Float3Vector pointBottom(Float3Vector nearPlanePoint, Float3Vector up, float hNear) {
        return nearPlanePoint.sub(up.mul(hNear / 2f));
    }

    private Float3Vector pointLeft(Float3Vector nearPlanePoint, Float3Vector right, float wNear) {
        return nearPlanePoint.sub(right.mul(wNear / 2f));
    }

    private Float3Vector pointRight(Float3Vector nearPlanePoint, Float3Vector right, float wNear) {
        return nearPlanePoint.add(right.mul(wNear / 2f));
    }

}
