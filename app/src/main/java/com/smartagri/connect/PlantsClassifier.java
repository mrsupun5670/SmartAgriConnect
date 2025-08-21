package com.smartagri.connect;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PlantsClassifier {
    private static final String TAG = "PlantsClassifier";
    private static final String MODEL_FILE = "plant_classifire_model.tflite";
    private static final String CLASSES_FILE = "class_names.json";

    private static final int INPUT_SIZE = 224;
    private static final int PIXEL_SIZE = 3;
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255.0f;

    private Interpreter interpreter;
    private List<String> classNames;
    private ByteBuffer inputBuffer;
    private float[][] outputBuffer;

    public PlantsClassifier(Context context) throws IOException {
        MappedByteBuffer modelBuffer = loadModelFile(context);
        interpreter = new Interpreter(modelBuffer);

        // Load class names
        classNames = loadClassNames(context);

        // Initialize buffers
        int numClasses = classNames.size();
        inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);
        inputBuffer.order(ByteOrder.nativeOrder());
        outputBuffer = new float[1][numClasses];
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadClassNames(Context context) throws IOException {
        List<String> classes = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open(CLASSES_FILE);
            Scanner scanner = new Scanner(inputStream);
            StringBuilder jsonString = new StringBuilder();
            while (scanner.hasNext()) {
                jsonString.append(scanner.nextLine());
            }
            scanner.close();

            JSONArray jsonArray = new JSONArray(jsonString.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                classes.add(jsonArray.getString(i));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading class names", e);
        }
        return classes;
    }

    public ClassificationResult classify(Bitmap bitmap) {
        if (interpreter == null) return null;

        // Preprocess the image
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        convertBitmapToByteBuffer(resizedBitmap);

        // Run inference
        interpreter.run(inputBuffer, outputBuffer);

        // Find the class with highest probability
        float maxProbability = 0;
        int maxIndex = 0;
        for (int i = 0; i < outputBuffer[0].length; i++) {
            if (outputBuffer[0][i] > maxProbability) {
                maxProbability = outputBuffer[0][i];
                maxIndex = i;
            }
        }

        String className = classNames.get(maxIndex);
        float confidence = maxProbability * 100;

        return new ClassificationResult(className, confidence);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        inputBuffer.rewind();
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int pixel = bitmap.getPixel(x, y);

                // Extract RGB values and normalize (same as your Python preprocessing)
                inputBuffer.putFloat(((pixel >> 16) & 0xFF) / IMAGE_STD);
                inputBuffer.putFloat(((pixel >> 8) & 0xFF) / IMAGE_STD);
                inputBuffer.putFloat((pixel & 0xFF) / IMAGE_STD);
            }
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public static class ClassificationResult {
        private String className;
        private float confidence;

        public ClassificationResult(String className, float confidence) {
            this.className = className;
            this.confidence = confidence;
        }

        public String getClassName() {
            if (confidence < 80.0f) {
                Log.d("BugClassifier sfsdfsdfsf", "Low confidence: " + confidence + "% for class: " + className);
                return "Uncertain - Try again";
            }
            return className;
        }

        public float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f%%)", getClassName(), confidence);
        }
    }
}
