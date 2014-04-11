package nl.esciencecenter.neon.examples.viaAppia;

import nl.esciencecenter.neon.math.Float3Vector;

/* Copyright [2013] [Netherlands eScience Center]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * Default element to be stored in an octree data structure, as used in
 * {@link OctreeNode}
 * 
 * @author Maarten van Meersbergen <m.vanmeersbergen@esciencecenter.nl>
 */
public class OctreeElement {
    /** The center location for this octree element. */
    private final Float3Vector center;
    private final Float3Vector color;

    /**
     * Basic constructor for OctreeElement
     * 
     * @param center
     *            the center location for this element.
     */
    public OctreeElement(Float3Vector center, Float3Vector color) {
        this.center = center;
        this.color = color;
    }

    /**
     * Getter for the center of this element.
     * 
     * @return the center of this element.
     */
    public Float3Vector getCenter() {
        return this.center;
    }

    /**
     * Getter for the color of this element.
     * 
     * @return the color of this element.
     */
    public Float3Vector getColor() {
        return this.color;
    }
}
