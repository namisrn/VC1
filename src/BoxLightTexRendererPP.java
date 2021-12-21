/**
 * Copyright 2012-2013 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import de.hshl.obj.loader.OBJLoader;
import de.hshl.obj.loader.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;

/**
 * Performs the OpenGL graphics processing using the Programmable Pipeline and the
 * OpenGL Core profile
 *
 * Starts an animation loop.
 * Zooming and rotation of the Camera is included (see InteractionHandler).
 * 	Use: left/right/up/down-keys and +/-Keys
 * Draws a simple box with light and textures.
 * Serves as a template (start code) for setting up an OpenGL/Jogl application
 * using a vertex and fragment shader.
 *
 * Please make sure setting the file path and names of the shader correctly (see below).
 *
 * Core code is based on a tutorial by Chua Hock-Chuan
 * http://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL2.0.html
 *
 * and on an example by Xerxes Rånby
 * http://jogamp.org/git/?p=jogl-demos.git;a=blob;f=src/demos/es2/RawGL2ES2demo.java;hb=HEAD
 *
 * @author Karsten Lehn
 * @version 12.11.2017, 18.9.2019
 *
 */
public class BoxLightTexRendererPP extends GLCanvas implements GLEventListener {

    private static final long serialVersionUID = 1L;

    // taking shader source code files from relative path
    private final String shaderPath = ".\\resources\\";
    // Shader for object 0
//    final String vertexShader0FileName = "O0_Basic.vert";
//    final String fragmentShader0FileName = "O0_Basic.frag";
//    private final String vertexShader0FileName = "BlinnPhongPoint.vert";
//    private final String fragmentShader0FileName = "BlinnPhongPoint.frag";
    private final String vertexShader0FileName = "BlinnPhongPointTex.vert";
    private final String fragmentShader0FileName = "BlinnPhongPointTex.frag";

    private final String vertexShader1FileName = "O1_Basic.vert";
    private final String fragmentShader1FileName = "O1_Basic.frag";

    private final String vertexShader2FileName = "O0_Basic.vert";
    private final String fragmentShader2FileName = "O0_Basic.frag";

    private static final Path objFile = Paths.get("./resources/models/gerade.obj");
    private static final Path objFile1 = Paths.get("./resources/models/L_Links.obj");
    private static final Path objFile2 = Paths.get("./resources/models/quadrat.obj");
    private static final Path objFile3 = Paths.get("./resources/models/podest.obj");
    private static final Path objFile4 = Paths.get("./resources/models/L_Rechts.obj");
    private static final Path objFile5 = Paths.get("./resources/models/versetzt_links.obj");
    private static final Path objFile6 = Paths.get("./resources/models/versetzt_rechts.obj");


    float [] verticies;
    float [] verticies1;
    float [] verticies2;
    float [] verticies3;
    float [] verticies4;
    float [] verticies5;
    float [] verticies6;



    // taking texture files from relative path
    private final String texturePath = ".\\resources\\";
//    final String textureFileName = "GelbGruenPalette.png";
    final String textureFileName = "HSHLLogo1.jpg";
//    final String textureFileName = "HSHLLogo2.jpg";

    private ShaderProgram shaderProgram1;
    private ShaderProgram shaderProgram2;

    // Pointers (names) for data transfer and handling on GPU
    private int[] vaoName;  // Name of vertex array object
    private int[] vboName;	// Name of vertex buffer object
    private int[] iboName;	// Name of index buffer object

    float[] barrey = verticies;
    int block = 28;
    //Startpunkt rechts/links
    float x = -1.5f;
    //Startpunkt oben/unten
    float h = 1.5f;
    //Startpunkt vorne/hinten
    float y = 0;
    //Fallgeschwindigkeit
    float fall = 0.01f;
    boolean start = true;
    public static boolean go = false;
    boolean stay = false;
    float[] barrey1 = verticies;
    int block1 = 28;



    // Define Materials
    private Material material0;

    // Define light sources
    private LightSource light0;

    // Object for handling keyboard and mouse interaction
    private InteractionHandler interactionHandler;
    // Projection model view matrix tool
    private PMVMatrix pmvMatrix;

    /**
     * Standard constructor for object creation.
     */
    public BoxLightTexRendererPP() {
        // Create the canvas with default capabilities
        super();
        // Add this object as OpenGL event listener
        this.addGLEventListener(this);
        createAndRegisterInteractionHandler();
    }

    /**
     * Create the canvas with the requested OpenGL capabilities
     * @param capabilities The capabilities of the canvas, including the OpenGL profile
     */
    public BoxLightTexRendererPP(GLCapabilities capabilities) {
        // Create the canvas with the requested OpenGL capabilities
        super(capabilities);
        // Add this object as an OpenGL event listener
        this.addGLEventListener(this);
        createAndRegisterInteractionHandler();
    }

    /**
     * Helper method for creating an interaction handler object and registering it
     * for key press and mouse interaction callbacks.
     */
    private void createAndRegisterInteractionHandler() {
        // The constructor call of the interaction handler generates meaningful default values
        // Nevertheless the start parameters can be set via setters
        // (see class definition of the interaction handler)
        interactionHandler = new InteractionHandler();
        this.addKeyListener(interactionHandler);
        this.addMouseListener(interactionHandler);
        this.addMouseMotionListener(interactionHandler);
        this.addMouseWheelListener(interactionHandler);
    }

    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * that is called when the OpenGL renderer is started for the first time.
     * @param drawable The OpenGL drawable
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();

        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        // Verify if VBO-Support is available
        if(!gl.isExtensionAvailable("GL_ARB_vertex_buffer_object"))
            System.out.println("Error: VBO support is missing");
        else
            System.out.println("VBO support is available");

        // BEGIN: Preparing scene
        // BEGIN: Allocating vertex array objects and buffers for each object
        int noOfObjects = 35;
        // create vertex array objects for noOfObjects objects (VAO)
        vaoName = new int[noOfObjects];
        gl.glGenVertexArrays(noOfObjects, vaoName, 0);
        if (vaoName[0] < 34)
            System.err.println("Error allocating vertex array object (VAO).");

        // create vertex buffer objects for noOfObjects objects (VBO)
        vboName = new int[noOfObjects];
        gl.glGenBuffers(noOfObjects, vboName, 0);
        if (vboName[0] < 34)
            System.err.println("Error allocating vertex buffer object (VBO).");

        // create index buffer objects for noOfObjects objects (IBO)
        iboName = new int[noOfObjects];
        gl.glGenBuffers(noOfObjects, iboName, 0);
        if (iboName[0] < 27)
            System.err.println("Error allocating index buffer object.");
        // END: Allocating vertex array objects and buffers for each object

        // Initialize objects to be drawn (see respective sub-methods)
        //initObject0(gl);
        //initObject1(gl);
        //initObject2(gl);
        //initObject3(gl);
        //initObject4(gl);
        //initObject5(gl);
        //initObject6(gl);
        initObject7(gl);
        initObject8(gl);
        initObject9(gl);
        //Die Blöcke werden aufgerufen
        initblock1(gl);
        initblock2(gl);
        initblock3(gl);
        initblock4(gl);
        initblock5(gl);
        initblock6(gl);
        initblock7(gl);


        // Specify light parameters
        float[] lightPosition = {0.0f, 3.0f, 3.0f, 1.0f};
        float[] lightAmbientColor = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightDiffuseColor = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightSpecularColor = {1.0f, 1.0f, 1.0f, 1.0f};
        light0 = new LightSource(lightPosition, lightAmbientColor,
                lightDiffuseColor, lightSpecularColor);
        // END: Preparing scene

        // Switch on back face culling
//        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
//        gl.glCullFace(GL.GL_FRONT);
        // Switch on depth test
        gl.glEnable(GL.GL_DEPTH_TEST);

        // defining polygon drawing mode
//        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, gl.GL_FILL);
//        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, gl.GL_LINE);

        // Create projection-model-view matrix
        pmvMatrix = new PMVMatrix();

        // Start parameter settings for the interaction handler might be called here
        interactionHandler.setEyeZ(11);
        // END: Preparing scene
    }

    public void initblock1 (GL3 gl){
        // Loading the vertex and fragment shaders and creation of the shader program.
        gl.glBindVertexArray(vaoName[28]);
        shaderProgram2 = new ShaderProgram(gl);
        shaderProgram2.loadShaderAndCreateProgram(shaderPath,
                vertexShader2FileName, fragmentShader2FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        try {
            verticies = new OBJLoader()
                    .setLoadNormals(true) // tell the loader to also load normal data
                    .loadMesh(Resource.file(objFile)) // actually load the file
                    .getVertices(); // take the vertices from the loaded mesh
        }
        catch (IOException e) {
            throw new RuntimeException(e); }
// Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[28]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, verticies.length * Float.BYTES,
                FloatBuffer.wrap(verticies), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 3* Float.BYTES);
    }

    public void initblock2 (GL3 gl){
        // Loading the vertex and fragment shaders and creation of the shader program.
        gl.glBindVertexArray(vaoName[29]);
        shaderProgram2 = new ShaderProgram(gl);
        shaderProgram2.loadShaderAndCreateProgram(shaderPath,
                vertexShader2FileName, fragmentShader2FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        try {
            verticies1 = new OBJLoader()
                    .setLoadNormals(true) // tell the loader to also load normal data
                    .loadMesh(Resource.file(objFile1)) // actually load the file
                    .getVertices(); // take the vertices from the loaded mesh
        }
        catch (IOException e) {
            throw new RuntimeException(e); }
// Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[29]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, verticies1.length * Float.BYTES,
                FloatBuffer.wrap(verticies1), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 3* Float.BYTES);
    }

    public void initblock3 (GL3 gl){
        // Loading the vertex and fragment shaders and creation of the shader program.
        gl.glBindVertexArray(vaoName[30]);
        shaderProgram2 = new ShaderProgram(gl);
        shaderProgram2.loadShaderAndCreateProgram(shaderPath,
                vertexShader2FileName, fragmentShader2FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        try {
            verticies2 = new OBJLoader()
                    .setLoadNormals(true) // tell the loader to also load normal data
                    .loadMesh(Resource.file(objFile2)) // actually load the file
                    .getVertices(); // take the vertices from the loaded mesh
        }
        catch (IOException e) {
            throw new RuntimeException(e); }
// Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[30]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, verticies2.length * Float.BYTES,
                FloatBuffer.wrap(verticies2), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 3* Float.BYTES);
    }

    public void initblock4 (GL3 gl){
        // Loading the vertex and fragment shaders and creation of the shader program.
        gl.glBindVertexArray(vaoName[31]);
        shaderProgram2 = new ShaderProgram(gl);
        shaderProgram2.loadShaderAndCreateProgram(shaderPath,
                vertexShader2FileName, fragmentShader2FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        try {
            verticies3 = new OBJLoader()
                    .setLoadNormals(true) // tell the loader to also load normal data
                    .loadMesh(Resource.file(objFile3)) // actually load the file
                    .getVertices(); // take the vertices from the loaded mesh
        }
        catch (IOException e) {
            throw new RuntimeException(e); }
// Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[31]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, verticies3.length * Float.BYTES,
                FloatBuffer.wrap(verticies3), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 3* Float.BYTES);
    }

    public void initblock5 (GL3 gl){
        // Loading the vertex and fragment shaders and creation of the shader program.
        gl.glBindVertexArray(vaoName[32]);
        shaderProgram2 = new ShaderProgram(gl);
        shaderProgram2.loadShaderAndCreateProgram(shaderPath,
                vertexShader2FileName, fragmentShader2FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        try {
            verticies4 = new OBJLoader()
                    .setLoadNormals(true) // tell the loader to also load normal data
                    .loadMesh(Resource.file(objFile4)) // actually load the file
                    .getVertices(); // take the vertices from the loaded mesh
        }
        catch (IOException e) {
            throw new RuntimeException(e); }
// Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[32]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, verticies4.length * Float.BYTES,
                FloatBuffer.wrap(verticies4), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 3* Float.BYTES);
    }

    public void initblock6 (GL3 gl){
        // Loading the vertex and fragment shaders and creation of the shader program.
        gl.glBindVertexArray(vaoName[33]);
        shaderProgram2 = new ShaderProgram(gl);
        shaderProgram2.loadShaderAndCreateProgram(shaderPath,
                vertexShader2FileName, fragmentShader2FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        try {
            verticies5 = new OBJLoader()
                    .setLoadNormals(true) // tell the loader to also load normal data
                    .loadMesh(Resource.file(objFile5)) // actually load the file
                    .getVertices(); // take the vertices from the loaded mesh
        }
        catch (IOException e) {
            throw new RuntimeException(e); }
// Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[33]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, verticies5.length * Float.BYTES,
                FloatBuffer.wrap(verticies5), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 3* Float.BYTES);
    }

    public void initblock7 (GL3 gl){
        // Loading the vertex and fragment shaders and creation of the shader program.
        gl.glBindVertexArray(vaoName[34]);
        shaderProgram2 = new ShaderProgram(gl);
        shaderProgram2.loadShaderAndCreateProgram(shaderPath,
                vertexShader2FileName, fragmentShader2FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        try {
            verticies6 = new OBJLoader()
                    .setLoadNormals(true) // tell the loader to also load normal data
                    .loadMesh(Resource.file(objFile6)) // actually load the file
                    .getVertices(); // take the vertices from the loaded mesh
        }
        catch (IOException e) {
            throw new RuntimeException(e); }
// Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[34]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, verticies6.length * Float.BYTES,
                FloatBuffer.wrap(verticies6), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 3* Float.BYTES);
    }

    /**
     * Initializes the GPU for drawing object0
     * @param gl OpenGL context
     */

    /**
     * Initializes the GPU for drawing object1
     * @param gl OpenGL context
     */

    /*
    Die nachfolgenden Zeilen (598-871) waren unsere erste version um Spielsteine zu erstellen
      diese wurden durch den Objektloader abgelöst, sind jedoch noch zu Dokumentationszwecken in dem
      Quellcode vorhanden

   //BLock Lang gerade
    private void initObject0(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[0]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color2 = {0.5f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color2);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[0]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[0]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }

   //Block L Links
    private void initObject1(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[1]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {0.5f, 0.8f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[1]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[1]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }

   //Block L Rechts
    private void initObject2(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[2]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {0.8f, 0.7f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[2]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[2]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }

    //Block Viereck
    private void initObject3(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[3]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {1f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[3]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[3]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }

    //Block Podest
    private void initObject4(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[4]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {1.5f, 0.5f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.5f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[4]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[4]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }

    //Block versetzt rechts
    private void initObject5(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[5]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {0.1f, 0.2f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[5]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[5]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }

    //Block versetzt links
    private void initObject6(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[6]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {1f, 0.9f, 0.5f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.4f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[6]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[6]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }
    */

    //initObject7 - initObject9 = Blöcke für das Spielfeld
    private void initObject7(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[7]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {1f, 1f, 1f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0.5f, 0.0f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[7]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[7]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }
    private void initObject8(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[8]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {1f, 1f, 1f};
        float[] cubeVertices = Box.makeBoxVertices(0f, 0.5f, 0.5f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[8]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[8]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }
    private void initObject9(GL3 gl) {
        // BEGIN: Prepare cube for drawing (object 1)
        gl.glBindVertexArray(vaoName[9]);
        shaderProgram1 = new ShaderProgram(gl);
        shaderProgram1.loadShaderAndCreateProgram(shaderPath,
                vertexShader1FileName, fragmentShader1FileName);

        float[] color1 = {1f, 1f, 1f};
        float[] cubeVertices = Box.makeBoxVertices(0.5f, 0f, 0.5f, color1);
        int[] cubeIndices = Box.makeBoxIndicesForTriangleStrip();

        // activate and initialize vertex buffer object (VBO)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[9]);
        // floats use 4 bytes in Java
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        // activate and initialize index buffer object (IBO)
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[9]);
        // integers use 4 bytes in Java
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length * 4,
                IntBuffer.wrap(cubeIndices), GL.GL_STATIC_DRAW);

        // Activate and order vertex buffer object data for the vertex shader
        // The vertex buffer contains: position (3), color (3), normals (3)
        // Defining input for vertex shader
        // Pointer for the vertex shader to the position information per vertex
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 9*4, 0);
        // Pointer for the vertex shader to the color information per vertex
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 9*4, 3*4);
        // Pointer for the vertex shader to the normal information per vertex
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 9*4, 6*4);
        // END: Prepare cube for drawing
    }




    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * called by the OpenGL animator for every frame.
     * @param drawable The OpenGL drawable
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        // Background color of the canvas
        gl.glClearColor(0f, 0f, 0f, 0f);

        // For monitoring the interaction settings
/*        System.out.println("Camera: z = " + interactionHandler.getEyeZ() + ", " +
                "x-Rot: " + interactionHandler.getAngleXaxis() +
                ", y-Rot: " + interactionHandler.getAngleYaxis() +
                ", x-Translation: " + interactionHandler.getxPosition()+
                ", y-Translation: " + interactionHandler.getyPosition());// definition of translation of model (Model/Object Coordinates --> World Coordinates)
*/
        // Using the PMV-Tool for geometric transforms
        pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
        pmvMatrix.glLoadIdentity();
        // Setting the camera position, based on user input
        pmvMatrix.gluLookAt(0f, 0f, interactionHandler.getEyeZ(),
                0f, 0f, 0f,
                0f, 1.0f, 0f);
        pmvMatrix.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
        pmvMatrix.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
        pmvMatrix.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

        // Transform for the complete scene
//        pmvMatrix.glTranslatef(1f, 0f, 0f);

        // Position of one light for all shapes
        float[] lightPos = {0f, 3f, 0f};

        /* Die nachfolgenden Zeilen (1015-1214) waren unsere erste version um Spielsteine zu erstellen
      diese wurden durch den Objektloader abgelöst, sind jedoch noch zu Dokumentationszwecken in dem
      Quellcode vorhanden
        /*Block lang gerade
        pmvMatrix.glPushMatrix();
       pmvMatrix.glTranslatef(0f, 0f, 0f);
        displayObject0(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.5f, 0f, 0f);
        displayObject0(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(1f, 0f, 0f);
        displayObject0(gl);
        pmvMatrix.glPopMatrix();*/




        /*Block L Links
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(2.5f, 0f, 0f);
        displayObject1(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(3f, 0f, 0f);
        displayObject1(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(3.5f, 0f, 0f);
        displayObject1(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(2.5f, 0.5f, 0f);
        displayObject1(gl);
        pmvMatrix.glPopMatrix();*/



        /*Block L Rechts
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 0f, 0f);
        displayObject2(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, 0f, 0f);
        displayObject2(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, 0f, 0f);
        displayObject2(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 0.5f, 0f);
        displayObject2(gl);
        pmvMatrix.glPopMatrix();*/


        /*Block Quadrat
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, blockY, 0f);
        displayObject3(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, blockX, 0f);
        displayObject3(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, blockY, 0f);
        displayObject3(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, blockX, 0f);
        displayObject3(gl);
        pmvMatrix.glPopMatrix();

        if (blockX>= -1.5){

            blockX = blockX - direction;

        }

        if (blockY >= -2){

            blockY= blockY -direction;
        }*/



        /*Block Podest
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0f, -2f, 0f);
        displayObject4(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.5f, -2f, 0f);
        displayObject4(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(1f, -2f, 0f);
        displayObject4(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.5f, -1.5f, 0f);
        displayObject4(gl);
        pmvMatrix.glPopMatrix();*/


        /*Block Versetzt Rechts
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(2f, -2f, 0f);
        displayObject5(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(2.5f, -2f, 0f);
        displayObject5(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(2.5f, -1.5f, 0f);
        displayObject5(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(3f, -1.5f, 0f);
        displayObject5(gl);
        pmvMatrix.glPopMatrix();*/


        /*Block Versetzt Links
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(4.5f, -2f, 0f);
        displayObject6(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(5f, -2f, 0f);
        displayObject6(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(4.5f, -1.5f, 0f);
        displayObject6(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(4f, -1.5f, 0f);
        displayObject6(gl);
        pmvMatrix.glPopMatrix();*/

        /*Block 1
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.25f, 0f, -0.25f);
        displayBlock1(gl);
        pmvMatrix.glPopMatrix();*/


        /*Block 2
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.25f, 0f, 0f);
        displayBlock2(gl);
        pmvMatrix.glPopMatrix();

        if (block1>= -3.83){

            block1 = block1 - direction;

        }

        //Block 3
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.25f, 0f, 0f);
        displayBlock3(gl);
        pmvMatrix.glPopMatrix();

        //Block 4
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.25f, 0f, -1f);
        displayBlock4(gl);
        pmvMatrix.glPopMatrix();

        //Block 5
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(0.25f, 0f, -1f);
        displayBlock5(gl);
        pmvMatrix.glPopMatrix();*/




        //Spielfeld


       //Rückseite des Spielfelds wird erstellt

        //Reihe 1
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, -2f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, -2f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, -2f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 2
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, -1.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, -1.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, -1.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 3
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, -1f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, -1f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, -1f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 4
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, -0.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, -0.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, -0.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 5
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, 0f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, 0f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 0f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 6
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, 0.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, 0.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 0.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 7
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, 1f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, 1f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 1f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 8
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, 1.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, 1.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 1.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 9
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, 2f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, 2f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 2f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 10
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, 2.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, 2.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, 2.5f, -0.5f);
        displayObject7(gl);
        pmvMatrix.glPopMatrix();







        //Seite des Spielfelds wird erstellt

        //Reihe 1
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -2f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -2f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -2f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 2
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -1.5f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -1.5f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -1.5f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 3
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -1f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -1f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -1f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 4
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -0.5f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -0.5f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, -0.5f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 5
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 0f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 0f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 0f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 6
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 0.5f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 0.5f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 0.5f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 7
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 1f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 1f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 1f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 8
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 1.5f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 1.5f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 1.5f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();



        //Reihe 9
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 2f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 2f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 2f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();


        //Reihe 10
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 2.5f, -0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 2.5f, 0.25f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2.25f, 2.5f, 0.75f);
        displayObject8(gl);
        pmvMatrix.glPopMatrix();



        //Boden des Spielfelds wird erstellt
        //Reihe 1
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, -2.25f, -0.25f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, -2.25f, 0.25f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-2f, -2.25f, 0.75f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        //Reihe 2
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, -2.25f, -0.25f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, -2.25f, 0.25f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1.5f, -2.25f, 0.75f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        //Reihe 3
        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, -2.25f, -0.25f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, -2.25f, 0.25f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(-1f, -2.25f, 0.75f);
        displayObject9(gl);
        pmvMatrix.glPopMatrix();


        /*Koordinaten für die letzte Position des fallenden Spielsteines
        wird festgelegt */
        if (stay) {

            block=block1;
            barrey=barrey1;

            pmvMatrix.glPushMatrix();
            pmvMatrix.glTranslatef(-1.5f, -2.25f, 0);
            displayBlock1(gl);
            pmvMatrix.glPopMatrix();
        }
        if (go){



       random();

        pmvMatrix.glPushMatrix();
        pmvMatrix.glTranslatef(x, h, y);
        displayBlock1(gl);
        pmvMatrix.glPopMatrix();
        }

        fallen();


    }
//fallen Methode um den Endpunkt der Blöcke zu definieren
    public void fallen(){
        h = h - fall;

        if (h<= -2.25f){
            fall = fall*0;


        start=true;
        stay=true;}
    }


//random Methode um die Blöcke zufällig zu laden
    public void random(){


        if(start==true) {

            fall=0.01f;
            h=3;
            double rand = Math.floor(Math.random() * 7);

            if (rand == 0) {
                block = 28;
                barrey = verticies;
                block1 = 28;
                barrey1 = verticies;
                start = false;
            } else if (rand == 1) {
                block = 29;
                barrey = verticies1;
                block1 = 29;
                barrey1 = verticies1;
                start = false;

            } else if (rand == 2) {
                block = 30;
                barrey = verticies2;
                block1 = 30;
                barrey1 = verticies2;
                start = false;
            } else if (rand == 3) {
                block = 31;
                barrey = verticies3;
                block1 = 31;
                barrey1 = verticies3;
                start = false;
            } else if (rand == 4) {
                block = 32;
                barrey = verticies4;
                block1 = 32;
                barrey1 = verticies4;
                start = false;}
            else if (rand == 5) {
                block = 33;
                barrey = verticies5;
                block1 = 33;
                barrey1 = verticies5;
                start = false;}
            else if (rand == 6) {
                block = 34;
                barrey = verticies6;
                block1 = 34;
                barrey1 = verticies6;
                start = false;}


        }

    }




    //Block lang gerade
    private void displayObject0(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[0]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }
    private void displayObject1(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[1]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }
    private void displayObject2(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[2]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }
    private void displayObject3(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[3]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }


    //Block L Links
    private void displayObject4(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[4]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }
    private void displayObject5(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[5]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }
    private void displayObject6(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[6]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }

    private void displayBlock1(GL3 gl){
        gl.glUseProgram(shaderProgram2.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[block]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, barrey.length);
    }

   /* private void displayBlock2(GL3 gl){
        gl.glUseProgram(shaderProgram2.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[29]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, verticies1.length);
    }

    private void displayBlock3(GL3 gl){
        gl.glUseProgram(shaderProgram2.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[30]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, verticies2.length);
    }

    private void displayBlock4(GL3 gl){
        gl.glUseProgram(shaderProgram2.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[31]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, verticies3.length);
    }*/

    /*private void displayBlock5(GL3 gl){
        gl.glUseProgram(shaderProgram2.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[block]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, barrey.length);*/


    //Spielfeld
    //Spielfeld hinten
    private void displayObject7(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[7]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }
    //Spielfeld links
    private void displayObject8(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[8]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }
    //Spielfeld unten
    private void displayObject9(GL3 gl) {
        gl.glUseProgram(shaderProgram1.getShaderProgramID());
        // Transfer the PVM-Matrix (model-view and projection matrix) to the vertex shader
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glBindVertexArray(vaoName[9]);
        // Draws the elements in the order defined by the index buffer object (IBO)
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, Box.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }



    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * called when the OpenGL window is resized.
     * @param drawable The OpenGL drawable
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();
        System.out.println("Reshape called.");
        System.out.println("x = " + x + ", y = " + y + ", width = " + width + ", height = " + height);

        pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.gluPerspective(45f, (float) width/ (float) height, 0.01f, 10000f);
    }

    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * called when OpenGL canvas ist destroyed.
     * @param drawable
     */
    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("Deleting allocated objects, incl. shader program.");
        GL3 gl = drawable.getGL().getGL3();

        // Detach and delete shader program
        gl.glUseProgram(0);
        shaderProgram1.deleteShaderProgram();
        shaderProgram2.deleteShaderProgram();

        // deactivate VAO and VBO
        gl.glBindVertexArray(0);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);

        gl.glDisable(GL.GL_CULL_FACE);
        gl.glDisable(GL.GL_DEPTH_TEST);

        System.exit(0);
    }
}


