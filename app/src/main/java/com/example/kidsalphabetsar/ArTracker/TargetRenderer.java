package com.example.kidsalphabetsar.ArTracker;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.example.kidsalphabetsar.SampleApplication.SampleApplicationSession;
import com.example.kidsalphabetsar.SampleApplication.utils.SampleUtils;
import com.example.kidsalphabetsar.SampleApplication.utils.Texture;
import com.example.kidsalphabetsar.Util.Sprite;
import com.vuforia.ImageTarget;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by User on 29-Dec-16.
 */

public class TargetRenderer implements GLSurfaceView.Renderer, ArScannerActivity.ButtonLeftClickListener,
        ArScannerActivity.ButtonRightClickListener
{

    private static final String LOGTAG = "VideoPlaybackRenderer";

    private SampleApplicationSession vuforiaAppSession;

    // Keyframe and icon rendering specific
    private int keyframeShaderID = 0;
    private int keyframeVertexHandle = 0;
    private int keyframeNormalHandle = 0;
    private int keyframeTexCoordHandle = 0;
    private int keyframeMVPMatrixHandle = 0;
    private int keyframeTexSampler2DHandle = 0;

    // Trackable dimensionsg
    private Vec3F targetPositiveDimensions[] = new Vec3F[ArScannerActivity.NUM_TARGETS];

    private static int NUM_QUAD_INDEX = 6;

    private double quadVerticesArray[] = {
            -1.0f, -1.0f, 0.4f,
            1.0f, -1.0f, 0.4f,
            1.0f, 1.0f, 0.4f,
            -1.0f, 1.0f, 0.4f };

    private double quadTexCoordsArray[] = {
            0.0f, 0.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f, 1.0f };

    private double quadNormalsArray[] = {
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1, };

    private short quadIndicesArray[] = { 0, 1, 2, 2, 3, 0 };

    private Buffer quadVertices, quadTexCoords, quadIndices, quadNormals;

    private boolean mIsActive = false, isInitRenderer = false;

    private ArScannerActivity mActivity;

    // Needed to calculate whether a screen tap is inside the target
    private Matrix44F modelViewMatrix[] = new Matrix44F[ArScannerActivity.NUM_TARGETS];

    private Vector<Texture> mTextures = new Vector<>();

    private boolean isTracking[] = new boolean[ArScannerActivity.NUM_TARGETS];

    private int currentTexture = 0;
    private float touchStartX;
    private String letter;
    private String[] images, soundList = null;
    private ArrayList<String> listTextures;

    private ImageTarget imageTarget;

    private float[] modelViewMatrixButton, modelViewProjectionButton;
    private int currentTarget = 0;
    private String prevLetter = "";

    private Sprite mSprite;
    private MediaPlayer mMediaPlayer;
    private String filename = "";
    private boolean isPatternFound = false;

    private String targetName = "";

    public TargetRenderer(ArScannerActivity arScannerActivity,SampleApplicationSession vuforiaAppSession) {
        mActivity = arScannerActivity;
        this.vuforiaAppSession = vuforiaAppSession;

        mMediaPlayer = new MediaPlayer();
        mSprite = new Sprite();

        mActivity.setLeftClickListener(this);
        mActivity.setRightClickListener(this);

        for (int i = 0; i < ArScannerActivity.NUM_TARGETS; i++)
            targetPositiveDimensions[i] = new Vec3F();

        for (int i = 0; i < ArScannerActivity.NUM_TARGETS; i++)
            modelViewMatrix[i] = new Matrix44F();

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // Define clear color

        keyframeShaderID = SampleUtils.createProgramFromShaderSrc(
                KeyFrameShaders.KEY_FRAME_VERTEX_SHADER,
                KeyFrameShaders.KEY_FRAME_FRAGMENT_SHADER);
        keyframeVertexHandle = GLES20.glGetAttribLocation(keyframeShaderID,
                "vertexPosition");
        keyframeNormalHandle = GLES20.glGetAttribLocation(keyframeShaderID,
                "vertexNormal");
        keyframeTexCoordHandle = GLES20.glGetAttribLocation(keyframeShaderID,
                "vertexTexCoord");
        keyframeMVPMatrixHandle = GLES20.glGetUniformLocation(keyframeShaderID,
                "modelViewProjectionMatrix");
        keyframeTexSampler2DHandle = GLES20.glGetUniformLocation(
                keyframeShaderID, "texSampler2D");

        quadVertices = fillBuffer(quadVerticesArray);
        quadIndices = fillBuffer(quadIndicesArray);
        quadNormals = fillBuffer(quadNormalsArray);
        quadTexCoords = fillBuffer(quadTexCoordsArray);

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        Vuforia.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Call Vuforia function to handle render surface size changes:
        Vuforia.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        targetName = "";

        //glClearColor(0.0f, 0.0f, 0.0f,1.0f);

        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = Renderer.getInstance().begin();

        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();

       // GLES20.glClearColor(10.0f, 0.0f, 10.0f,1.0f);

        // Call our function to render content
        // Get the state from Vuforia and mark the beginning of a rendering
        // section

        float temp[] = { 0.0f, 0.0f, 0.0f };
        for (int i = 0; i < ArScannerActivity.NUM_TARGETS; i++)
        {
            isTracking[i] = false;
            targetPositiveDimensions[i].setData(temp);
        }

        isPatternFound = false;

        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {

            isPatternFound = true;
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);

            imageTarget = (ImageTarget) trackableResult
                    .getTrackable();

            //System.out.println("Trackable Result name: " + imageTarget.getName());

            modelViewMatrix[currentTarget] = Tool
                    .convertPose2GLMatrix(trackableResult.getPose());

            isTracking[currentTarget] = true;

            targetPositiveDimensions[currentTarget] = imageTarget.getSize();

            System.out.println("targe array " + targetPositiveDimensions[currentTarget].getData()[0] + " , " + targetPositiveDimensions[currentTarget].getData()[1]);

            // The pose delivers the center of the target, thus the dimensions
            // go from -width/2 to width/2, same for height
            /*temp[0] = targetPositiveDimensions[currentTarget].getData()[0] / 1.0f;
            temp[1] = targetPositiveDimensions[currentTarget].getData()[1] / 1.0f;*/

            temp[0] = targetPositiveDimensions[currentTarget].getData()[0] + 50.0f;
            temp[1] = targetPositiveDimensions[currentTarget].getData()[1] + 50.0f;

            targetPositiveDimensions[currentTarget].setData(temp);

            // If the movie is ready to be played, targetPositiveDimensions pause, has reached end or
            // is not
            // ready then we display one of the icons
            modelViewMatrixButton = Tool.convertPose2GLMatrix(
                    trackableResult.getPose()).getData();
            modelViewProjectionButton = new float[16];

            String currentLetter = imageTarget.getName();

            targetName = currentLetter;

            if((mTextures.size()) == 0 || (!prevLetter.equals(currentLetter))){
                prevLetter = currentLetter;
                loadTextures(imageTarget.getName());
            }

            /*if(!isInitRenderer && currentLetter.equals(prevLetter)){
                System.out.println("if block 1");
                startSound(filename,currentTexture);
                isInitRenderer = false;
            }*/

            /*if(mTextures.size() > 0 && isInitRenderer){
                System.out.println("if block 2");
                isInitRenderer = false;
            }*/

            if(mTextures.size() > 0)
                renderFrame();
        }

        if(!targetName.equalsIgnoreCase("")){
            if(isInitRenderer){
                startSound(filename,currentTexture);
            }
            isInitRenderer = false;
        }else{
            isInitRenderer = true;
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        Renderer.getInstance().end();
    }

    @SuppressLint("InlinedApi")
    void initRendering()
    {
        Log.d(LOGTAG, "VideoPlayback VideoPlaybackRenderer initRendering");

        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        //GLES20.glClearColor(10.0f, 0.0f, 0.0f, 0.0f);

        System.out.println("Texture size: " + mTextures.size());

        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            // Here we create the textures for the keyframe
            // and for all the icons
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        isInitRenderer = true;
    }

    private Buffer fillBuffer(double[] array)
    {
        // Convert to floats because OpenGL doesnt work on doubles, and manually
        // casting each input value would take too much time.
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length); // each
        // float
        // takes 4
        // bytes
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (double d : array)
            bb.putFloat((float) d);
        bb.rewind();

        return bb;
    }

    private Buffer fillBuffer(short[] array)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(2 * array.length); // each
        // short
        // takes 2
        // bytes
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : array)
            bb.putShort(s);
        bb.rewind();

        return bb;
    }

    private Buffer fillBuffer(float[] array)
    {
        // Convert to floats because OpenGL doesnt work on doubles, and manually
        // casting each input value would take too much time.
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length); // each
        // float
        // takes 4
        // bytes
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();

        return bb;
    }

    @SuppressLint("InlinedApi")
    void renderFrame()
    {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Set the viewport
        int[] viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

            GLES20.glDepthFunc(GLES20.GL_LEQUAL);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
                    GLES20.GL_ONE_MINUS_SRC_ALPHA);

            Matrix
                    .translateM(
                            modelViewMatrixButton,
                            0,
                            0.0f,
                            0.0f,
                            targetPositiveDimensions[currentTarget].getData()[1] / 10f);
            Matrix
                    .scaleM(
                            modelViewMatrixButton,
                            0,
                            (targetPositiveDimensions[currentTarget].getData()[1] / 1.5f),
                            (targetPositiveDimensions[currentTarget].getData()[1] / 1.5f),
                            (targetPositiveDimensions[currentTarget].getData()[1] / 1.5f));

            Matrix.multiplyMM(modelViewProjectionButton, 0,
                    vuforiaAppSession.getProjectionMatrix().getData(), 0,
                    modelViewMatrixButton, 0);

            GLES20.glUseProgram(keyframeShaderID);

            GLES20.glVertexAttribPointer(keyframeVertexHandle, 3,
                    GLES20.GL_FLOAT, false, 0, quadVertices);
            GLES20.glVertexAttribPointer(keyframeNormalHandle, 3,
                    GLES20.GL_FLOAT, false, 0, quadNormals);
            GLES20.glVertexAttribPointer(keyframeTexCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, quadTexCoords);

            GLES20.glEnableVertexAttribArray(keyframeVertexHandle);
            GLES20.glEnableVertexAttribArray(keyframeNormalHandle);
            GLES20.glEnableVertexAttribArray(keyframeTexCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(currentTexture).mTextureID[0]);

            GLES20.glUniformMatrix4fv(keyframeMVPMatrixHandle, 1, false,
                    modelViewProjectionButton, 0);
            GLES20.glUniform1i(keyframeTexSampler2DHandle, 0);

            // Render
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, NUM_QUAD_INDEX,
                    GLES20.GL_UNSIGNED_SHORT, quadIndices);

            GLES20.glDisableVertexAttribArray(keyframeVertexHandle);
            GLES20.glDisableVertexAttribArray(keyframeNormalHandle);
            GLES20.glDisableVertexAttribArray(keyframeTexCoordHandle);

            GLES20.glUseProgram(0);

            // Finally we return the depth func to its original state
            GLES20.glDepthFunc(GLES20.GL_LESS);
            GLES20.glDisable(GLES20.GL_BLEND);
            //}

            SampleUtils.checkGLError("VideoPlayback renderFrame");
    }

    private void loadTextures(String currentLetter){
        System.out.println("current letter");
        currentTexture = 0;
        mTextures.clear();
        letter = currentLetter;
        try {
            // sounds
            filename = letter + "_sound";
            soundList = mActivity.getAssets().list(filename);

            images=mActivity.getAssets().list(letter);
            listTextures = new ArrayList<>(Arrays.asList(images));

            for (String image : listTextures) {
                String fullpath = letter + "/";
                mTextures.add(Texture.loadTextureFromApk(fullpath + image, mActivity.getAssets()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            mTextures.add(Texture.loadTextureFromApk("note/modi.png", mActivity.getAssets()));
        }

        //mTextures.add(Texture.loadTextureFromApk("note/modi.png", mActivity.getAssets()));

        initRendering();

        //renderFrame();

        /*GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(currentTexture).mTextureID[0]);*/
    }

    private void startSound(String filename,int value){
        AssetFileDescriptor afd;

            try {
                afd = mActivity.getAssets().openFd(filename + "/" + soundList[value]);
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } else {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void onLeftClick() {
        leftSwipe();
    }

    @Override
    public void onRightClick() {
        rightSwipe();
    }

    protected void processTouchEvent(MotionEvent event){

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float touchEndX = event.getX();
                float meanDiff = touchEndX - touchStartX;
                if ((touchStartX > touchEndX) && (meanDiff < -70)) {
                    rightSwipe();
                } else if ((touchStartX < touchEndX) && (meanDiff > 70)) {
                    leftSwipe();
                }
                break;
        }
    }

    private void leftSwipe(){
        if(currentTexture > 0 && isPatternFound){
            currentTexture--;
            startSound(filename,currentTexture);
        }
    }

    private void rightSwipe(){
        if(currentTexture < (mTextures.size() - 1) && isPatternFound){
            currentTexture++;
            startSound(filename,currentTexture);
        }
    }
}
