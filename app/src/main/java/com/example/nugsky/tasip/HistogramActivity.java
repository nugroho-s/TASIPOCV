package com.example.nugsky.tasip;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.nugsky.tasip.utils.LastPhotoWrapper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.vision.text.Line;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class HistogramActivity extends AppCompatActivity {
    private static final String TAG = "HistogramActivity";

    static {
        System.loadLibrary("native-lib");
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG,"OCV not loaded!");
        } else {
            Log.d(TAG,"OCV loaded!");
        }
    }

    private ImageView imageView;
    private LineChart blueChart;
    private LineChart greenChart;
    private LineChart redChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram);
        imageView = (ImageView) findViewById(R.id.iv_photo);
        redChart = (LineChart) findViewById(R.id.red_chart);
        greenChart = (LineChart) findViewById(R.id.green_chart);
        blueChart = (LineChart) findViewById(R.id.blue_chart);

        imageView.setImageBitmap(LastPhotoWrapper.bitmap);
        Mat mat = new Mat();
        Utils.bitmapToMat(LastPhotoWrapper.bitmap, mat);
        new HistogramCalc().execute(mat);
    }

    private class HistogramCalc extends AsyncTask<Mat,Integer,Void>{
        List<Entry> redEntries = new ArrayList<>();
        List<Entry> greenEntries = new ArrayList<>();
        List<Entry> blueEntries = new ArrayList<>();

        LineData redData;
        LineData greenData;
        LineData blueData;

        @Override
        protected Void doInBackground(Mat... params) {
            List<Mat> images = new ArrayList<>();
            Core.split(params[0],images);

            // set the number of bins at 256
            MatOfInt histSize = new MatOfInt(256);
            // only one channel
            MatOfInt channels = new MatOfInt(0);
            // set the ranges
            MatOfFloat histRange = new MatOfFloat(0, 256);

            // compute the histograms for the B, G and R components
            Mat hist_b = new Mat();
            Mat hist_g = new Mat();
            Mat hist_r = new Mat();

            // B component or gray image
            Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);

            // G and R components (if the image is not in gray scale)
            Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
            Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);

            for(int i=0;i<256;i++){
                redEntries.add(new Entry(i, (float) hist_r.get(i,0)[0]));
                greenEntries.add(new Entry(i, (float) hist_g.get(i,0)[0]));
                blueEntries.add(new Entry(i, (float) hist_b.get(i,0)[0]));
            }

            LineDataSet redDataSet = new LineDataSet(redEntries,"RED");
            LineDataSet greenDataSet = new LineDataSet(redEntries,"GREEN");
            LineDataSet blueDataSet = new LineDataSet(redEntries,"BLUE");
            redDataSet.setColor(Color.RED);
            greenDataSet.setColor(Color.GREEN);
            blueDataSet.setColor(Color.BLUE);

            redData = new LineData(redDataSet);
            greenData = new LineData(greenDataSet);
            blueData = new LineData(blueDataSet);

            Log.d(TAG, String.valueOf(hist_b.get(1,0)[0]));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            redChart.setData(redData);
            greenChart.setData(greenData);
            blueChart.setData(blueData);

            redChart.setVisibility(View.VISIBLE);
            greenChart.setVisibility(View.VISIBLE);
            blueChart.setVisibility(View.VISIBLE);
        }
    }
}
