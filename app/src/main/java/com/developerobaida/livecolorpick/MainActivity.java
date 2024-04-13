package com.developerobaida.livecolorpick;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    View colorView;
    TextView colorCode;
    PreviewView preview;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    ImageCapture imageCapture;

    private final ActivityResultLauncher<String> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) startCamera(cameraFacing);
                 else Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorView = findViewById(R.id.colorView);
        preview = findViewById(R.id.preview);
        colorCode = findViewById(R.id.colorCode);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            resultLauncher.launch(Manifest.permission.CAMERA);
        else startCamera(cameraFacing);


        preview.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (imageCapture != null) {
                    imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                            super.onCaptureSuccess(image);
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            int x = (int) event.getX();
                            int y = (int) event.getY();
                            if (x < bitmap.getWidth() && y < bitmap.getHeight()) {
                                int pixel = bitmap.getPixel(x, y);
                                int r = Color.red(pixel);
                                int g = Color.green(pixel);
                                int b = Color.blue(pixel);

                                colorView.setBackgroundColor(Color.rgb(r, g, b));
                                String hex = "#"+ Integer.toHexString(pixel);
                                colorCode.setText("Hex = "+hex+" \nRGB = "+r+g+b);
                            }
                            image.close();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            super.onError(exception);
                            exception.printStackTrace();
                        }
                    });
                }
            }
            return true;
        });
    }

    private void startCamera(int cameraFacing) {
        int aspectRatio = aspectRatio(preview.getWidth(), preview.getHeight());
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview1 = new Preview.Builder()
                        .setTargetAspectRatio(aspectRatio).build();

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview1, imageCapture);

                preview1.setSurfaceProvider(preview.getSurfaceProvider());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
}