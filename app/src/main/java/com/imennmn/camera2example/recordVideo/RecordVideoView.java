package com.imennmn.camera2example.recordVideo;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.support.annotation.StringRes;
import android.view.TextureView;

/**
 * Created by imen_nmn on 17/11/17.
 */

public interface RecordVideoView {

    int getRotation() ;

    int getOrientation() ;

    String getVideoFilePath() ;

    void onCameraConfigureFailed() ;

    void onCameraError();

    SurfaceTexture getSurfaceTexture() ;

    void setAspectRatio(int w, int h) ;

    void setTransform(Matrix matrix) ;

    CameraManager getCameraManager();

    void showMessage(@StringRes int stringId);

    void stopCamera() ;

    void startCamera() ;

    TextureView getTextureView() ;

    boolean requestVideoPermissions();

    void onVideoRecordStart() ;
    void onVideoRecordEnd(String url) ;
    void showLoading() ;
    void hideLoading() ;
}
