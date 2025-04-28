package com.example.injuryrecoveryapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import android.graphics.Matrix;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.injuryrecoveryapplication.utils.ExerciseSpecLibrary;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraExerciseActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    // Dimensions for the MoveNet model input
    private static final int MODEL_WIDTH = 192;
    private static final int MODEL_HEIGHT = 192;

    private String exerciseId;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;
    private ExerciseSpec spec;
    private long lastFrameTime = 0L;
    private float timeInCorrectRange = 0f;
    private TextView feedbackTextView;

    // TFLite
    private Interpreter tflite;

    // Converts YUV images from the camera to RGB Bitmaps
    private YuvToRgbConverter yuvToRgbConverter;

    // Overlay that draws keypoints and skeleton
    private PoseOverlayView poseOverlayView;

    // Will hold the 17 keypoints for the pose
    private float[][] currentKeypoints = new float[17][3];


    private int repCount = 0;
    private boolean armDown = false;

    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_exercise);

        // Get exerciseId if passed
        if (getIntent().hasExtra("exerciseId")) {
            exerciseId = getIntent().getStringExtra("exerciseId");
        }

        // Lookup the spec in ExerciseSpecLibrary
        this.spec  = ExerciseSpecLibrary.getSpec(exerciseId);
        if (spec == null) {
            android.util.Log.w("CameraExerciseActivity",
                    "No ExerciseSpec found for exerciseId=" + exerciseId
                            + ". Using default or no logic.");
        } else {
            android.util.Log.d("CameraExerciseActivity",
                    "Loaded spec for exerciseId=" + exerciseId
                            + " angles=" + spec.getAnglesToTrack()
                            + " repGoal=" + spec.getRepGoal()
                            + " holdTime=" + spec.getHoldTimeSeconds());
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbarCamera);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Camera Exercise");
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationViewCamera);
        bottomNav.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });

        // Preview and executor
        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Overlay view
        poseOverlayView = findViewById(R.id.poseOverlay);
        feedbackTextView = findViewById(R.id.textViewFeedback);

        // Create YUV-RGB converter
        yuvToRgbConverter = new YuvToRgbConverter(this);

        // Init TFLite
        initTfliteInterpreter();

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }
    }

    // Loads the .tflite model from assets and creates the Interpreter
    private void initTfliteInterpreter() {
        try {
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(this, "movenet_lightning.tflite");
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            tflite = new Interpreter(modelBuffer, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Called when user grants or denies camera permission
    @ExperimentalGetImage
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Check if camera permission was granted
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }

    // Starts up the camera once permissions are available
    @ExperimentalGetImage
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @ExperimentalGetImage
    private void bindCameraUseCases() {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Set up image analysis to process every frame
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // Direct frames to the processImageProxy() method
        imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

        // Use the front camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
        );
    }

    // Process each camera frame to detect pose and update feedback
    @ExperimentalGetImage
    private void processImageProxy(ImageProxy image) {
        try {
            // Convert the ImageProxy into a ByteBuffer in the format needed by the model
            ByteBuffer inputBuffer = prepareInput(image);
            if (inputBuffer == null) return;


            float[][][][] output = new float[1][1][17][3];
            tflite.run(inputBuffer, output);


            // Update current keypoints from the model output
            for (int kp = 0; kp < 17; kp++) {
                float y = output[0][0][kp][0];
                float x = output[0][0][kp][1];
                float conf = output[0][0][kp][2];

                currentKeypoints[kp][0] = y;
                currentKeypoints[kp][1] = x;
                currentKeypoints[kp][2] = conf;
            }

            // Update the PoseOverlayView
            runOnUiThread(() -> poseOverlayView.setKeypoints(currentKeypoints));

            // if spec is null, skip posture logic
            if (spec == null) {
                return;
            }

            // Calculate time elapsed since the last frame
            long currentTime = System.currentTimeMillis();
            float deltaSeconds;
            if (lastFrameTime == 0L) {
                // First frame
                deltaSeconds = 0f;
            } else {
                deltaSeconds = (currentTime - lastFrameTime) / 1000f;
            }
            lastFrameTime = currentTime;

            // Check if all angles are in the range
            boolean allAnglesInRange = true;

            for (String angleName : spec.getAnglesToTrack()) {
                float angleDegrees = computeAngleForName(angleName);
                // Compare to minAngle..maxAngle
                if (angleDegrees < spec.getMinAngle() || angleDegrees > spec.getMaxAngle()) {
                    allAnglesInRange = false;
                    // (Optional) log or highlight
                    android.util.Log.d("CameraExercise", angleName + " out of range=" + angleDegrees);
                    break;
                } else {
                    android.util.Log.d("CameraExercise", angleName + " is GOOD=" + angleDegrees);
                }
            }

            final boolean finalAllAnglesInRange = allAnglesInRange;

            // If hold-based exercise
            if (spec.isRequiresHold()) {
                // add up timeInCorrectRange if posture is correct
                if (finalAllAnglesInRange) {
                    timeInCorrectRange += deltaSeconds;
                } else {
                    timeInCorrectRange = 0f;
                }
                android.util.Log.d("CameraExercise", "timeInCorrectRange=" + timeInCorrectRange);


                float timeLeft = spec.getHoldTimeSeconds() - timeInCorrectRange;
                runOnUiThread(() -> {
                    if (!finalAllAnglesInRange) {
                        feedbackTextView.setText("Incorrect form!");
                    } else if (timeLeft > 0) {
                        feedbackTextView.setText(String.format("Holding: %.1f / %d seconds",
                                timeInCorrectRange, spec.getHoldTimeSeconds()));
                    }
                });

                if (timeInCorrectRange >= spec.getHoldTimeSeconds()) {
                    // user has held posture long enough
                    runOnUiThread(() -> {
                        android.util.Log.d("CameraExercise",
                                "Hold complete! " + spec.getHoldTimeSeconds() + "s");

                    });
                }

            } else {
                // For rep-based exercises
                if (spec.getRepGoal() > 0 && spec.getAnglesToTrack().size() == 1) {
                    float angleDegrees = computeAngleForName(spec.getAnglesToTrack().get(0));


                    if (!finalAllAnglesInRange) {

                        runOnUiThread(() -> feedbackTextView.setText("Incorrect form!"));
                    } else {

                        runOnUiThread(() -> feedbackTextView.setText("Good posture!"));
                    }

                    // treat angle < 60 => "armDown", angle > 150 => "armUp"
                    if (!armDown && angleDegrees < 60f) {
                        armDown = true;
                    } else if (armDown && angleDegrees > 150f) {
                        repCount++;
                        armDown = false;
                        runOnUiThread(() -> {
                            android.util.Log.d("CameraExercise", "Rep count=" + repCount);
                            if (repCount >= spec.getRepGoal()) {
                                android.util.Log.d("CameraExercise",
                                        "Exercise complete! Reached repGoal=" + spec.getRepGoal());
                                // Show success dialog
                            }
                        });
                    }
                }
            }

        } finally {
            image.close();
        }
    }

    // Compute the angle for a given key (leftShoulderAngle)
    private float computeAngleForName(String angleName) {
        float angleDegrees = 0f;
        if ("leftShoulderAngle".equals(angleName)) {
            float sY = currentKeypoints[5][0];
            float sX = currentKeypoints[5][1];
            float eY = currentKeypoints[7][0];
            float eX = currentKeypoints[7][1];
            float wY = currentKeypoints[9][0];
            float wX = currentKeypoints[9][1];
            angleDegrees = computeAngle(sX, sY, eX, eY, wX, wY);

        } else if ("rightShoulderAngle".equals(angleName)) {
            float sY = currentKeypoints[6][0];
            float sX = currentKeypoints[6][1];
            float eY = currentKeypoints[8][0];
            float eX = currentKeypoints[8][1];
            float wY = currentKeypoints[10][0];
            float wX = currentKeypoints[10][1];
            angleDegrees = computeAngle(sX, sY, eX, eY, wX, wY);

        }
        else if ("crossBodyAngleLeft".equals(angleName)) {

            float lsX = currentKeypoints[5][1];
            float lsY = currentKeypoints[5][0];
            float rsX = currentKeypoints[6][1];
            float rsY = currentKeypoints[6][0];
            float leX = currentKeypoints[7][1];
            float leY = currentKeypoints[7][0];

            angleDegrees = computeAngle(lsX, lsY, rsX, rsY, leX, leY);
        }

        else if ("leftKneeAngle".equals(angleName)) {
            float hX = currentKeypoints[11][1], hY = currentKeypoints[11][0];
            float kX = currentKeypoints[13][1], kY = currentKeypoints[13][0];
            float aX = currentKeypoints[15][1], aY = currentKeypoints[15][0];
            angleDegrees = computeAngle(hX, hY, kX, kY, aX, aY);

        } else if ("rightKneeAngle".equals(angleName)) {
            float hX = currentKeypoints[12][1], hY = currentKeypoints[12][0];
            float kX = currentKeypoints[14][1], kY = currentKeypoints[14][0];
            float aX = currentKeypoints[16][1], aY = currentKeypoints[16][0];
            angleDegrees = computeAngle(hX, hY, kX, kY, aX, aY);

        } else if ("leftHipAngle".equals(angleName)) {
            float sX = currentKeypoints[5][1],  sY = currentKeypoints[5][0];
            float hX = currentKeypoints[11][1], hY = currentKeypoints[11][0];
            float kX = currentKeypoints[13][1], kY = currentKeypoints[13][0];
            angleDegrees = computeAngle(sX, sY, hX, hY, kX, kY);

        } else if ("rightHipAngle".equals(angleName)) {
            float sX = currentKeypoints[6][1],  sY = currentKeypoints[6][0];
            float hX = currentKeypoints[12][1], hY = currentKeypoints[12][0];
            float kX = currentKeypoints[14][1], kY = currentKeypoints[14][0];
            angleDegrees = computeAngle(sX, sY, hX, hY, kX, kY);
        }

        return angleDegrees;
    }

    // Converts the raw camera frame into a ByteBuffer that can be sent to the TFLite model
    @ExperimentalGetImage
    private ByteBuffer prepareInput(ImageProxy image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Convert YUV to RGB
        Bitmap rgbBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        yuvToRgbConverter.yuvToRgb(image, rgbBitmap);

        // For front camera in portrait mode

        Matrix matrix = new Matrix();
        matrix.preScale(-1f, 1f);
        matrix.postRotate(90f); // or 270f

        Bitmap rotatedBitmap = Bitmap.createBitmap(
                rgbBitmap, 0, 0, rgbBitmap.getWidth(), rgbBitmap.getHeight(),
                matrix, false
        );

        // 3) Do the final resize on rotatedBitmap
        Bitmap resized = Bitmap.createScaledBitmap(
                rotatedBitmap, MODEL_WIDTH, MODEL_HEIGHT, false
        );

        // Prepare the ByteBuffer for uint8 data
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(MODEL_WIDTH * MODEL_HEIGHT * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[MODEL_WIDTH * MODEL_HEIGHT];
        resized.getPixels(pixels, 0, MODEL_WIDTH, 0, 0, MODEL_WIDTH, MODEL_HEIGHT);

        for (int p : pixels) {
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = (p & 0xFF);

            inputBuffer.put((byte) r);
            inputBuffer.put((byte) g);
            inputBuffer.put((byte) b);
        }

        inputBuffer.rewind();
        return inputBuffer;
    }

    // Compute the angle between three points (A, B, C) where B is the vertex
    private float computeAngle(float ax, float ay, float bx, float by, float cx, float cy) {

        float bax = ax - bx;
        float bay = ay - by;
        float bcx = cx - bx;
        float bcy = cy - by;

        float dot = (bax * bcx + bay * bcy);
        float magBA = (float) Math.sqrt(bax * bax + bay * bay);
        float magBC = (float) Math.sqrt(bcx * bcx + bcy * bcy);
        if (magBA < 1e-5 || magBC < 1e-5) return 0f;

        float cosine = dot / (magBA * magBC);
        if (cosine > 1f) cosine = 1f;
        else if (cosine < -1f) cosine = -1f;

        float angleRad = (float) Math.acos(cosine);
        return (float) Math.toDegrees(angleRad);
    }

    // Update rep count based on elbow angle changes
    private void updateElbowRep(float angle) {
        if (!armDown && angle < 60f) {
            // Detected bend in elbow
            armDown = true;
        } else if (armDown && angle > 150f) {
            // Moved from bent to extended, count as a rep
            repCount++;
            armDown = false;

            runOnUiThread(() -> {
                android.util.Log.d("CameraExercise", "Rep count=" + repCount);

            });
        }
    }

    // Bottom navigation
    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        } else if (itemId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        } else if (itemId == R.id.nav_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (tflite != null) {
            tflite.close();
        }
        if (yuvToRgbConverter != null) {
            yuvToRgbConverter.release();
        }
    }
}
