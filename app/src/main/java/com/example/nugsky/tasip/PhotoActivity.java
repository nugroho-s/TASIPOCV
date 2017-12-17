package com.example.nugsky.tasip;

import android.content.Context;
import android.graphics.*;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.example.nugsky.tasip.utils.FaceDetectorWrapper;
import com.example.nugsky.tasip.utils.FaceView;
import com.example.nugsky.tasip.utils.LastPhotoWrapper;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.concurrent.Future;

public class PhotoActivity extends AppCompatActivity {
    public static PhotoActivity sInstance;

    private Bitmap bitmap;

    private Button proses;

    public Bitmap bwBitmap;
    SparseArray<Face> faces;
    FaceView overlay;
    ImageView bwImage;

    Future<Bitmap> future;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        sInstance = this;
        bitmap = LastPhotoWrapper.bitmap;
        FaceDetector detector = new FaceDetector.Builder(this)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        FaceDetectorWrapper faceDetectorWrapper = new FaceDetectorWrapper(detector);
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        faces = detector.detect(frame);

        overlay = (FaceView) findViewById(R.id.faceView);
        proses = (Button) findViewById(R.id.prosesButton);
        bwImage = (ImageView) findViewById(R.id.bwImage);
//        proses.setOnClickListener((View view)->{
//            try {
//                future.get();
//                bwImage.setImageBitmap(bwBitmap);
//                overlay.setVisibility(View.GONE);
//                proses.setVisibility(View.GONE);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                Log.e("Exception","",e);
//            }
//        });
        new ConverttoGrayAsync(this).execute(bitmap);
    }

    class ConverttoGrayAsync extends AsyncTask<Bitmap, Integer, Bitmap> {
        private Context context;
        private Bitmap src;

        public ConverttoGrayAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            AlertDialog.Builder b = new AlertDialog.Builder(context);
            b.setTitle("Example");
            String[] types = {"By Zip", "By Category"};

            src = bitmaps[0];

            int width = bitmaps[0].getWidth();
            int height = bitmaps[0].getHeight();

            final Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmpGrayscale);
            Paint paint = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bitmaps[0], 0, 0, paint);

//            ExecutorService executor = Executors.newFixedThreadPool(1);
//            Callable<Bitmap> task = () -> {
//                try{
//                    bwBitmap = new OtsuThresholder().getBW(bmpGrayscale);
//                } catch (Exception e){
//                    Log.e("Exception","",e);
//                }
//                return bwBitmap;
//            };
//            future = executor.submit(task);
            return bmpGrayscale;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            super.onPostExecute(bitmap);
            overlay.setContent(bitmap, faces);
        }
    }
}
