package com.imennmn.camera2example;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static android.media.MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_METADATA_UPDATE;
import static android.media.MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT;
import static android.media.MediaPlayer.MEDIA_INFO_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;
import static com.imennmn.camera2example.ApplicationCommonValues.PLAY_VIDEO;
import static com.imennmn.camera2example.ApplicationCommonValues.RECORD_VIDEO;


/**
 * Created by imen_nmn on 08/09/16.
 */
public class VideoRecordManager implements SurfaceHolder.Callback,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnInfoListener, Camera.PictureCallback, MediaPlayer.OnSeekCompleteListener {
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static String TAG = "CreateTAG-VideoM";
    private static int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    private static int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
    boolean previewing = false;
    private Camera mCamera;
    private int cameraId;
    //    private SurfaceHolder mHolder;
    private SurfaceView mPreview;
    private MediaRecorder mediaRecorder;
    private boolean recording = false;
    private Activity mActivity;

    private static final String VIDEO_FILE_BASE = "video_android.mp4";
    private static final String PICTURE_FILE_BASE = "photo_android.png";

    private String mediaServerUrl = "";

    private String mediaPath;
    private int cameraFacing = CAMERA_FACING_FRONT;
    private VideoEventListener videoRecordCallback;
    private int videoEvent = RECORD_VIDEO;
    private int mVideoWidth;
    private int mVideoHeight;
    private MediaPlayer mMediaPlayer;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    private RawCallback mRawCallback;
    private boolean takePictureInvoked = false;
    private boolean isFromServer = false;
    private Camera.Size preferredSize;
    private boolean addMirrorEffect = false;


    public VideoRecordManager(Activity activity, SurfaceView surfaceView) {
        Log.i(TAG, "stopCamera");
        mActivity = activity;
        mediaPath = Environment.getExternalStorageDirectory() + "/" + activity.getApplication().getPackageName();
        // create a File object for the parent directory
        File folder = new File(mediaPath);
        // have the object build the directory structure, if needed.
        folder.mkdirs();
        mPreview = surfaceView;
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mRawCallback = new RawCallback();
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public Bitmap mirrorEffect(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        matrix.postTranslate(bitmap.getWidth(), 0);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    public SurfaceView getCameraPreview() {
        return mPreview;
    }

    public void setVideoRecordCallback(VideoEventListener callback) {
        this.videoRecordCallback = callback;
    }

    public void takePicture() {
        Log.i(TAG, "takePicture  " + takePictureInvoked);
        if (!takePictureInvoked && mCamera != null) {
            takePictureInvoked = true;
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mRawCallback = new RawCallback();
                    try {
                        camera.takePicture(mRawCallback, mRawCallback, null,
                                VideoRecordManager.this); //ToDO
                    } catch (Exception ex) {
                        takePicture();
                    }
                }
            });
        }
    }

    public void captureImageFromVideo() {
        Log.d(TAG, "captureImageFromVideo");

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        // Set data source to retriever.
        // From your code, you might want to use your 'String path' here.
        try {
            retriever.setDataSource(getVideoPath());
            // Get a frame in Bitmap by specifying time.
            // Be aware that the parameter must be in "microseconds", not milliseconds.
            Bitmap capturedImage = retriever.getFrameAtTime(300);

            /****************/
            if (capturedImage != null) {

                deleteMediaRecordedFile(getPicturePath());
                // create a File object for the output file
                File pictureFile = new File(getPicturePath());

                if (pictureFile.exists()) {
                    pictureFile.delete();
                }

                FileOutputStream fos = new FileOutputStream(pictureFile);
                boolean bo = capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                fos.close();
                Log.d(TAG, bo + "");
            }

        } catch (Exception e) {
            Log.d(TAG, "captureImageFromVideo IllegalStateException Error accessing file: " + e.getMessage());
        }
        /***********/
    }

    public void resetCamera(SurfaceHolder holder) {
        takePictureInvoked = false;
        if (findFacingCamera(CAMERA_FACING_BACK) < 0) {
            Log.e(TAG, "No front facing camera found.");
        }
        cameraId = findFacingCamera(cameraFacing);
        mPreview.getHolder().addCallback(this);
        mRawCallback = new RawCallback();
        Log.e(TAG, "NReset Camera " + previewing);
        if (!previewing) {
            try {
                mCamera = Camera.open(cameraId);
            } catch (Exception ex) {
                Log.e(TAG, "Camera.open(cameraId)  Exception = " + ex.toString());
                stopCamera();
            }

            if (mCamera != null) {
                Camera.Parameters params = mCamera.getParameters();
                preferredSize = params.getPreferredPreviewSizeForVideo();
                adaptPrefferedVideoSize(params);
                params.setPreviewSize(preferredSize.width, preferredSize.height);
                if(videoRecordCallback != null)
                videoRecordCallback.videoEventAdjustLayout(preferredSize.width, preferredSize.height);
                Camera.Size preferredPicSize = getPreferredPictureSize(params) ;
                params.setPictureSize(preferredPicSize.width, preferredPicSize.height);

                /***************/

                mCamera.setParameters(params);
                try {
                    manageCameraOrientation();
                    mCamera.stopPreview();
                    mPreview.getHolder().setFixedSize(preferredSize.width, preferredSize.height);
                    if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    } else if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    } else if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                    }
                    mCamera.setParameters(params);
                    mCamera.setPreviewDisplay(mPreview.getHolder());
                    mCamera.startPreview();
                    previewing = true;
                } catch (Exception e) {

                }
            }
        }

    }

    public void adaptPrefferedVideoSize(Camera.Parameters params) {

        Camera.Size sizeW640 = params.getPreviewSize();
        Camera.Size sizeW720 = params.getPreviewSize();
        Camera.Size sizeW1280 = params.getPreviewSize();

        sizeW640.width = 640;
        sizeW640.height = 480;

        sizeW720.width = 720;
        sizeW720.height = 480;

        sizeW1280.width = 1280;
        sizeW1280.height = 720;

        preferredSize = sizeW1280;

        /*************************/
        if (params.getSupportedVideoSizes() != null) {
            if (params.getSupportedVideoSizes().contains(sizeW1280)) {
                preferredSize = sizeW1280;
            } else if (params.getSupportedVideoSizes().contains(sizeW720)) {
                preferredSize = sizeW720;
            } else {
                preferredSize = sizeW640;
            }
        } else if (preferredSize != null) {
            if (preferredSize.width > sizeW1280.width) {
                preferredSize = sizeW1280;
            } else if (!preferredSize.equals(sizeW720)) {
                preferredSize = sizeW640;
            }
        } else {
            preferredSize = sizeW640;
        }
    }

    private  Camera.Size  getPreferredPictureSize(Camera.Parameters params){
        List<Size> picSupportedSizes = params.getSupportedPictureSizes();
        List<Size> preferredPicturesSize = new ArrayList<>();

        Camera.Size preferredPicSize = preferredSize;
        float diff = 100.0f;
        for (int i = 0; i < picSupportedSizes.size(); i++) {
            float localDiff = (float) preferredSize.width / preferredSize.height
                    - (float) picSupportedSizes.get(i).width / picSupportedSizes.get(i).height;

            if (localDiff == 0) {
                preferredPicSize = picSupportedSizes.get(i);
                preferredPicturesSize.add(picSupportedSizes.get(i));

            } else if (Math.abs(localDiff) < Math.abs(diff)) {
                diff = Math.abs(localDiff);
                preferredPicSize = picSupportedSizes.get(i);

            }
        }

        if (preferredPicturesSize.size() != 0) {
            int midIndex = preferredPicturesSize.size() / 2;
            preferredPicSize = preferredPicturesSize.get(midIndex);
        }

        return preferredPicSize;
    }


    public void refreshCamera(SurfaceHolder holder) {
        if (videoEvent == PLAY_VIDEO) {
            playVideo(holder);
        } else {
            stopCamera();
            resetCamera(holder);
        }


    }

    public void refreshCamera() {
        refreshCamera(mPreview.getHolder());
    }

    public void stopCamera() {

        previewing = false;
        if (mCamera == null) {
            return;
        }
        if (mPreview.getHolder() != null) {
            mPreview.getHolder().removeCallback(this);
        }
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public boolean switchCamera() {


        if (!recording) {
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                // release the old camera instance
                // switch camera, from the front and the back and vice versa
                stopCamera();
                if (cameraFacing == CAMERA_FACING_BACK) {
                    cameraFacing = CAMERA_FACING_FRONT;
                } else {
                    cameraFacing = CAMERA_FACING_BACK;
                }
                cameraId = findFacingCamera(cameraFacing);
                resetCamera(mPreview.getHolder());
            }

            return false;
        }
        return false;
    }

    public void manageCameraOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

//        if (info.canDisableShutterSound) {
//            mCamera.enableShutterSound(false);
//        }

        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror

        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }


    public boolean hasCamera(Context context) {
        // check if the device has camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public int findFacingCamera(int facing) {

        // faciing == Camera.CameraInfo.CAMERA_FACING_BACK   cameraFront = false; ou info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == facing) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

//        refreshCamera(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {

        refreshCamera(surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        stopCamera();
    }

    /****************************/


    private boolean prepareMediaRecorder() {
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        refreshCamera();
        if (mCamera != null) {
            mediaRecorder = new MediaRecorder();

            mCamera.unlock();
            mediaRecorder.setCamera(mCamera);

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setMaxDuration(ApplicationCommonValues.VIDEO_MAX_DURATION);


            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            mediaRecorder.setVideoSize(preferredSize.width, preferredSize.height);
            int videoEncodingBitRate = 500 * 1000;
            int videoMaxSize = Math.max(preferredSize.width, preferredSize.height);

            if (videoMaxSize >= 720) {
                videoEncodingBitRate = 2 * 1000 * 1000;
            } else {
                mediaRecorder.setAudioEncodingBitRate(128000);
            }

            mediaRecorder.setVideoEncodingBitRate(videoEncodingBitRate);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setAudioChannels(2);

            // create a File object for the parent directory
            File folder = new File(mediaPath);
            // have the object build the directory structure, if needed.
            folder.mkdirs();
            // create a File ovibject for the output file

            File outputFile = new File(getVideoPath());

            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

            // ile size 50M
            if (cameraFacing == CAMERA_FACING_FRONT) {
                mediaRecorder.setOrientationHint(270);
            } else {
                // back-facing
                mediaRecorder.setOrientationHint(90);
            }

            try {
                mediaRecorder.prepare();
            } catch (IllegalStateException e) {
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                releaseMediaRecorder();
                return false;
            }
        }

        return true;
    }

    public void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
        }

        if (mCamera != null) {
            mCamera.lock(); // lock camera for later use
        }
    }

    public boolean captureVideo() {
        // mCamera.startPreview();
        if (recording) {
            return false;
        }

        if (!prepareMediaRecorder()) {
            recording = false;
        }

        // work on UiThread for better performance
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                // If there are stories, add them to the table
                try {
                    mediaRecorder.start();
                    isFromServer = false;
                    if (videoRecordCallback != null) {
                        videoRecordCallback.videoEventInProgress(RECORD_VIDEO, 0);
                        recording = true;
                        videoEvent = RECORD_VIDEO;
                    }
                } catch (final Exception ex) {
                    releaseMediaRecorder();
                    recording = false;
                }
            }
        });

        return recording;
    }


    public String getPicturePath() {
        if(cameraFacing== CAMERA_FACING_FRONT){
            return mediaPath + "/" + "front_"+PICTURE_FILE_BASE;
        }
        return mediaPath + "/" +PICTURE_FILE_BASE;
    }

    public String getVideoPath() {
        if (videoEvent == PLAY_VIDEO && isFromServer()) {
            return mediaServerUrl;
        }

        if(cameraFacing== CAMERA_FACING_FRONT){
            return mediaPath + "/" + "front_"+VIDEO_FILE_BASE;
        }

        return mediaPath + "/" +VIDEO_FILE_BASE;
    }

    public void deleteMediaRecordedFile(String mediaPath) {
        File file = new File(mediaPath);
        if (file.exists()) {
            file.delete();
        }
    }

    public void deleteMediaRecordedFiles() {
        deleteMediaRecordedFile(getPicturePath());
        deleteMediaRecordedFile(getVideoPath());
    }

    public void stopRecording() {


        if (recording) {
            // stop recording and release camera
            try {
                mediaRecorder.stop(); // stop the recording
            } catch (RuntimeException ex) {

            }
            releaseMediaRecorder(); // release the MediaRecorder object

            stopCamera();
            if (videoRecordCallback != null) {
                videoRecordCallback.videoEventCompleted(RECORD_VIDEO);
            }
            recording = false;
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int idCamera) {
        this.cameraId = idCamera;
    }

    /*****************************************/

    public void playVideo() {
        playVideo(mPreview.getHolder());
    }

    public void playVideo(final SurfaceHolder holder) {
        takePictureInvoked = false;
        doCleanUp();


        if (mMediaPlayer == null || (mMediaPlayer != null && !mMediaPlayer.isPlaying())) {
            try {
                stopVideo();
                // Create a new media player and set the listeners
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(getVideoPath());
                holder.addCallback(VideoRecordManager.this);
                mMediaPlayer.setDisplay(holder);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setOnBufferingUpdateListener(VideoRecordManager.this);
                mMediaPlayer.setOnInfoListener(VideoRecordManager.this);
                mMediaPlayer.setOnCompletionListener(VideoRecordManager.this);
                mMediaPlayer.setOnSeekCompleteListener(VideoRecordManager.this);
                mMediaPlayer.setOnPreparedListener(VideoRecordManager.this);
                mMediaPlayer.setOnVideoSizeChangedListener(VideoRecordManager.this);

                if (isFromServer) {
                    mMediaPlayer.prepareAsync();
                } else {
                    mMediaPlayer.prepare();
                }

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                if(videoRecordCallback != null)
                if (!isFromServer) {
                    videoRecordCallback.videoEventInProgress(PLAY_VIDEO, mMediaPlayer.getDuration());
                } else {
                    videoRecordCallback.videoEventBufferingStart();
                }

                videoEvent = PLAY_VIDEO;

            } catch (Exception ex) {

                videoEvent = RECORD_VIDEO;
                if (videoRecordCallback != null)
                    videoRecordCallback.videoEventFailed(videoEvent);
                deleteMediaRecordedFiles();
                releaseMediaRecorder();
                stopVideo();
                stopCamera();
                resetCamera(mPreview.getHolder());
                return;
            }
        }
    }

    public void stopVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.setOnBufferingUpdateListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnVideoSizeChangedListener(null);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void onBufferingUpdate(MediaPlayer arg0, int percent) {

    }

    public void onCompletion(MediaPlayer arg0) {

        if (videoRecordCallback != null)
            videoRecordCallback.videoEventCompleted(PLAY_VIDEO);
    }


    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

        if (width == 0 || height == 0) {

            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;

        if (isFromServer() && videoRecordCallback != null)
            videoRecordCallback.videoEventAdjustLayout(width, height);

        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }

    }

    public void onPrepared(MediaPlayer mediaplayer) {

        startVideoPlayback();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
 ;
        if (videoRecordCallback != null)
            videoRecordCallback.videoEventInProgress(PLAY_VIDEO, mMediaPlayer.getDuration());
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {


        switch (what) {
            case MEDIA_INFO_UNKNOWN:


                break;
            case MEDIA_INFO_BUFFERING_START:

                if (videoRecordCallback != null)
                    videoRecordCallback.videoEventBufferingStart();
                break;
            case MEDIA_INFO_VIDEO_RENDERING_START:

                if (isFromServer && videoRecordCallback != null) {
                    videoRecordCallback.videoEventInProgress(PLAY_VIDEO, mMediaPlayer.getDuration());
                }
                break;
            case MEDIA_INFO_BUFFERING_END:

                if (isFromServer && videoRecordCallback != null) {
                    videoRecordCallback.videoEventInProgress(PLAY_VIDEO, mMediaPlayer.getDuration());
                }
                break;
            case MEDIA_INFO_METADATA_UPDATE:

                break;
            case MEDIA_INFO_BAD_INTERLEAVING:

                break;
            case MEDIA_INFO_SUBTITLE_TIMED_OUT:


                break;

        }
        return false;
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayback() {

        mPreview.getHolder().setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }

    /******************************************************/

//    public void uploadPictureRetrofit(File file, Callback<InstantResponseRetrofit> response) {
//
//
//        // this will build full path of API url where we want to send data.
//        RestAdapter restAdapter = new RestAdapter
//                .Builder()
//                .setEndpoint(ApplicationCommonValues.SEND_MEDIA_BASE_URL)
//                .setConverter(new SimpleXMLConverter())
//                .setLogLevel(RestAdapter.LogLevel.FULL)
//                .build();
//
//        // SubmitAPI is name of our interface which will send data to server.
//        SendMediaApiInterface api = restAdapter.
//                create(SendMediaApiInterface.class);
//
//        api.sendImage(getTypedFile(file), response);
//    }
//
//
//    public void uploadVideoAndPicture(File video, File picture, Callback<InstantResponseRetrofit> response) {
//
//
//        // this will build full path of API url where we want to send data.
//        RestAdapter restAdapter = new RestAdapter
//                .Builder()
//                .setEndpoint(ApplicationCommonValues.SEND_MEDIA_BASE_URL)
//                .setConverter(new SimpleXMLConverter())
//                .setLogLevel(RestAdapter.LogLevel.FULL)
//                .build();
//
//        // SubmitAPI is name of our interface which will send data to server.
//        SendMediaApiInterface api = restAdapter.
//                create(SendMediaApiInterface.class);
//
//        api.sendVideoAndImage(getTypedFile(video), getTypedFile(picture), response);
//    }

//    private TypedFile getTypedFile(File file) {
//        TypedFile typedFile = new TypedFile(MULTIPART_FORM_DATA, file);
//
//        return typedFile;
//    }

    public void setAddMirrorEffect(boolean addMirrorEffect) {
        this.addMirrorEffect = addMirrorEffect;
    }

    public boolean isFromServer() {
        return isFromServer;
    }

    public void setFromServer(boolean fromServer) {
        isFromServer = fromServer;
    }

    /*******************************/


    public Bitmap getBitmapOfMediaRecorded() {
        Bitmap bitmap = BitmapFactory.decodeFile(getPicturePath());
        return bitmap;
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        /****************/
        deleteMediaRecordedFile(getPicturePath());
        // create a File object for the output file
        File pictureFile = new File(getPicturePath());

        if (pictureFile.exists()) {
            pictureFile.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);

            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);

            ExifInterface exif = new ExifInterface(pictureFile.toString());



            if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                realImage = rotate(realImage, 90);
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                realImage = rotate(realImage, 270);
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                realImage = rotate(realImage, 180);
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")
                    && (cameraFacing == CAMERA_FACING_FRONT)) {
                realImage = rotate(realImage, -90);
                if (addMirrorEffect)
                    realImage = mirrorEffect(realImage);
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")
                    && (cameraFacing == CAMERA_FACING_BACK)) {
                realImage = rotate(realImage, 90);
            }

            boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.close();

            if (videoRecordCallback != null)
                videoRecordCallback.videoEventPictureTaken(realImage);
            stopCamera();
            takePictureInvoked = false;



        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        /***********/
    }

    public String getMediaServerUrl() {
        return mediaServerUrl;
    }

    public void setMediaServerUrl(String mediaServerUrl) {
        isFromServer = true;
        this.mediaServerUrl = mediaServerUrl;
    }

    public int getVideoEvent() {
        return videoEvent;
    }

    public void setVideoEvent(int videoEvent) {
        this.videoEvent = videoEvent;
    }

    public interface VideoEventListener {
        void videoEventInProgress(int type, long timeLimit);

        void videoEventCompleted(int type);

        void videoEventBufferingStart();

        void videoEventPictureTaken(Bitmap btp);

        void videoEventFailed(int type);

        void videoEventAdjustLayout(int w, int h);

    }
/****************************/

    public class RawCallback implements Camera.ShutterCallback, Camera.PictureCallback {
        @Override
        public void onShutter() {
            // notify the user, normally with a sound, that the picture has
            // been taken
        }
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // manipulate uncompressed image data
        }
    }
}