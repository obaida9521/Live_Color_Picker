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
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.developerobaida.livecolorpick.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    ImageCapture imageCapture;
    View colorSelected;
    float[] lastEvent = null;
    float d = 0f, newRot = 0f,oldDist = 1f;
    private boolean isZoomAndRotate,isOutSide;
    private static final int NONE = 0, DRAG = 1,ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float xCoOrdinate, yCoOrdinate;

    private final ActivityResultLauncher<String> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) startCamera(cameraFacing);
                 else Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            resultLauncher.launch(Manifest.permission.CAMERA);
        else startCamera(cameraFacing);



        binding.preview.setOnTouchListener((v, event) -> {

            binding.preview.removeView(colorSelected);
            binding.preview.removeView(binding.colorView);

            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                binding.preview.removeView(colorSelected);
                binding.preview.removeView(binding.colorView);
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
                                int alpha = Color.alpha(pixel);
                                int r = Color.red(pixel);
                                int g = Color.green(pixel);
                                int b = Color.blue(pixel);

                                colorSelected = new View(MainActivity.this);

                                int size = getResources().getDimensionPixelSize(R.dimen.color_selected_size);
                                colorSelected.setLayoutParams(new ViewGroup.LayoutParams(size, size));

                                colorSelected.setX(x - size / 2);
                                colorSelected.setY(y - size / 2);


                                colorSelected.setBackgroundColor(Color.argb(alpha,r,g,b));

                                binding.preview.addView(colorSelected);
                                binding.colorView.setBackgroundColor(Color.argb(alpha,r, g, b));
                                String hex = String.format("#%08X", pixel);
                                binding.colorCode.setText("Hex = "+hex+" \nRGB = "+alpha+r+g+b);
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
        int aspectRatio = aspectRatio(binding.preview.getWidth(), binding.preview.getHeight());
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview1 = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview1, imageCapture);

                preview1.setSurfaceProvider(binding.preview.getSurfaceProvider());

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

    //---------=====================================================================---------------
    private void viewTransformation(View view, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();
                start.set(event.getRawX(), event.getRawY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    mode = ZOOM;
                }

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
                isZoomAndRotate = false;
                if (mode == DRAG) {
                    float x = event.getRawX();
                    float y = event.getRawY();
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false;
                        float newX = event.getRawX() + xCoOrdinate;
                        float newY = event.getRawY() + yCoOrdinate;

                        view.setX(newX);
                        view.setY(newY);
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            float scale = newDist1 / oldDist;
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            view.setRotation((float) (view.getRotation() + (newRot - d)));
                        }
                    }
                }
                break;
        }
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (int) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    //---------=====================================================================---------------
}