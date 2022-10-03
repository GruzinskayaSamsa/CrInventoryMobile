package com.example.storageappmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 123;

    private ImageView preView;
    private TextView textView;
    private View layout;

    int screenWidth, screenHeight;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    ConverterYUVtoRGB translator = new ConverterYUVtoRGB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preView = findViewById(R.id.imageView);
//        textView = findViewById(R.id.TextView);
        layout = findViewById(R.id.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            initializeCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        }
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    //Preview preview = new Preview.Builder().build();

                    //ImageCapture imageCapture = new ImageCapture.Builder().build();

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                                .setTargetResolution(new Size(screenWidth, screenHeight))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MainActivity.this),
                            new ImageAnalysis.Analyzer() {
                                @Override
                                public void analyze(@NonNull ImageProxy image) {
                                    @SuppressLint("UnsafeOptInUsageError")
                                    Image img = image.getImage();

                                    Bitmap myBitmap = translator.translateYUV(img, MainActivity.this);

                                    BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                                            .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                                            .build();

                                    if (!barcodeDetector.isOperational()) {
//                                        textView.setText("Could not set up the detector!");
                                    } else {
                                        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                                        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

                                        try {
                                            Barcode thisCode = barcodes.valueAt(0);
//                                            textView.setText(thisCode.rawValue);

                                            Intent intent = new Intent(MainActivity.this,
                                                    ObjectInfo.class);
                                            intent.putExtra("data", thisCode.rawValue);
//                                            image.close();
                                            startActivity(intent);
                                            image.close();

                                        } catch (ArrayIndexOutOfBoundsException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(image.getImageInfo().getRotationDegrees());
//                                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap,
//                                            myBitmap.getWidth(), myBitmap.getHeight(), true);
                                    Bitmap rotatedBitmap = Bitmap.createBitmap(myBitmap, 0, 0,
                                            myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);

//                                    preView.setRotation(image.getImageInfo().getRotationDegrees());
                                    preView.setImageBitmap(rotatedBitmap);
                                    image.close();
                                }
                            });

                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, imageAnalysis);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

}