package com.example.injuryrecoveryapplication;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

/**
 * Demonstrates loading and running a MoveNet model (e.g. movenet_lightning.tflite)
 * that expects uint8 input in [0..255].
 *
 * We create a ByteBuffer of size (1 * width * height * 3) for [1,192,192,3],
 * each pixel channel is a single byte. The model's first node "Cast" will
 * interpret that as uint8 -> float.
 */
public class TestPoseActivity extends AppCompatActivity {

    private static final String TAG = "TestPoseActivity";

    private ImageView imageView;
    private Interpreter interpreter;

    private final int inputWidth = 192;
    private final int inputHeight = 192;

    // The model's output: [1][1][17][3] => for single-pose
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pose);

        imageView = findViewById(R.id.testPoseImageView);

        initInterpreter();

        Bitmap testBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_pose);
        if (testBitmap == null) {
            Log.e(TAG, "test_pose image not found in drawable.");
            return;
        }

        Person person = estimatePose(testBitmap);
        Bitmap annotated = drawKeypointsOnBitmap(testBitmap, person);
        imageView.setImageBitmap(annotated);
    }

    /**
     * Loads movenet_lightning.tflite from assets (which expects uint8 input).
     */
    private void initInterpreter() {
        try {
            AssetFileDescriptor fd = getAssets().openFd("movenet_lightning.tflite");
            long size = fd.getLength();
            Log.d(TAG, "Size of movenet_lightning in assets: " + size);

            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(this, "movenet_lightning.tflite");

            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(modelBuffer, options);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1) Resize image to 192x192
     * 2) Convert it to a ByteBuffer of shape [1,192,192,3], with each channel as a single byte in [0..255].
     * 3) Run TFLite, interpret the output.
     */
    private Person estimatePose(Bitmap original) {
        Bitmap resized = Bitmap.createScaledBitmap(original, inputWidth, inputHeight, false);

        // Prepare output array
        float[][][][] output = new float[1][1][17][3];

        // Build the input buffer as UINT8
        ByteBuffer inputBuffer = convertBitmapToUint8(resized);

        // Run inference
        interpreter.run(inputBuffer, output);

        // Convert raw output -> Person
        KeyPoint[] keyPoints = new KeyPoint[17];
        for (int kp = 0; kp < 17; kp++) {
            float y = output[0][0][kp][0];
            float x = output[0][0][kp][1];
            float score = output[0][0][kp][2];

            BodyPart bodyPart = BodyPart.fromIndex(kp);
            PointF coordinate = new PointF(x, y);
            keyPoints[kp] = new KeyPoint(bodyPart, coordinate, score);
        }

        return new Person(keyPoints, 1.0f);
    }

    /**
     * Creates a ByteBuffer [1,192,192,3] (uint8).
     * We do 1*(192*192*3) raw bytes. We also set the ByteOrder to nativeOrder
     * for consistency, though for uint8 it matters less.
     */
    private ByteBuffer convertBitmapToUint8(Bitmap bitmap) {
        // total for one image is 192*192*3 = 110,592 bytes
        // If the interpreter strictly wants a batch dimension, we do:
        // int totalPixels = inputWidth * inputHeight * 3;
        // ByteBuffer buffer = ByteBuffer.allocateDirect(1 * totalPixels);
        // But TFLite typically reads shape from the `.tflite` so just do that + 1 batch.

        int totalPixels = inputWidth * inputHeight;
        ByteBuffer buffer = ByteBuffer.allocateDirect(totalPixels * 3);
        buffer.order(ByteOrder.nativeOrder()); // not strictly required for uint8, but good practice

        int[] pixels = new int[totalPixels];
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            buffer.put((byte) r);
            buffer.put((byte) g);
            buffer.put((byte) b);
        }

        buffer.rewind();
        return buffer;
    }

    private Bitmap drawKeypointsOnBitmap(Bitmap original, Person person) {
        Bitmap annotated = original.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(annotated);

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setStrokeWidth(6f);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);

        // Just partial skeleton lines
        BodyPart[][] skeletonPairs = {
                {BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW},
                {BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST},
                {BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW},
                {BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST}
        };

        for (BodyPart[] pair : skeletonPairs) {
            KeyPoint kp1 = person.getKeyPoint(pair[0]);
            KeyPoint kp2 = person.getKeyPoint(pair[1]);
            if (kp1 != null && kp2 != null && kp1.score > 0.3f && kp2.score > 0.3f) {
                float x1 = kp1.coordinate.x * annotated.getWidth();
                float y1 = kp1.coordinate.y * annotated.getHeight();
                float x2 = kp2.coordinate.x * annotated.getWidth();
                float y2 = kp2.coordinate.y * annotated.getHeight();
                canvas.drawLine(x1, y1, x2, y2, linePaint);
            }
        }

        // Draw circles
        for (KeyPoint kp : person.keyPoints) {
            if (kp.score > 0.3f) {
                float cx = kp.coordinate.x * annotated.getWidth();
                float cy = kp.coordinate.y * annotated.getHeight();
                canvas.drawCircle(cx, cy, 8f, circlePaint);
            }
        }

        return annotated;
    }

    // ------------------------------------------------------------------------
    // Data classes
    // ------------------------------------------------------------------------

    static class Person {
        KeyPoint[] keyPoints;
        float score;
        Person(KeyPoint[] keyPoints, float score) {
            this.keyPoints = keyPoints;
            this.score = score;
        }

        KeyPoint getKeyPoint(BodyPart part) {
            for (KeyPoint kp : keyPoints) {
                if (kp.bodyPart == part) return kp;
            }
            return null;
        }
    }

    static class KeyPoint {
        BodyPart bodyPart;
        PointF coordinate;
        float score;
        KeyPoint(BodyPart bodyPart, PointF coordinate, float score) {
            this.bodyPart = bodyPart;
            this.coordinate = coordinate;
            this.score = score;
        }
    }

    enum BodyPart {
        NOSE, LEFT_EYE, RIGHT_EYE, LEFT_EAR, RIGHT_EAR,
        LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_ELBOW, RIGHT_ELBOW,
        LEFT_WRIST, RIGHT_WRIST, LEFT_HIP, RIGHT_HIP,
        LEFT_KNEE, RIGHT_KNEE, LEFT_ANKLE, RIGHT_ANKLE, UNKNOWN;

        static BodyPart fromIndex(int index) {
            switch (index) {
                case 0: return NOSE;
                case 1: return LEFT_EYE;
                case 2: return RIGHT_EYE;
                case 3: return LEFT_EAR;
                case 4: return RIGHT_EAR;
                case 5: return LEFT_SHOULDER;
                case 6: return RIGHT_SHOULDER;
                case 7: return LEFT_ELBOW;
                case 8: return RIGHT_ELBOW;
                case 9: return LEFT_WRIST;
                case 10: return RIGHT_WRIST;
                case 11: return LEFT_HIP;
                case 12: return RIGHT_HIP;
                case 13: return LEFT_KNEE;
                case 14: return RIGHT_KNEE;
                case 15: return LEFT_ANKLE;
                case 16: return RIGHT_ANKLE;
                default: return UNKNOWN;
            }
        }
    }
}
