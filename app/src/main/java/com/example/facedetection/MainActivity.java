package com.example.facedetection;


import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Size;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.example.facedetection.camera.CameraSourcePreview;
import com.example.facedetection.camera.GraphicFaceTrackerFactory;
import com.example.facedetection.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.common.util.concurrent.ListenableFuture;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    FaceDetector faceDetector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {

        } else {
            requestCameraPermission();
        }
        initView();

        faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        if (!faceDetector.isOperational()) {
            new AlertDialog.Builder(this)
                    .setMessage("Face Detector could not be set up on your device :(")
                    .show();
        }
        cameraShow();
    }


    ImageView img;
    float WIDTH;
    float HEIGHT;

    int top = 0;
    int left = 0;
    int right = 0;
    int bottom = 0;

    private void cameraShow() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = previewView.getBitmap();
                if (bitmap != null) {
//                    WIDTH = bitmap.getWidth();
//                    HEIGHT = bitmap.getHeight();
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Face> sparseArray = faceDetector.detect(frame);
                    if (sparseArray.size() > 0) {
                        for (int i = 0; i < sparseArray.size(); i++) {
                            Face face = sparseArray.valueAt(i);
                            int left = (int) face.getPosition().x;
                            int top = (int) face.getPosition().y;
                            if (left < 0) {
                                left = 0;
                            }
                            if (top < 0) {
                                top = 0;
                            }
                            int right = left + (int) face.getWidth();
                            int bottom = top + (int) face.getHeight();


                            //draw boudding box
                            drawBoundingBox.updateRectangle(left, top, right, bottom);

                        }
                    } else {
                        top = 0;
                        left = 0;
                        right = 0;
                        bottom = 0;
                        drawBoundingBox.updateRectangle(0, 0, 0, 0);
                    }
                }
                handler.postDelayed(this, 1);
            }
        };
        handler.post(runnable);
    }

    DrawBoundingBox drawBoundingBox;

    private void initView() {
        previewView = findViewById(R.id.preview);
        drawBoundingBox = findViewById(R.id.drawbox);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());
    }


    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private ImageCapture imageCapture;

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setMaxResolution(new Size(640, 640))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();


        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }


    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage("Khong duoc cap quyen camera")
                .setPositiveButton("OK", listener)
                .show();
    }


}