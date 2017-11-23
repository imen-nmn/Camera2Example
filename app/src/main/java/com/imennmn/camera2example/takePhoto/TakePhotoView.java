package com.imennmn.camera2example.takePhoto;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.util.Size;

import java.io.File;

/**
 * Created by imen_nmn on 16/11/17.
 */

public interface TakePhotoView {
    void requestCameraPermission();

    int getRotation() ;

    int getOrientation() ;

    Size getScreenSize() ;

    void getSize(Point displaySize) ;

    void onCameraCaptureCompleted(File mFile) ;

    void onCameraConfigureFailed() ;

    void onCameraError();

    Context getContext() ;

    SurfaceTexture getSurfaceTexture() ;

    void setAspectRatio(int w, int h) ;

    void setTransform(Matrix matrix) ;

    CameraManager getCameraManager();

    void showMessage(int stringId);

    void stopCamera() ;
    void startCamera() ;

}