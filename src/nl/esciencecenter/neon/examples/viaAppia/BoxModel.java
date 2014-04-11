package nl.esciencecenter.neon.examples.viaAppia;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.Float4Vector;
import nl.esciencecenter.neon.math.FloatVectorMath;
import nl.esciencecenter.neon.math.Point4;
import nl.esciencecenter.neon.models.Model;

/* Copyright [2013] [Netherlands eScience Center]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Box implementation of the Model class.
 * 
 * @author Maarten van Meersbergen <m.vanmeersbergen@esciencecenter.nl>
 */
public class BoxModel extends Model {

    private static final int VERTICES_PER_QUAD = 6;

    /**
     * Basic constructor for Box. Allows for an optional bottom side.
     * 
     * @param width
     *            The width (X) of this box (should fall in 0.0 to 1.0
     *            interval).
     * @param height
     *            The height (Y) of this box (should fall in 0.0 to 1.0
     *            interval).
     * @param depth
     *            The depth (Z) for this box (should fall in 0.0 to 1.0
     *            interval).
     * @param bottom
     *            flag to draw either a bottom (true) or no bottom (false).
     */
    public BoxModel() {
        super(VertexFormat.TRIANGLES);

        Point4[] vertices = makeVertices();

        List<Float4Vector> allPoints = new ArrayList<Float4Vector>();
        List<Float3Vector> allNormals = new ArrayList<Float3Vector>();
        List<Float3Vector> allTexCoords = new ArrayList<Float3Vector>();

        // FRONT QUAD
        List<Point4> points = tesselate(vertices, 0, 1, 4, 5);
        List<Float3Vector> normals = createNormals(new Float3Vector(0, 0, -1));
        List<Float3Vector> tCoords = createTexCoords();

        allPoints.addAll(points);
        allNormals.addAll(normals);
        allTexCoords.addAll(tCoords);

        // RIGHT QUAD
        points = tesselate(vertices, 1, 3, 5, 7);
        normals = createNormals(new Float3Vector(1, 0, 0));
        tCoords = createTexCoords();

        allPoints.addAll(points);
        allNormals.addAll(normals);
        allTexCoords.addAll(tCoords);

        // BOTTOM QUAD
        points = tesselate(vertices, 2, 3, 0, 1);
        normals = createNormals(new Float3Vector(0, -1, 0));
        tCoords = createTexCoords();

        allPoints.addAll(points);
        allNormals.addAll(normals);
        allTexCoords.addAll(tCoords);

        // TOP QUAD
        points = tesselate(vertices, 4, 5, 6, 7);
        normals = createNormals(new Float3Vector(0, 1, 0));
        tCoords = createTexCoords();

        allPoints.addAll(points);
        allNormals.addAll(normals);
        allTexCoords.addAll(tCoords);

        // BACK QUAD
        points = tesselate(vertices, 6, 7, 2, 3);
        normals = createNormals(new Float3Vector(0, 0, 1));
        tCoords = createTexCoords();

        allPoints.addAll(points);
        allNormals.addAll(normals);
        allTexCoords.addAll(tCoords);

        // LEFT QUAD
        points = tesselate(vertices, 2, 0, 6, 4);
        normals = createNormals(new Float3Vector(-1, 0, 0));
        tCoords = createTexCoords();

        allPoints.addAll(points);
        allNormals.addAll(normals);
        allTexCoords.addAll(tCoords);

        this.setNumVertices(allPoints.size());

        this.setVertices(FloatVectorMath.vec4ListToBuffer(allPoints));
        this.setNormals(FloatVectorMath.vec3ListToBuffer(allNormals));
        this.setTexCoords(FloatVectorMath.vec3ListToBuffer(allTexCoords));
    }

    /**
     * Helper method to create a Vertex array describing the corners of a box.
     * The sides still need to be divided into triangles before it can be used.
     * 
     * @param width
     *            The width of the box to make (assumes input from 0.0 to 1.0).
     * @param height
     *            The height of the box to make (assumes input from 0.0 to 1.0).
     * @param depth
     *            The depth of the box to make (assumes input from 0.0 to 1.0).
     * @return The array of 8 points that makes up all the corners of a box.
     */
    private Point4[] makeVertices() {
        float x1 = 1f;
        float x0 = -1f;
        float y1 = 1f;
        float y0 = -1f;
        float z1 = 1f;
        float z0 = -1f;

        Point4[] result = new Point4[] { new Point4(x0, y0, z0), new Point4(x1, y0, z0), new Point4(x0, y0, z1),
                new Point4(x1, y0, z1), new Point4(x0, y1, z0), new Point4(x1, y1, z0), new Point4(x0, y1, z1),
                new Point4(x1, y1, z1) };

        return result;
    }

    /**
     * Create two triangles with vertices (Points) in the correct order out of
     * the given points.
     * 
     * @param source
     *            The source array with base-model vertices.
     * @param a
     *            The index of the first corner in the source array.
     * @param b
     *            The index of the second corner in the source array.
     * @param c
     *            The index of the third corner in the source array.
     * @param d
     *            The index of the fourth corner in the source array.
     * @return A list with 6 vertices representing 2 triangles.
     */
    private List<Point4> tesselate(Point4[] source, int a, int b, int c, int d) {
        ArrayList<Point4> result = new ArrayList<Point4>();

        result.add(source[a]);
        result.add(source[b]);
        result.add(source[c]);
        result.add(source[c]);
        result.add(source[b]);
        result.add(source[d]);

        return result;
    }

    /**
     * Create standard texture coordinates for a box.
     * 
     * @return An array with 6 vectors representing texture coordinates for the
     *         given points.
     */
    private List<Float3Vector> createTexCoords() {
        ArrayList<Float3Vector> result = new ArrayList<Float3Vector>();

        result.add(new Float3Vector(0, 0, 0));
        result.add(new Float3Vector(0, 1, 0));
        result.add(new Float3Vector(1, 1, 0));
        result.add(new Float3Vector(0, 0, 0));
        result.add(new Float3Vector(1, 1, 0));
        result.add(new Float3Vector(1, 0, 0));

        return result;
    }

    /**
     * Create normals for the given points.
     * 
     * @param normalToCreate
     *            The normal vector to copy X times ;)
     * @return An array with 6 vectors representing normals for the given
     *         points.
     */
    private List<Float3Vector> createNormals(Float3Vector normalToCreate) {
        ArrayList<Float3Vector> result = new ArrayList<Float3Vector>();

        for (int i = 0; i < VERTICES_PER_QUAD; i++) {
            result.add(new Float3Vector(normalToCreate));
        }

        return result;
    }
}
