package com.imennmn.camera2example;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class CameraFragment extends Fragment implements CameraView {


    private static final int REQUEST_CAMERA_PERMISSION = 1;
    @BindView(R.id.texture)
    AutoFitTextureView mTextureView;
    @BindView(R.id.record)
    Button mButtonVideo;
    @BindView(R.id.take_photo)
    Button takePhotoBtn;
    @BindView(R.id.switch_camera)
    Button switchCamera;
    CameraController cameraController;
    @BindView(R.id.picked_img)
    ImageView pickedImg;
    private OnFragmentInteractionListener mListener;
    private Unbinder unbinder;

    public CameraFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        cameraController = new CameraController(this);
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        unbinder = ButterKnife.bind(this, view);
        pickedImg.setVisibility(View.GONE);
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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

    public void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
//            new Camera2BasicFragment.ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Camera2BasicFragment.ErrorDialog.newInstance(getString(R.string.request_permission))
//                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public int getRotation() {
        return getActivity().getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    @Override
    public void getSize(Point displaySize) {
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
    }

    @Override
    public void onCameraCaptureCompleted(final File photoFile) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopCamera();

                Glide.with(CameraFragment.this)
                        .load(photoFile)
                        .asBitmap()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(pickedImg);
            }
        });

    }

    @Override
    public void onCameraConfigureFailed() {

    }

    @Override
    public void onCameraError() {

    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mTextureView.getSurfaceTexture();
    }

    @Override
    public void setAspectRatio(int w, int h) {
        mTextureView.setAspectRatio(w, h);
    }

    @Override
    public void setTransform(Matrix matrix) {
        mTextureView.setTransform(matrix);
    }

    @Override
    public CameraManager getCameraManager() {
        return (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public void showMessage(int stringId) {

    }

    @OnClick(R.id.take_photo)
    void takePhoto() {
        cameraController.takePicture();
    }

    @OnClick(R.id.record)
    void recordVideo() {

    }

    @OnClick(R.id.switch_camera)
    void switchCamera() {
        cameraController.switchCamera();
    }

    @OnClick(R.id.start)
    public void startCamera() {
        pickedImg.setImageBitmap(null);
        pickedImg.setVisibility(View.GONE);
        cameraController.startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            cameraController.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(cameraController);
        }
    }

    @OnClick(R.id.stop)
    public void stopCamera() {
        pickedImg.setVisibility(View.VISIBLE);
        cameraController.closeCamera();
        cameraController.stopBackgroundThread();
    }

   /* @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture: {
                cameraController.takePicture();
                break;
            }
            case R.id.info: {
                Activity activity = getActivity();
                if (null != activity) {
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.intro_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;
            }
        }*/
//}


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }
}
