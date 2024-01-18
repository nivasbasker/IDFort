package com.zio.idfort;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.zio.idfort.databinding.ActivityCamBinding;
import com.zio.idfort.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class CamActivity extends AppCompatActivity {

    private PreviewView viewFinder;
    private FrameLayout guideOverlay;
    private ImageButton captureButton;
    ActivityCamBinding binding;

    private ProcessCameraProvider cameraProvider = null;
    private ImageCapture imageCapture;
    private ProgressBar pbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewFinder = binding.viewFinder;
        guideOverlay = binding.guideOverlay;
        captureButton = binding.captureButton;
        pbar = binding.progressBar;

        set_layout();

        imageCapture = new ImageCapture.Builder().build();

        configCamX();

        captureButton.setOnClickListener(v -> {
            captureButton.setEnabled(false);
            pbar.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Saving please wait..", Toast.LENGTH_LONG).show();

            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {

                    cameraProvider.unbindAll();

                    Log.d(Constants.TAG, "Captured image");
                    Bitmap originalBitmap = correction(imageProxyToBitmap(image), image.getImageInfo().getRotationDegrees());
                    Bitmap croppedBitmap = cropBitmap(originalBitmap);
                    Bitmap finalBitmap = reduceBitmapSize(croppedBitmap);

                    File outputFile = new File(getCacheDir(), Constants.TEMP_FILE);
                    saveBitmapToFile(finalBitmap, outputFile);

                    image.close();
                    Log.d(Constants.TAG, "sending back results");
                    onWorkDone(true);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    // Handle capture error
                    Toast.makeText(CamActivity.this, "try again", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    public static Bitmap reduceBitmapSize(Bitmap originalBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        int targetSizeKB = 250;

        // Reduce the quality of the bitmap until it meets the target size
        Bitmap reducedBitmap = null;
        for (int quality = 100; quality >= 0; quality -= 10) {
            byteArrayOutputStream.reset();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            byte[] compressedByteArray = byteArrayOutputStream.toByteArray();
            int compressedSizeKB = compressedByteArray.length / 1024;
            Log.d(Constants.TAG, quality + "  " + compressedSizeKB);
            if (compressedSizeKB <= targetSizeKB) {
                reducedBitmap = BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.length);
                break;
            }
        }

        return reducedBitmap;
    }

    private Bitmap compressImage(Bitmap originalBitmap, int quality) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

        byte[] compressedData = outputStream.toByteArray();
        Log.d(Constants.TAG, "Size & quality    " + quality + compressedData.length / 1024);
        return BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
    }


    private void onWorkDone(boolean isSuccess) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("success", isSuccess);
        setResult(isSuccess ? RESULT_OK : RESULT_CANCELED, resultIntent);
        finish();
    }

    private void configCamX() {

        // Configure CameraX

        Preview preview = new Preview.Builder().build();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private Bitmap correction(Bitmap originalBitmap, int rotationDegrees) {

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);

        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
    }

    private Bitmap cropBitmap(Bitmap originalBitmap) {
        int width = (int) (originalBitmap.getWidth());

        int left = (int) (width * 0.1);
        int top = (int) ((originalBitmap.getHeight() - (width * 0.53)) / 2);
        Log.d(Constants.TAG, "Cropping in progress : width - " + width + "\n"
                + "left - " + left + "\n"
                + "top - " + top + "\n");

        return Bitmap.createBitmap(originalBitmap, left, top, (int) (width * 0.8), (int) (width * 0.53));
    }

    private void saveBitmapToFile(Bitmap croppedBitmap, File outputFile) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(Constants.TAG, "Saved in cache");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ExperimentalGetImage
    public static Bitmap imageProxyToBitmap(ImageProxy image) {
        Image mediaImage = image.getImage();
        if (mediaImage == null) {
            return null;
        }

        ByteBuffer buffer = mediaImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private void set_layout() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = (int) (displayMetrics.widthPixels);
        int overlayWidth = (int) (width * 0.8);// Adjust as needed
        int overlayHeight = (int) (overlayWidth * 0.53);
        ViewGroup.LayoutParams overlayParams = guideOverlay.getLayoutParams();
        overlayParams.width = overlayWidth;
        overlayParams.height = overlayHeight;
        guideOverlay.setLayoutParams(overlayParams);
    }
}