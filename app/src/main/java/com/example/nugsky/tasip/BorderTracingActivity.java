package com.example.nugsky.tasip;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nugsky.imageproc.ImageProc;
import com.example.nugsky.tasip.utils.LastPhotoWrapper;
import com.example.nugsky.tasip.utils.Utils;
import com.github.chrisbanes.photoview.PhotoView;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.List;

public class BorderTracingActivity extends AppCompatActivity {
    PhotoView srcView;
    PhotoView dstView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_border_tracing);
        srcView = (PhotoView) findViewById(R.id.src_photo);
        dstView = (PhotoView) findViewById(R.id.dst_photo);
        Bitmap srcBmp = LastPhotoWrapper.bitmap;
        Mat srcGray = ImageProc.toGrayscale(ImageProc.bitmap2Mat(srcBmp));
        Mat srcBw = ImageProc.otsuThreshold(srcGray);
        List<MatOfPoint> contours = ImageProc.findChainCode(srcBw);
        Mat dst = ImageProc.drawContours(contours,srcBw.rows(),srcBw.cols());
        srcView.setImageBitmap(ImageProc.mat2Bitmap(srcGray));
        dstView.setImageBitmap(ImageProc.mat2Bitmap(dst));
    }
}
