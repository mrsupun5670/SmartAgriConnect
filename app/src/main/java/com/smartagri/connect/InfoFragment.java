package com.smartagri.connect;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;

public class InfoFragment extends BaseFragment {

    private ImageView imageView;
    private TextView textDescriptionSi, textDescriptionEn, textImageNameEn, textImageNameSi, confidenceTextView;

    private PlantsClassifier PlantsClassifier;
    private String classifiedLeafName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        initializeViews(view);
        initializePlantsClassifier();

        // Get arguments from ScanFragment
        Bundle args = getArguments();
        if (args != null) {
            loadAndClassifyImage();
        } else {
            Toast.makeText(requireContext(), "No image data provided", Toast.LENGTH_SHORT).show();
            Log.d("InfoFragment dgdfg", "No arguments found in Bundle");
        }

        return view;
    }

    private void initializeViews(View view) {
        imageView = view.findViewById(R.id.imageView);
        textImageNameEn = view.findViewById(R.id.textImageNameEn);
        textImageNameSi = view.findViewById(R.id.textImageNameSi);
        textDescriptionSi = view.findViewById(R.id.textDescriptionSi);
        textDescriptionEn = view.findViewById(R.id.textDescriptionEn);
        confidenceTextView = view.findViewById(R.id.confidenceTextView);
    }

    private void initializePlantsClassifier() {
        try {
            PlantsClassifier = new PlantsClassifier(requireContext());
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to load ML model", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadAndClassifyImage() {
        Bundle args = getArguments();
        if (args == null) {
            showError("No image data provided");
            return;
        }

        boolean isFromGallery = args.getBoolean("isFromGallery", false);
        Uri imageUri = null;

        if (isFromGallery) {
            String imagePath = args.getString("imagePath");
            if (imagePath != null) {
                File imageFile = new File(imagePath);
                imageUri = Uri.fromFile(imageFile);
            } else {
                String imageUriString = args.getString("imageUri");
                if (imageUriString != null) {
                    imageUri = Uri.parse(imageUriString);
                }
            }
        } else {
            // From camera capture
            String imagePath = args.getString("imagePath");
            if (imagePath != null) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imageUri = Uri.fromFile(imageFile);
                }
            } else {
                String imageUriString = args.getString("imageUri");
                if (imageUriString != null) {
                    imageUri = Uri.parse(imageUriString);
                }
            }
        }

        if (imageUri != null) {
            // Load image into ImageView
            Glide.with(this).load(imageUri).into(imageView);
            // Classify the image
            classifyImageFromUri(imageUri);
        } else {
            showError("Unable to load image");
        }
    }

    private void classifyImageFromUri(Uri imageUri) {
        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), imageUri);

                if (PlantsClassifier != null) {
                    PlantsClassifier.ClassificationResult result = PlantsClassifier.classify(bitmap);

                    if (result != null) {
                        classifiedLeafName = result.getClassName();

                        requireActivity().runOnUiThread(() -> {
                            confidenceTextView.setText((String.format("%.2f", result.getConfidence())) + "%");
                            displayPlantInformation();
                        });
                    } else {
                        requireActivity().runOnUiThread(this::showClassificationError);
                    }
                } else {
                    requireActivity().runOnUiThread(this::showClassificationError);
                }

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(this::showClassificationError);
            }
        }).start();
    }

    private void displayPlantInformation() {
        if (classifiedLeafName == null || classifiedLeafName.isEmpty() ||
                classifiedLeafName.equals("Uncertain - Try again")) {
            classifiedLeafName = "Unknown Leaf";
        }

        // Set the leaf name
        textImageNameEn.setText(classifiedLeafName);

        // Fetch data from Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("diseases").document(classifiedLeafName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String plantNameSi = documentSnapshot.getString("plant");
                        String descriptionEn = documentSnapshot.getString("description_en");
                        String descriptionSi = documentSnapshot.getString("description_si");

                        textImageNameSi.setText(plantNameSi != null ? plantNameSi : "N/A");
                        textDescriptionEn.setText(descriptionEn != null ? descriptionEn : "No description available");
                        textDescriptionSi.setText(descriptionSi != null ? descriptionSi : "විස්තරයක් නොමැත");
                    } else {
                        textImageNameSi.setText("N/A");
                        textDescriptionEn.setText("No description found");
                        textDescriptionSi.setText("විස්තරයක් හමු නොවීය");
                    }
                })
                .addOnFailureListener(e -> {
                    textImageNameSi.setText("N/A");
                    textDescriptionEn.setText("Error loading data");
                    textDescriptionSi.setText("දත්ත පූරණය කිරීමට දෝෂයක්");
                });
    }


    private void showClassificationError() {
        Toast.makeText(requireContext(), "Failed to classify image", Toast.LENGTH_SHORT).show();

        classifiedLeafName = "Unknown Leaf";
        displayPlantInformation();
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Navigate back
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (PlantsClassifier != null) {
            PlantsClassifier.close();
        }
    }
}