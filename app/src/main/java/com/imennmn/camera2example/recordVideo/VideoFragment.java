package com.imennmn.camera2example.recordVideo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.VideoView;

import com.imennmn.camera2example.AutoFitTextureView;
import com.imennmn.camera2example.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.media.MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_METADATA_UPDATE;
import static android.media.MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

/**
 * Created by imen_nmn on 17/11/17.
 */

public class VideoFragment extends Fragment implements RecordVideoView {
    private OnMediaFragmentInteraction mListener;
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private RecordVideoController recordVideoController ;

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    @BindView(R.id.texture)
    AutoFitTextureView mTextureView;
    @BindView(R.id.video_btn)
    Button mButtonVideo;
    @BindView(R.id.close)
    Button closeBtn;
    @BindView(R.id.videoView)
    VideoView videoView ;
    @BindView(R.id.camera_monitor)
    View cameraMonitor ;
    private Unbinder unbinder;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_camera2_video, container, false);
        recordVideoController = new RecordVideoController(this) ;
        return view ;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this,view) ;
        cameraMonitor.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        closeBtn.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMediaFragmentInteraction) {
            mListener = (OnMediaFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.video_btn)
    public void triggerVideo(){
        recordVideoController.startStopRecord();
    }

    @OnClick(R.id.close)
    public void closeMedia(){
        stopVideo();
        startCamera();
    }

    @Override
    public int getRotation() {
        return  getActivity().getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public int getOrientation() {
        return  getResources().getConfiguration().orientation ;
    }

    @Override
    public String getVideoFilePath() {
        final File dir = getContext().getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    @UiThread
    @Override
    public void onVideoRecordStart() {
       mButtonVideo.setText("Recording now ");
    }

    @Override
    public void onVideoRecordEnd(final String url) {
        playVideo(url);
        mButtonVideo.setText("Record");
    }

    @Override
    public void onCameraConfigureFailed() {

    }

    @Override
    public void onCameraError() {

    }

    private void playVideo(String url){
        Log.d("handleMessage", "playVideo *********");
        videoView.setVisibility(View.VISIBLE);
        closeBtn.setVisibility(View.VISIBLE);
        videoView.setVideoPath(url);
        videoView.start();
        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("handleMessage", "onPrepared *********");
                mediaPlayer.setLooping(true);
            }
        });

        videoView.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                switch (what) {
                    case MEDIA_INFO_BUFFERING_START:
                        hideLoading();
                        break;
                    case MEDIA_INFO_VIDEO_RENDERING_START:
                        break;
                    case MEDIA_INFO_BUFFERING_END:
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
        });
        videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d("handleMessage", "onCompletion *********");
            }
        });
//        stopCamera();
    }

    private void stopVideo(){
        videoView.stopPlayback();
        videoView.setVisibility(View.GONE);
        closeBtn.setVisibility(View.GONE);
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mTextureView.getSurfaceTexture();
    }

    @Override
    public void setAspectRatio(int w, int h) {
        mTextureView.setAspectRatio(w,h);
    }

    @Override
    public void setTransform(Matrix matrix) {
        mTextureView.setTransform(matrix);
    }

    @Override
    public CameraManager getCameraManager() {
        return  (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public void showMessage(@StringRes int stringId) {
        mListener.showMessage(getString(stringId));
    }

    @Override
    public TextureView getTextureView() {
        return mTextureView;
    }

    @Override
    public void showLoading() {
        if(mListener!= null){
            mListener.showLoading();
        }
    }

    @Override
    public void hideLoading() {
        if(mListener!= null){
            mListener.hideLoading();
        }
    }

    @Override
    public  boolean requestVideoPermissions() {

        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
                showConfirmationPermissionDialog();
            } else {
                requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
            }
            return true ;
        }
        return false;
    }


    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d("VideoTag", "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        mListener.showMessage(getString(R.string.permission_request));
                        break;
                    }
                }
            } else {
                mListener.showMessage(getString(R.string.permission_request));
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void stopCamera(){
        recordVideoController.closeCamera();
        recordVideoController.stopBackgroundThread();
        cameraMonitor.setVisibility(View.GONE);
        mTextureView.setVisibility(View.GONE);
    }

    public void startCamera() {
        mTextureView.setVisibility(View.VISIBLE);
        recordVideoController.startBackgroundThread();
        if (mTextureView.isAvailable()) {
            recordVideoController.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(recordVideoController);
        }
        cameraMonitor.setVisibility(View.VISIBLE);

    }


    public void showConfirmationPermissionDialog(){
        Dialog dialog =  new AlertDialog.Builder(getActivity())
                .setMessage(R.string.permission_request)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(VIDEO_PERMISSIONS,
                                REQUEST_VIDEO_PERMISSIONS);
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        })
                .create();

        dialog.show();
    }

    public interface OnMediaFragmentInteraction {
        // TODO: Update argument type and name
        void showLoading() ;
        void hideLoading() ;
        void showMessage(String msg) ;
    }
}

