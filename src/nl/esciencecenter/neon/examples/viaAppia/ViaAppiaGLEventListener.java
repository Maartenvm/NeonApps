package nl.esciencecenter.neon.examples.viaAppia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;

import nl.esciencecenter.neon.NeonGLEventListener;
import nl.esciencecenter.neon.datastructures.FrameBufferObject;
import nl.esciencecenter.neon.datastructures.IntPixelBufferObject;
import nl.esciencecenter.neon.examples.viaAppia.las.BoundingBox;
import nl.esciencecenter.neon.examples.viaAppia.las.LASFile;
import nl.esciencecenter.neon.examples.viaAppia.las.LASPointCloudModel;
import nl.esciencecenter.neon.examples.viaAppia.las.LASPublicHeader;
import nl.esciencecenter.neon.exceptions.UninitializedException;
import nl.esciencecenter.neon.input.InputHandler;
import nl.esciencecenter.neon.math.Float3Vector;
import nl.esciencecenter.neon.math.Float4Matrix;
import nl.esciencecenter.neon.math.Float4Vector;
import nl.esciencecenter.neon.math.FloatMatrixMath;
import nl.esciencecenter.neon.math.FloatVectorMath;
import nl.esciencecenter.neon.math.Point4;
import nl.esciencecenter.neon.models.Axis;
import nl.esciencecenter.neon.models.GeoSphere;
import nl.esciencecenter.neon.models.Model;
import nl.esciencecenter.neon.models.Sphere;
import nl.esciencecenter.neon.shaders.ShaderProgram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Example implementation of a ESightGLEventListener. Renders Axes in different
 * colors to a texture and renders then this texture to the screen.
 * 
 * @author Maarten van Meersbergen <m.van.meersbergen@esciencecenter.nl>
 * 
 */
public class ViaAppiaGLEventListener extends NeonGLEventListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(ViaAppiaGLEventListener.class);

    // Two example shader program definitions.
    private ShaderProgram axesShaderProgram, textShaderProgram, lineShaderProgram, pointCloudShaderProgram;

    // Model definitions, the quad is necessary for Full-screen rendering. The
    // axes are the model we wish to render (example)
    private Model xAxis, yAxis, zAxis;

    // private ScatBuilder scatBuilder;
    // private ScatterPlot3D scat;
    private List<LASPointCloudModel> pcModel;

    // Global (singleton) settings instance.
    private final ViaAppiaSettings settings = ViaAppiaSettings.getInstance();

    // Pixelbuffer Object, we use this to get screenshots.
    private IntPixelBufferObject finalPBO;

    // Global (singleton) inputhandler instance.
    private final ViaAppiaInputHandler inputHandler = ViaAppiaInputHandler.getInstance();

    // State keeping variable
    private boolean screenshotWanted;

    // Height and width of the drawable area. We extract this from the opengl
    // instance in the reshape method every time it is changed, but set it in
    // the init method initially. The default values are defined by the settings
    // class.
    private int canvasWidth, canvasHeight;

    // Variables needed to calculate the viewpoint and camera angle.
    final Point4 eye = new Point4((float) (getRadius() * Math.sin(getFtheta()) * Math.cos(getPhi())),
            (float) (getRadius() * Math.sin(getFtheta()) * Math.sin(getPhi())),
            (float) (getRadius() * Math.cos(getFtheta())));
    final Point4 at = new Point4(0.0f, 0.0f, 0.0f);
    final Float4Vector up = new Float4Vector(0.0f, 1.0f, 0.0f, 0.0f);

    private final Float3Vector clickTranslation = new Float3Vector();

    private Sphere clickSphere;

    private GeoSphere globe;

    int filesToLoad = 105;

    /**
     * Basic constructor for ESightExampleGLEventListener.
     */
    public ViaAppiaGLEventListener() {
        super();

    }

    // int cachedFileNumber = inputHandler.getFileNumber();

    // Initialization method, this is called by the animator before anything
    // else, and is therefore the perfect place to initialize all of the
    // ShaderPrograms, FrameBuffer objects and such.
    @Override
    public void init(GLAutoDrawable drawable) {
        // Get the Opengl context from the drawable, and make it current, so
        // we can see it and draw on it. I've never seen this fail, but there is
        // error checking anyway.
        contextOn(drawable);

        // Once we have the context current, we can extract the OpenGL instance
        // from it. We have defined a OpenGL 3.0 instance in the
        // ESightNewtWindow by adding the line
        // glp = GLProfile.get(GLProfile.GL3);
        // Therefore, we extract a GL3 instance, so we cannot make any
        // unfortunate mistakes (calls to methods that are undefined for this
        // version).
        final GL3 gl = GLContext.getCurrentGL().getGL3();

        // set the canvas size and aspect ratio in the global variables.
        canvasWidth = GLContext.getCurrent().getGLDrawable().getWidth();
        canvasHeight = GLContext.getCurrent().getGLDrawable().getHeight();
        setAspect((float) canvasWidth / (float) canvasHeight);

        // Enable Anti-Aliasing (smoothing of jagged edges on the edges of
        // objects).
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);

        // Enable Depth testing (Render only those objects that are not obscured
        // by other objects).
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_LEQUAL);
        gl.glClearDepth(1.0f);

        // Enable Culling (render only the camera-facing sides of objects).
        // gl.glEnable(GL3.GL_CULL_FACE);
        // gl.glCullFace(GL3.GL_BACK);

        // Enable Blending (needed for both Transparency and Anti-Aliasing)
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL3.GL_BLEND);

        // Enable Vertical Sync
        gl.setSwapInterval(1);

        // Set black background
        gl.glClearColor(0f, 0f, 0f, 0f);

        // Enable programmatic setting of point size, for rendering points (not
        // needed for this example application).
        gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);

        // Load and compile shaders from source Files (there are other options;
        // check the ShaderProgram Javadoc).
        try {
            // Create the ShaderProgram that we're going to use for the Example
            // Axes. The source code for the VertexShader: shaders/vs_axes.vp,
            // and the source code for the FragmentShader: shaders/fs_axes.fp
            axesShaderProgram = getLoader().createProgram(gl, "axes", new File("shaders/vs_axes.vp"),
                    new File("shaders/fs_axes.fp"));
            // Do the same for the text shader
            textShaderProgram = getLoader().createProgram(gl, "text", new File("shaders/vs_multiColorTextShader.vp"),
                    new File("shaders/fs_multiColorTextShader.fp"));
            // Do the same for the line shader
            lineShaderProgram = getLoader().createProgram(gl, "line", new File("shaders/vs_lineShader.vp"),
                    new File("shaders/fs_lineShader.fp"));
            // Do the same for the PointCloud shader
            pointCloudShaderProgram = getLoader().createProgram(gl, "lasFile", new File("shaders/vs_lasFileShader.vp"),
                    new File("shaders/fs_lasFileShader.fp"));

            // Same for the postprocessing shader.
            // postprocessShader = getLoader().createProgram(gl, "postProcess",
            // new File("shaders/vs_postprocess.vp"),
            // new File("shaders/fs_examplePostprocess.fp"));
        } catch (final Exception e) {
            // If compilation fails, we will output the error message and quit
            // the application.
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // Here we define the Axis models, and initialize them.
        xAxis = new Axis(new Float3Vector(-1f, 0f, 0f), new Float3Vector(1f, 0f, 0f), .1f, .02f);
        xAxis.init(gl);
        yAxis = new Axis(new Float3Vector(0f, -1f, 0f), new Float3Vector(0f, 1f, 0f), .1f, .02f);
        yAxis.init(gl);
        zAxis = new Axis(new Float3Vector(0f, 0f, -1f), new Float3Vector(0f, 0f, 1f), .1f, .02f);
        zAxis.init(gl);

        // Here we define a PixelBufferObject, which is used for getting
        // screenshots.
        finalPBO = new IntPixelBufferObject(canvasWidth, canvasHeight);
        finalPBO.init(gl);

        clickSphere = new Sphere(2, false);
        clickSphere.init(gl);

        globe = new GeoSphere(20, 20, 1, false);
        globe.init(gl);

        pcModel = new ArrayList<LASPointCloudModel>();

        double minMinX = Double.MAX_VALUE;
        double minMinY = Double.MAX_VALUE;
        double minMinZ = Double.MAX_VALUE;

        double maxMaxX = Double.MIN_VALUE;
        double maxMaxY = Double.MIN_VALUE;
        double maxMaxZ = Double.MIN_VALUE;

        BoundingBox overallBoundingBox = new BoundingBox(minMinX, maxMaxX, minMinY, maxMaxY, minMinZ, maxMaxZ);

        File dataFile;
        for (int sequenceNumber = 1; sequenceNumber < filesToLoad; sequenceNumber++) {
            // cachedFileNumber = inputHandler.getFileNumber();
            dataFile = new File("/media/maarten/diskhdd2/Via Appia/Rome-000" + String.format("%03d", sequenceNumber)
                    + ".las");
            if (dataFile != null && dataFile.exists()) {
                LASFile lasFile = new LASFile(dataFile, 10);

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

                LASPointCloudModel currentModel = new LASPointCloudModel(lasFile, overallBoundingBox);
                pcModel.add(currentModel);
            }
        }

        overallBoundingBox.set(minMinX, maxMaxX, minMinY, maxMaxY, minMinZ, maxMaxZ);

        for (LASPointCloudModel currentModel : pcModel) {
            // LASPointDataRecord record = currentModel.getRecord();

            currentModel.init(gl);// , minMinX, maxMaxX, minMinY, maxMaxY,
                                  // minMinZ, maxMaxZ);
        }

        System.out.println("init complete");

        // scatBuilder = new ScatBuilder();
        // new Thread(scatBuilder).start();

        // Release the context.
        contextOff(drawable);
    }

    // Display method, this is called by the animator thread to render a single
    // frame. Expect this to be running 60 times a second.
    // The GLAutoDrawable is a JOGL concept that holds the current opengl state.
    @Override
    public void display(GLAutoDrawable drawable) {
        // Get the Opengl context from the drawable, and make it current, so
        // we can see it and draw on it. I've never seen this fail, but there is
        // error checking anyway.
        contextOn(drawable);

        // Once we have the context current, we can extract the OpenGL instance
        // from it. We have defined a OpenGL 3.0 instance in the
        // ESightNewtWindow by adding the line
        // glp = GLProfile.get(GLProfile.GL3);
        // Therefore, we extract a GL3 instance, so we cannot make any
        // unfortunate mistakes (calls to methods that are undefined for this
        // version).
        final GL3 gl = GLContext.getCurrentGL().getGL3();

        // First, we clear the buffer to start with a clean slate to draw on.
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Construct a modelview matrix out of camera viewpoint and angle.
        // Float4Matrix modelViewMatrix = FloatMatrixMath.lookAt(eye, at, up);
        Float4Matrix modelViewMatrix = inputHandler.getModelview();

        // Translate the camera backwards according to the inputhandler's view
        // distance setting.
        // modelViewMatrix =
        // modelViewMatrix.mul(FloatMatrixMath.translate(clickTranslation));

        // modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.translate(0f,
        // 0f, inputHandler.getViewDist()));
        //
        // modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.translate(new
        // Float3Vector(inputHandler.getTranslation()
        // .getX(), inputHandler.getTranslation().getY(), 0f)));

        // Rotate tha camera according to the rotation angles defined in the
        // inputhandler.
        // modelViewMatrix =
        // modelViewMatrix.mul(FloatMatrixMath.rotationX(inputHandler.getRotation().getX()));
        // modelViewMatrix =
        // modelViewMatrix.mul(FloatMatrixMath.rotationY(inputHandler.getRotation().getY()));
        // modelViewMatrix =
        // modelViewMatrix.mul(FloatMatrixMath.rotationZ(inputHandler.getRotation().getZ()));

        // Render the scene with these modelview settings. In this case, the end
        // result of this action will be that the AxesFBO has been filled with
        // the right pixels.
        renderScene(gl, modelViewMatrix);

        // ViaAppiaInputHandler.PickRequest pr = ViaAppiaInputHandler.doPick();
        // ViaAppiaInputHandler.PickRequest pr = null;
        // if (pr != null) {
        // try {
        // int mouseX = pr.x;
        // int mouseY = canvasHeight - pr.y;
        //
        // // gl.glFlush();
        // // gl.glFinish();
        // // gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);
        //
        // FloatBuffer depthData = FloatBuffer.allocate(1);
        // gl.glReadPixels(mouseX, mouseY, 1, 1, GL3.GL_DEPTH_COMPONENT,
        // GL3.GL_FLOAT, depthData);
        // float mouseZ = depthData.get();
        //
        // FloatBuffer colorData = FloatBuffer.allocate(4);
        // gl.glReadBuffer(GL3.GL_FRONT);
        // gl.glReadPixels(mouseX, mouseY, 1, 1, GL3.GL_RGBA, GL3.GL_FLOAT,
        // colorData);
        //
        // // System.out.println("Color: " + data.get(0) + " " +
        // // data.get(1) + " " + data.get(2) + " "
        // // + data.get(3));
        //
        // // System.out.println("Click : " + mouseX + "/" + mouseY);
        //
        // if (FloatVectorMath.length(new Float3Vector(colorData.get(0),
        // colorData.get(1), colorData.get(2))) > 0f) {
        //
        // Float3Vector pickResultNear =
        // MatrixFMathExt.unProject(makePerspectiveMatrix(), modelViewMatrix,
        // new float[] { 0, 0, canvasWidth, canvasHeight }, new
        // Float3Vector(mouseX, mouseY, 0f));
        // // System.out.println("Pick result near: " +
        // // pickResultNear);
        //
        // Float3Vector pickResultFar =
        // MatrixFMathExt.unProject(makePerspectiveMatrix(), modelViewMatrix,
        // new float[] { 0, 0, canvasWidth, canvasHeight }, new
        // Float3Vector(mouseX, mouseY, 1f));
        // // System.out.println("Pick result far: " + pickResultFar);
        //
        // Float3Vector pickRay = pickResultFar.sub(pickResultNear);
        // float distance = pointToVectorDistance(new Float4Vector(0f, 0f, 0f,
        // 1f), pickResultNear,
        // pickResultFar);
        // System.out.println("Pick ray: " + pickRay + " Distance to origin: " +
        // distance);
        //
        // Float3Vector pickResultZ =
        // MatrixFMathExt.unProject(makePerspectiveMatrix(), modelViewMatrix,
        // new float[] { 0, 0, canvasWidth, canvasHeight }, new
        // Float3Vector(mouseX, mouseY, mouseZ));
        // // System.out.println("Pick result Z: " + pickResultZ);
        //
        // clickTranslation = pickResultZ;
        // }
        //
        // } catch (InverseNotAvailableException e) {
        // e.printStackTrace();
        // }

        // int window_y = (int)((canvasHeight - pr.y) - (canvasHeight/2f));
        // float norm_y = (float)(window_y)/((float)canvasHeight/2f);
        // int window_x = (int)(pr.x - (canvasWidth/2f));
        // float norm_x = (float)window_x/((float)canvasWidth/2f);
        //
        // float y = near_height * norm_y;
        // float x = near_height * aspect * norm_x;
        // }

        // try {
        // drawClickSphere(gl, lineShaderProgram, clickTranslation);
        // } catch (UninitializedException e) {
        // e.printStackTrace();
        // }

        // Make a screenshot, when wanted. The PBO copies the current
        // framebuffer. We then set the state back because we dont want to make
        // a screenshot 60 times a second.
        if (screenshotWanted) {
            finalPBO.makeScreenshotPNG(gl, settings.getScreenshotFileName());

            screenshotWanted = false;
        }

        // if (cachedFileNumber != inputHandler.getFileNumber()) {
        // File dataFile;
        // for (int sequenceNumber = 12; sequenceNumber < 13; sequenceNumber++)
        // {
        // cachedFileNumber = inputHandler.getFileNumber();
        // dataFile = new File("/media/maarten/diskhdd2/Via Appia/Rome-000"
        // + String.format("%03d", sequenceNumber) + ".las");
        // if (dataFile != null && dataFile.exists()) {
        // LASFile lasFile = new LASFile(dataFile, 10);
        // pcModel.delete(gl);
        // pcModel = new LASPointCloudModel(lasFile);
        // pcModel.init(gl);
        //
        // }
        // }
        // }

        contextOff(drawable);
    }

    private float pointToVectorDistance(Float4Vector point, Float3Vector a, Float3Vector b) {
        float distance = Float.MAX_VALUE;

        Float3Vector lineDirection = FloatVectorMath.normalize(b.sub(a));
        Float3Vector pointDirection = point.stripAlpha().sub(a);

        float t = FloatVectorMath.dot(pointDirection, lineDirection);

        Float3Vector projection = a.add(lineDirection.mul(t));

        distance = FloatVectorMath.length((projection.sub(point.stripAlpha())));

        return distance;
    }

    private Float4Matrix makePerspectiveMatrix() {
        return FloatMatrixMath.perspective(getFovy(), getAspect(), getzNear(), getzFar());
    }

    /**
     * Scene rendering method. we can add more things here to render than only
     * axes.
     * 
     * @param gl
     *            The current openGL instance.
     * @param mv
     *            The current modelview matrix.
     */
    private void renderScene(GL3 gl, Float4Matrix mv) {
        try {
            // renderScatterplot(gl, mv, textShaderProgram);
            renderAxes(gl, new Float4Matrix(mv), axesShaderProgram);
            renderPointCloud(gl, new Float4Matrix(mv), pointCloudShaderProgram);

            // drawGlobe(gl, lineShaderProgram);

        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Axes rendering method. This assumes rendering to an
     * {@link FrameBufferObject}. This is not a necessity, but it allows for
     * post processing.
     * 
     * @param gl
     *            The current openGL instance.
     * @param mv
     *            The current modelview matrix.
     * @param target
     *            The {@link ShaderProgram} to use for rendering.
     * @param target
     *            The target {@link FrameBufferObject} to render to.
     * @throws UninitializedException
     *             if either the shader Program or FrameBufferObject used in
     *             this method are uninitialized before use.
     */
    private void renderAxes(GL3 gl, Float4Matrix mv, ShaderProgram program) throws UninitializedException {
        // Stage the Perspective and Modelview matrixes in the ShaderProgram.
        program.setUniformMatrix("PMatrix", makePerspectiveMatrix());
        program.setUniformMatrix("MVMatrix", mv);

        // Stage the Color vector in the ShaderProgram.
        program.setUniformVector("Color", new Float4Vector(1f, 0f, 0f, 1f));

        // Load all staged variables into the GPU, check for errors and
        // omissions.
        program.use(gl);
        // Call the model's draw method, this links the model's VertexBuffer to
        // the ShaderProgram and then calls the OpenGL draw method.
        xAxis.draw(gl, program);

        // Do this 2 more times, with different colors and models.
        program.setUniformVector("Color", new Float4Vector(0f, 1f, 0f, 1f));
        program.use(gl);
        yAxis.draw(gl, program);

        program.setUniformVector("Color", new Float4Vector(0f, 0f, 1f, 1f));
        program.use(gl);
        zAxis.draw(gl, program);
    }

    private void drawClickSphere(GL3 gl, ShaderProgram program, Float3Vector center) throws UninitializedException {
        // Stage the Perspective and Modelview matrixes in the ShaderProgram.
        program.setUniformMatrix("PMatrix", makePerspectiveMatrix());

        // Construct a modelview matrix out of camera viewpoint and angle.
        Float4Matrix modelViewMatrix = FloatMatrixMath.lookAt(eye, at, up);

        // Translate the camera backwards according to the inputhandler's view
        // distance setting.
        // modelViewMatrix =
        // modelViewMatrix.mul(FloatMatrixMath.translate(clickTranslation));

        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.translate(new Float3Vector(inputHandler.getTranslation()
                .getX(), inputHandler.getTranslation().getY(), inputHandler.getViewDist())));

        // Rotate tha camera according to the rotation angles defined in the
        // inputhandler.
        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.rotationX(inputHandler.getRotation().getX()));
        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.rotationY(inputHandler.getRotation().getY()));
        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.rotationZ(inputHandler.getRotation().getZ()));

        // modelViewMatrix =
        // modelViewMatrix.mul(modelViewMatrix.mul(FloatMatrixMath.translate(center.neg())));
        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.scale(0.01f));

        program.setUniformMatrix("MVMatrix", modelViewMatrix);

        // Stage the Color vector in the ShaderProgram.
        program.setUniformVector("Color", new Float4Vector(1f, 0f, 0f, 1f));

        // Load all staged variables into the GPU, check for errors and
        // omissions.
        program.use(gl);
        // Call the model's draw method, this links the model's VertexBuffer to
        // the ShaderProgram and then calls the OpenGL draw method.
        clickSphere.draw(gl, program);
    }

    private void drawGlobe(GL3 gl, ShaderProgram program) throws UninitializedException {
        Float4Matrix modelViewMatrix = FloatMatrixMath.lookAt(eye, at, up);

        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.translate(new Float3Vector(inputHandler.getTranslation()
                .getX(), inputHandler.getTranslation().getY(), inputHandler.getViewDist())));

        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.rotationX(inputHandler.getRotation().getX()));
        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.rotationY(inputHandler.getRotation().getY()));
        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.rotationZ(inputHandler.getRotation().getZ()));

        modelViewMatrix = modelViewMatrix.mul(FloatMatrixMath.scale(1f));

        program.setUniformMatrix("PMatrix", makePerspectiveMatrix());
        program.setUniformMatrix("MVMatrix", modelViewMatrix);
        program.setUniformVector("Color", new Float4Vector(0f, 0f, 1f, 0.1f));

        program.use(gl);
        globe.draw(gl, program);
    }

    /**
     * Renders the scatterplot.
     * 
     * @param gl
     *            The current openGL instance.
     * @param mv
     *            The current modelview matrix.
     * @param program
     *            The {@link ShaderProgram} to use for rendering.
     * @throws UninitializedException
     */
    private void renderScatterplot(GL3 gl, Float4Matrix mv, ShaderProgram program) throws UninitializedException {
        // Stage the Perspective and Modelview matrixes in the ShaderProgram.
        program.setUniformMatrix("PMatrix", makePerspectiveMatrix());

        // Construct a modelview matrix out of camera viewpoint and angle.
        // Float4Matrix modelViewMatrix = FloatMatrixMath.lookAt(eye, at, up);

        // Translate the camera backwards according to the inputhandler's view
        // distance setting.
        // modelViewMatrix =
        // modelViewMatrix.mul(FloatMatrixMath.translate(clickTranslation));
        // Float4Matrix modelViewMatrix =
        // modelViewMatrix.mul(FloatMatrixMath.translate(new
        // Float3Vector(inputHandler.getTranslation()
        // .getX(), inputHandler.getTranslation().getY(),
        // inputHandler.getViewDist())));

        Float4Matrix objectRotationMatrix = FloatMatrixMath.rotationX(-90f);

        mv = mv.mul(objectRotationMatrix);

        program.setUniformMatrix("MVMatrix", mv);
        // mv.mul(FloatMatrixMath.translate(-0.5f, 0,
        // 4.5f)).mul(FloatMatrixMath.rotationX(-90)));

        // Load all staged variables into the GPU, check for errors and
        // omissions.
        program.use(gl);

        // scat = scatBuilder.getScatterPlot(gl);
        // if (scat != null) {
        // scat.draw(gl, program);
        // }
    }

    public void renderPointCloud(GL3 gl, Float4Matrix mv, ShaderProgram program) throws UninitializedException {
        program.setUniformMatrix("PMatrix", makePerspectiveMatrix());

        Float4Matrix objectRotationMatrix = FloatMatrixMath.rotationX(-90f);
        mv = mv.mul(objectRotationMatrix);
        program.setUniformMatrix("MVMatrix", mv);
        // program.use(gl);

        for (LASPointCloudModel model : pcModel) {
            model.draw(gl, program);
        }
    }

    // The reshape method is automatically called by the openGL animator if the
    // window holding the OpenGL 'canvas' is resized.
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        // Get the Opengl context from the drawable, and make it current, so
        // we can see it and draw on it. I've never seen this fail, but there is
        // error checking anyway.
        contextOn(drawable);

        // Once we have the context current, we can extract the OpenGL instance
        // from it. We have defined a OpenGL 3.0 instance in the
        // ESightNewtWindow by adding the line
        // "glp = GLProfile.get(GLProfile.GL3);"
        // Therefore, we extract a GL3 instance, so we cannot make any
        // unfortunate mistakes (calls to methods that are undefined for this
        // version).
        final GL3 gl = GLContext.getCurrentGL().getGL3();

        // set the new canvas size and aspect ratio in the global variables.
        canvasWidth = GLContext.getCurrent().getGLDrawable().getWidth();
        canvasHeight = GLContext.getCurrent().getGLDrawable().getHeight();
        setAspect((float) canvasWidth / (float) canvasHeight);

        gl.glViewport(0, 0, canvasWidth, canvasHeight);

        // Resize the PixelBuffer Object that can be used for screenshots.
        finalPBO.delete(gl);
        finalPBO = new IntPixelBufferObject(w, h);
        finalPBO.init(gl);

        // Release the context.
        contextOff(drawable);
    }

    // This dispose method is called when the OpenGL 'canvas' is destroyed. It
    // is used for cleanup.
    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Get the Opengl context from the drawable, and make it current, so
        // we can see it and draw on it. I've never seen this fail, but there is
        // error checking anyway.
        contextOn(drawable);

        // Once we have the context current, we can extract the OpenGL instance
        // from it. We have defined a OpenGL 3.0 instance in the
        // ESightNewtWindow by adding the line
        // "glp = GLProfile.get(GLProfile.GL3);"
        // Therefore, we extract a GL3 instance, so we cannot make any
        // unfortunate mistakes (calls to methods that are undefined for this
        // version).
        final GL3 gl = GLContext.getCurrentGL().getGL3();

        // Delete the FramBuffer Objects.
        finalPBO.delete(gl);

        // Let the ShaderProgramLoader clean up. This deletes all of the
        // ShaderProgram instances as well.
        try {
            getLoader().cleanup(gl);
        } catch (UninitializedException e1) {
            e1.printStackTrace();
        }

        // Release the context.
        contextOff(drawable);
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }
}
