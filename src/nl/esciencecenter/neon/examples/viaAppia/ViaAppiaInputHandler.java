package nl.esciencecenter.neon.examples.viaAppia;

import nl.esciencecenter.neon.input.InputHandler;
import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.Float4Matrix;
import nl.esciencecenter.neon.math.Float4Vector;
import nl.esciencecenter.neon.math.FloatMatrixMath;
import nl.esciencecenter.neon.math.FloatVectorMath;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

/* Copyright 2013 Netherlands eScience Center
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
 * Example {@link InputHandler} class implementation, that overrides the default
 * mousePressed event. This class uses the Singleton design pattern found here:
 * 
 * http://en.wikipedia.org/wiki/Singleton_pattern
 * 
 * @author Maarten van Meersbergen <m.van.meersbergen@esciencecenter.nl>
 * 
 */
public class ViaAppiaInputHandler extends InputHandler implements MouseListener, KeyListener {
    private static class SingletonHolder {
        public static final ViaAppiaInputHandler instance = new ViaAppiaInputHandler();
    }

    public static ViaAppiaInputHandler getInstance() {
        return SingletonHolder.instance;
    }

    /** Initial value for the rotation in the X direction */
    private final float rotationXorigin = 0;

    /** Initial value for the rotation in the Y direction */
    private final float rotationYorigin = 0;

    /** Mouse drag start point in X direction */
    private float dragXorigin;

    /** Mouse drag start point in Y direction */
    private float dragYorigin;

    private float cameraAngleH = 0f;
    private float cameraAngleV = 0f;

    private final Float3Vector cameraDirection = new Float3Vector(0f, 0f, -1f);
    private final Float3Vector cameraPosition = new Float3Vector(0f, 0.1f, 1.5f);

    private ViaAppiaInputHandler() {
        reset();
    }

    private void reset() {
    }

    float deltaAngleX = 0f;
    float deltaAngleY = 0f;

    private final boolean[] keyStates = new boolean[Short.MAX_VALUE];

    @Override
    public void mousePressed(MouseEvent e) {
        dragXorigin = e.getX();
        dragYorigin = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        cameraAngleH = cameraAngleH + deltaAngleX;
        cameraAngleV = cameraAngleV + deltaAngleY;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isButtonDown(MouseEvent.BUTTON1)) {
            deltaAngleX = (e.getX() - dragXorigin) * 0.001f;
            deltaAngleY = -(e.getY() - dragYorigin) * 0.001f;

            // cameraAngleH += dragDistX / 1000f;
            cameraDirection.setX((float) Math.sin(cameraAngleH + deltaAngleX));
            cameraDirection.setY((float) Math.sin(cameraAngleV + deltaAngleY));
            cameraDirection.setZ((float) -Math.cos(cameraAngleH + deltaAngleX));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        float fraction = 0.01f;

        keyStates[arg0.getKeyCode()] = true;

        if (keyStates[KeyEvent.VK_SHIFT]) {
            fraction = 0.1f;
        }

        if (keyStates[KeyEvent.VK_W]) {
            cameraPosition.setX(cameraPosition.getX() + cameraDirection.getX() * fraction);
            cameraPosition.setY(cameraPosition.getY() + cameraDirection.getY() * fraction);
            cameraPosition.setZ(cameraPosition.getZ() + cameraDirection.getZ() * fraction);
        }
        if (keyStates[KeyEvent.VK_S]) {
            cameraPosition.setX(cameraPosition.getX() - cameraDirection.getX() * fraction);
            cameraPosition.setY(cameraPosition.getY() - cameraDirection.getY() * fraction);
            cameraPosition.setZ(cameraPosition.getZ() - cameraDirection.getZ() * fraction);
        }

        if (keyStates[KeyEvent.VK_A]) {
            Float3Vector perp = FloatVectorMath.cross(
                    new Float3Vector(cameraDirection.getX(), 0f, cameraDirection.getZ()), new Float3Vector(
                            cameraDirection.getX(), 1f, cameraDirection.getZ()));
            cameraPosition.setX(cameraPosition.getX() - perp.getX() * fraction);
            cameraPosition.setZ(cameraPosition.getZ() - perp.getZ() * fraction);
        }

        if (keyStates[KeyEvent.VK_D]) {
            Float3Vector perp = FloatVectorMath.cross(
                    new Float3Vector(cameraDirection.getX(), 0f, cameraDirection.getZ()), new Float3Vector(
                            cameraDirection.getX(), 1f, cameraDirection.getZ()));
            cameraPosition.setX(cameraPosition.getX() + perp.getX() * fraction);
            cameraPosition.setZ(cameraPosition.getZ() + perp.getZ() * fraction);
        }

        if (keyStates[KeyEvent.VK_SPACE]) {
            cameraPosition.setY(cameraPosition.getY() + fraction);
        }

        if (keyStates[KeyEvent.VK_CONTROL]) {
            cameraPosition.setY(cameraPosition.getY() - fraction);
        }

        // if (arg0.getKeyCode() == KeyEvent.VK_A) {
        // cameraAngleH -= 0.01f;
        // cameraDirection.setX((float) Math.sin(cameraAngleH));
        // cameraDirection.setZ((float) -Math.cos(cameraAngleH));
        // }
        // if (arg0.getKeyCode() == KeyEvent.VK_D) {
        // cameraAngleH += 0.01f;
        // cameraDirection.setX((float) Math.sin(cameraAngleH));
        // cameraDirection.setZ((float) -Math.cos(cameraAngleH));
        // }
        // if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
        // cameraAngleV -= 0.01f;
        // cameraDirection.setY((float) Math.sin(cameraAngleV));
        // }
        // if (arg0.getKeyCode() == KeyEvent.VK_CONTROL) {
        // cameraAngleV += 0.01f;
        // cameraDirection.setY((float) Math.sin(cameraAngleV));
        // }
    }

    public Float4Matrix getModelview() {
        Float4Vector eye = new Float4Vector(cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ(), 1f);
        Float4Vector at = new Float4Vector(cameraPosition.getX() + cameraDirection.getX(), cameraPosition.getY()
                + cameraDirection.getY(), cameraPosition.getZ() + cameraDirection.getZ(), 1f);
        Float4Vector up = new Float4Vector(0f, 1f, 0f, 0f);
        Float4Matrix modelViewMatrix = FloatMatrixMath.lookAt(eye, at, up);

        return modelViewMatrix;
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        keyStates[arg0.getKeyCode()] = false;
    }

    public Float3Vector getCameraPosition() {
        return cameraPosition;
    }
}
