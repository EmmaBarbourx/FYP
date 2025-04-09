package com.example.injuryrecoveryapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class PoseOverlayView extends View {

    // Holds the current list of keypoints
    private float[][] keypoints = null;

    private final Paint circlePaint;
    private final Paint linePaint;

    // Define the skeleton pairs for connecting
    private static final int[][] SKELETON = {
            {5, 6},   // leftShoulder -> rightShoulder
            {5, 7},   // leftShoulder -> leftElbow
            {7, 9},   // leftElbow -> leftWrist
            {6, 8},   // rightShoulder -> rightElbow
            {8, 10},  // rightElbow -> rightWrist
            {5, 11},  // leftShoulder -> leftHip
            {6, 12},  // rightShoulder -> rightHip
            {11, 12}, // leftHip -> rightHip
            {11, 13}, // leftHip -> leftKnee
            {13, 15}, // leftKnee -> leftAnkle
            {12, 14}, // rightHip -> rightKnee
            {14, 16}, // rightKnee -> rightAnkle
    };

    public PoseOverlayView(Context context) {
        this(context, null);
    }

    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        circlePaint = new Paint();
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setStrokeWidth(6f);

        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
    }

    // Updates the list of keypoints and triggers a redraw of this view
    public void setKeypoints(float[][] keypoints) {
        this.keypoints = keypoints;
        invalidate(); // redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (keypoints == null) return;

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        //  Draw circles at each keypoint
        for (int i = 0; i < keypoints.length; i++) {
            float yNorm = keypoints[i][0];
            float xNorm = keypoints[i][1];
            float confidence = keypoints[i][2];

            // Skip if confidence < 0.3
            if (confidence < 0.3f) continue;

            // Scale from [0..1] to actual view size
            float cx = xNorm * viewWidth;
            float cy = yNorm * viewHeight;

            // Draw a circle
            canvas.drawCircle(cx, cy, 8f, circlePaint);
        }

        // Draw lines connecting the skeleton pairs
        for (int[] pair : SKELETON) {
            int i1 = pair[0];
            int i2 = pair[1];

            float conf1 = keypoints[i1][2];
            float conf2 = keypoints[i2][2];

            // Only draw a line if both keypoints are confident enough
            if (conf1 >= 0.3f && conf2 >= 0.3f) {
                float x1 = keypoints[i1][1] * viewWidth;
                float y1 = keypoints[i1][0] * viewHeight;
                float x2 = keypoints[i2][1] * viewWidth;
                float y2 = keypoints[i2][0] * viewHeight;

                canvas.drawLine(x1, y1, x2, y2, linePaint);
            }
        }
    }
}
