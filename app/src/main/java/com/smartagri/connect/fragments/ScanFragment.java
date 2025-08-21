package com.smartagri.connect.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.smartagri.connect.BaseFragment;
import com.smartagri.connect.InfoFragment;
import com.smartagri.connect.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ScanFragment extends BaseFragment {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private PreviewView previewView;
    private CardView captureButton;
    private CardView galleryButton;
    private ImageCapture imageCapture;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                String realPath = getRealPathFromURI(selectedImageUri);
                                Fragment infoFragment = new InfoFragment();
                                Bundle args = new Bundle();
                                if (realPath != null) {

                                    args.putString("imagePath", realPath);
                                } else {
                                    args.putString("imageUri", selectedImageUri.toString());
                                }
                                args.putBoolean("isFromGallery", true);
                                infoFragment.setArguments(args);
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.nav_host_fragment, infoFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }
                    }
            );

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = requireActivity().getContentResolver()
                .query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        initializeViews(view);
        checkPermissions();
        setClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        previewView = view.findViewById(R.id.previewView);
        captureButton = view.findViewById(R.id.captureButton);
        galleryButton = view.findViewById(R.id.galleryButton);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE
            );
        } else {
            startCamera();
        }
    }

    private void setClickListeners() {
        captureButton.setOnClickListener(v -> capturePhoto());
        galleryButton.setOnClickListener(v -> openGallery());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(requireContext(),
                        "Error starting camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception ignored) {
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());
        File photoFile = new File(requireContext().getExternalFilesDir(null),
                "IMG_" + timeStamp + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri == null) {
                            savedUri = Uri.fromFile(photoFile);
                        }

                        Fragment infoFragment = new InfoFragment();

                        Bundle args = new Bundle();
                        args.putString("imageUri", savedUri.toString());
                        args.putString("imagePath", photoFile.getAbsolutePath());
                        args.putBoolean("isFromGallery", false);

                        infoFragment.setArguments(args);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.nav_host_fragment, infoFragment)
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(requireContext(),
                                "Failed to capture image: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(),
                        "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}