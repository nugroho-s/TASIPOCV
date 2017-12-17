package com.example.nugsky.tasip.utils;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.HashMap;

public class FaceView extends View {
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float ID_TEXT_SIZE = 40.0f;

    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the bitmap background and the associated face detections.
     */
    public void setContent(Bitmap bitmap, SparseArray<Face> faces) {
        mBitmap = bitmap;
        mFaces = faces;
        invalidate();
    }

    /**
     * Draws the bitmap background and the associated face landmarks.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
            drawFaceAnnotations(canvas, scale);
        }
    }

    /**
     * Draws the bitmap background, scaled to the device size.  Returns the scale for future use in
     * positioning the facial landmark graphics.
     */
    private double drawBitmap(Canvas canvas) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        Rect destBounds = new Rect(0, 0, (int)(imageWidth * scale), (int)(imageHeight * scale));
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    /**
     * Draws a small circle for each detected landmark, centered at the detected landmark position.
     * <p>
     *
     * Note that eye landmarks are defined to be the midpoint between the detected eye corner
     * positions, which tends to place the eye landmarks at the lower eyelid rather than at the
     * pupil position.
     */
    private void drawFaceAnnotations(Canvas canvas, double scale) {
        Paint mBoxPaint = new Paint();
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        Paint mIdPaint = new Paint();
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        for (int i = 0; i < mFaces.size(); ++i) {
            Face face = mFaces.valueAt(i);
            mBoxPaint.setColor(COLOR_CHOICES[i%COLOR_CHOICES.length]);
            mIdPaint.setColor(COLOR_CHOICES[i%COLOR_CHOICES.length]);
            // Draws a bounding box around the face.
            float x = (face.getPosition().x + face.getWidth() / 2);
            float y = (face.getPosition().y + face.getHeight() / 2);
            float xOffset = (face.getWidth() / 2.0f);
            float yOffset = (face.getHeight() / 2.0f);
            float left = x - xOffset;
            float top = y - yOffset;
            float right = x + xOffset;
            float bottom = y + yOffset;
            left *= scale;
            top *= scale;
            right *= scale;
            bottom *= scale;
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            HashMap<Integer,Landmark> mapLandmark = new HashMap<>();
            for (Landmark landmark : face.getLandmarks()) {
                mapLandmark.put(landmark.getType(),landmark);
            }

//            Landmark nose = mapLandmark.get(Landmark.NOSE_BASE);
//            if (nose != null){
//                int cx = (int) (nose.getPosition().x * scale);
//                int cy = (int) (nose.getPosition().y * scale);
//                canvas.drawCircle(cx, cy, 5, mBoxPaint);
//            }

            Landmark leftEye = mapLandmark.get(Landmark.LEFT_EYE);
            Landmark rightEye = mapLandmark.get(Landmark.RIGHT_EYE);
            int censorOffset = 0;
            if((leftEye!=null)&&(rightEye!=null)){
                PointF leftEyePos = leftEye.getPosition();
                PointF rightEyePos = rightEye.getPosition();
                mBoxPaint.setStrokeWidth(2);
                canvas.drawLine((float)((leftEyePos.x+censorOffset)*scale), (float)(leftEyePos.y*scale),
                        (float)((rightEyePos.x-censorOffset)*scale),(float)(rightEyePos.y*scale),mBoxPaint);
            }

            canvas.drawText(FaceClassifier.classifyFace(face), left+5, top+ID_TEXT_SIZE, mIdPaint);
        }
    }
}