package com.example.nugsky.tasip;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.nugsky.imageproc.ImageProc;
import com.example.nugsky.tasip.utils.LastPhotoWrapper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class EqualizeActivity extends AppCompatActivity {

    ImageView srcImage;
    ImageView dstImage;
    LineChart normalChart;
    LineChart equalizedChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalize);
        srcImage = (ImageView) findViewById(R.id.src_photo);
        dstImage = (ImageView) findViewById(R.id.dst_photo);
        normalChart = (LineChart) findViewById(R.id.normal_chart);
        equalizedChart = (LineChart) findViewById(R.id.equalized_chart);
        Bitmap bmp = LastPhotoWrapper.bitmap;
        Mat mat = ImageProc.bitmap2Mat(bmp);
        Mat gray = ImageProc.toGrayscale(mat);
        Bitmap cvtRes = ImageProc.mat2Bitmap(gray);
        srcImage.setImageBitmap(cvtRes);
        Mat equalized = new Mat();
        Imgproc.equalizeHist(gray,equalized);
        dstImage.setImageBitmap(ImageProc.mat2Bitmap(equalized));
        new DrawHistogram().execute(gray,equalized);
    }

    class DrawHistogram extends AsyncTask<Mat,Void,Void>{
        LineData normalData;
        LineData equalizedData;

        @Override
        protected Void doInBackground(Mat... params) {
            Mat grayHistAwal = ImageProc.getGrayHist(params[0]);
            Mat grayHistAkhir = ImageProc.getGrayHist(params[1]);
            List<Entry> normalEntries = new ArrayList<>();
            List<Entry> equalizedEntries = new ArrayList<>();

            for(int i=0;i<256;i++){
                normalEntries.add(new Entry(i, (float) grayHistAwal.get(i,0)[0]));
                equalizedEntries.add(new Entry(i, (float) grayHistAkhir.get(i,0)[0]));
            }

            LineDataSet normalDataSet = new LineDataSet(normalEntries,"normal");
            LineDataSet equalizedDataSet = new LineDataSet(equalizedEntries,"equalized");

            normalData = new LineData(normalDataSet);
            equalizedData = new LineData(equalizedDataSet);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            normalChart.setData(normalData);
            equalizedChart.setData(equalizedData);
            normalChart.setVisibility(View.VISIBLE);
            equalizedChart.setVisibility(View.VISIBLE);
        }
    }
}
