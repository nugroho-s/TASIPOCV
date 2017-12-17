package com.example.nugsky.tasip;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.nugsky.imageproc.ImageProc;
import com.example.nugsky.tasip.utils.LastPhotoWrapper;

import org.opencv.core.Mat;

public class OtsuActivity extends AppCompatActivity {
    ImageView srcView;
    ImageView dstView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otsu);
        srcView = (ImageView) findViewById(R.id.src_photo);
        dstView = (ImageView) findViewById(R.id.dst_photo);
        Bitmap srcBmp = LastPhotoWrapper.bitmap;
        Mat src = ImageProc.toGrayscale(ImageProc.bitmap2Mat(srcBmp));
        Mat dst = ImageProc.otsuThreshold(src);
        srcView.setImageBitmap(ImageProc.mat2Bitmap(src));
        dstView.setImageBitmap(ImageProc.mat2Bitmap(dst));
    }
}
