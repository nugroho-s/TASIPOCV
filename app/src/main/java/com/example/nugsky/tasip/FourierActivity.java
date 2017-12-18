package com.example.nugsky.tasip;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.nugsky.imageproc.ImageProc;
import com.example.nugsky.tasip.utils.LastPhotoWrapper;
import com.github.chrisbanes.photoview.PhotoView;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class FourierActivity extends AppCompatActivity {
    PhotoView srcPhoto;
    PhotoView fourierPhoto;
    PhotoView dstPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourier);
        srcPhoto = (PhotoView) findViewById(R.id.src_photo);
        fourierPhoto = (PhotoView) findViewById(R.id.fourier_photo);
        dstPhoto = (PhotoView) findViewById(R.id.dst_photo);

        Bitmap srcBmp = LastPhotoWrapper.bitmap;
        Mat srcColor = ImageProc.bitmap2Mat(srcBmp);
        Mat src = ImageProc.toGrayscale(srcColor);
        Mat complexI = new Mat();
        List<Mat> planes = new ArrayList<>();
        Mat fourier = ImageProc.fastFourier(src,planes,complexI);
        Mat restored = ImageProc.fastFourierInverse(planes,complexI);
        srcPhoto.setImageBitmap(ImageProc.mat2Bitmap(src));
        fourierPhoto.setImageBitmap(ImageProc.mat2Bitmap(fourier));
        dstPhoto.setImageBitmap(ImageProc.mat2Bitmap(restored));
    }
}
