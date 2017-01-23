package com.example.kidsalphabetsar.ArTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.kidsalphabetsar.R;
import com.example.kidsalphabetsar.SampleApplication.SampleApplicationControl;
import com.example.kidsalphabetsar.SampleApplication.SampleApplicationException;
import com.example.kidsalphabetsar.SampleApplication.SampleApplicationSession;
import com.example.kidsalphabetsar.SampleApplication.utils.LoadingDialogHandler;
import com.example.kidsalphabetsar.SampleApplication.utils.SampleApplicationGLView;
import com.example.kidsalphabetsar.SampleApplication.utils.Texture;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.HINT;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.HashMap;
import java.util.Vector;

public class ArScannerActivity extends AppCompatActivity implements
        SampleApplicationControl, View.OnClickListener{

    private static final String LOGTAG = "VideoPlayback";

    SampleApplicationSession vuforiaAppSession;

    Activity mActivity;

    // Movie for the Targets:
    public static final int NUM_TARGETS = 2;
    public static final int ALPHA_A = 0;
    public static final int ALPHA_B = 1;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private TargetRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private HashMap<String,Vector> textureHashMap = new HashMap<>();

    DataSet dataSetStonesAndChips = null;

    private RelativeLayout mUILayout;
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
            this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    boolean mIsDroidDevice = false;
    boolean mIsInitialized = false;

    public interface ButtonRightClickListener{
        void onRightClick();
    }

    public interface ButtonLeftClickListener{
        void onLeftClick();
    }

    private static ButtonLeftClickListener mButtonLeftClickListener;
    private static ButtonRightClickListener mButtonRightClickListener;

    private ImageButton mButtonRight, mButtonLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vuforiaAppSession = new SampleApplicationSession(this);

        /*try {
            images=mActivity.getAssets().list(letter);
            listTextures = new ArrayList<>(Arrays.asList(images));

            for (String image : listTextures) {
                String fullpath = letter + "/";
                mTextures.add(Texture.loadTextureFromApk(fullpath + image, mActivity.getAssets()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            mTextures.add(Texture.loadTextureFromApk("note/modi.png", mActivity.getAssets()));
        }*/

        mActivity = this;

        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        //loadTextures();

    }

    public void setRightClickListener(ButtonRightClickListener mRightClickListener){
        this.mButtonRightClickListener = mRightClickListener;
    }

    public void setLeftClickListener(ButtonLeftClickListener mLeftClickListener){
        this.mButtonLeftClickListener = mLeftClickListener;
    }

    private void loadTextures() {
        /*for (String image : listTextures) {
            String fullpath = letter + "/";
            mTextures.add(Texture.loadTextureFromApk(fullpath + image, getAssets()));
        }*/
        mTextures.add(Texture.loadTextureFromApk("a/a_kutra.png", getAssets()));
    }

    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }

    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

    }

    // The final call you receive before your activity is destroyed.
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }

    private void startLoadingAnimation()
    {
        View layout = View.inflate(this, R.layout.camera_overlay,
                null);

        mUILayout = (RelativeLayout) layout;

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        mButtonRight = (ImageButton)layout.findViewById(R.id.rightArrow);
        mButtonLeft = (ImageButton)layout.findViewById(R.id.leftArrow);

        mButtonRight.setOnClickListener(this);
        mButtonLeft.setOnClickListener(this);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.rightArrow){
            if(mButtonRightClickListener != null)
                mButtonRightClickListener.onRightClick();
        }else if(v.getId() == R.id.leftArrow){
            if(mButtonLeftClickListener != null)
                mButtonLeftClickListener.onLeftClick();
        }
    }

    // We do not handle the touch event here, we just forward it to the
    // gesture detector
    public boolean onTouchEvent(MotionEvent event)
    {
        if(mRenderer != null)
            mRenderer.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        // Initialize the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker tracker = trackerManager.initTracker(ObjectTracker
                .getClassType());
        if (tracker == null)
        {
            Log.d(LOGTAG, "Failed to initialize ObjectTracker.");
            result = false;
        }

        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
        {
            Log.d(
                    LOGTAG,
                    "Failed to load tracking data set because the ObjectTracker has not been initialized.");
            return false;
        }

        // Create the data sets:
        dataSetStonesAndChips = objectTracker.createDataSet();

        //dataSetAlphabets = objectTracker.createDataSet();

        if (dataSetStonesAndChips == null )
        {
            Log.d(LOGTAG, "Failed to create a new tracking data.");
            return false;
        }

        //StonesAndChips.xml
        // FirstTest

        // Load the data sets:
        if (!dataSetStonesAndChips.load("kidsAlphabets.xml",
                STORAGE_TYPE.STORAGE_APPRESOURCE) )
        {
            Log.d(LOGTAG, "Failed to load data set.");
            return false;
        }

        // Activate the data set:
        if (!objectTracker.activateDataSet(dataSetStonesAndChips))
        {
            Log.d(LOGTAG, "Failed to activate data set.");
            return false;
        }

        Log.d(LOGTAG, "Successfully loaded and activated data set.");
        return true;
    }

    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
        {
            objectTracker.start();
            Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 9);
        } else
            result = false;

        return result;
    }

    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();
        else
            result = false;

        return result;
    }

    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
        {
            Log.d(
                    LOGTAG,
                    "Failed to destroy the tracking data set because the ObjectTracker has not been initialized.");
            return false;
        }

        if (dataSetStonesAndChips != null)
        {
            if ((objectTracker.getActiveDataSet() == dataSetStonesAndChips
                    && !objectTracker.deactivateDataSet(dataSetStonesAndChips)))
            {
                Log.d(
                        LOGTAG,
                        "Failed to destroy the tracking data set StonesAndChips because the data set could not be deactivated.");
                result = false;
            } else if (!objectTracker.destroyDataSet(dataSetStonesAndChips))
            {
                Log.d(LOGTAG,
                        "Failed to destroy the tracking data set StonesAndChips.");
                result = false;
            }

            dataSetStonesAndChips = null;
        }

        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        // Deinit the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        trackerManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    @Override
    public void onInitARDone(SampleApplicationException exception) {
        if (exception == null)
        {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Hides the Loading Dialog
            loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

            // Sets the layout background to transparent
            //mUILayout.setBackgroundColor(Color.TRANSPARENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mUILayout.setBackground(getResources().getDrawable(R.drawable.overlay));
            }else{
                mUILayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.overlay));
            }

            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result)
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            /*mSampleAppMenu = new SampleAppMenu(this, this, "Video Playback",
                mGlView, mUILayout, null);
            setSampleAppMenuSettings();*/

            mIsInitialized = true;

        } else
        {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }

    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
       /* mGlView.setEGLContextClientVersion( 2 );
        mGlView.setZOrderOnTop( true );
        mGlView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
        mGlView.getHolder().setFormat( PixelFormat.RGBA_8888 );*/

        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new TargetRenderer(this, vuforiaAppSession);
        //mRenderer.setTextures(mTextures);

        mGlView.setRenderer(mRenderer);

    }

    @Override
    public void onVuforiaUpdate(State state) {
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ArScannerActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }

}
