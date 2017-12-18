package com.example.nugsky.imageproc;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import com.example.nugsky.imageproc.filter.KernelFilter;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class ImageProc {
    private static final String TAG = "ImageProc";

    public static Bitmap convertToGray(final Bitmap src){
        Bitmap gray = Bitmap.createBitmap(src.getWidth(),src.getHeight(), Bitmap.Config.RGB_565);
        int height = src.getHeight();
        int width = src.getWidth();

        for(int y=0;y<height;y++){
            for(int x=0;x<width;x++){
                int pixelColor = src.getPixel(x,y);
                int r = Color.red(pixelColor);
                int g = Color.green(pixelColor);
                int b = Color.blue(pixelColor);
                int grey = (r+g+b)/3;
                gray.setPixel(x,y,Color.rgb(grey,grey,grey));
            }
            if (y%100==0)
                Log.d(TAG.concat(".convertToGray"),String.format("%d/%d",y,height));
        }
        return gray;
    }

    public static Bitmap convertToGrayv2(final Bitmap src){
        Bitmap gray = Bitmap.createBitmap(src.getWidth(),src.getHeight(), Bitmap.Config.RGB_565);
        int height = src.getHeight();
        int width = src.getWidth();

        int threads = Runtime.getRuntime().availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        int deviderY = height/threads;

        for(int t=0;t<threads-1;t++){
            GrayRunnable grayRunnable = new GrayRunnable(src,gray,deviderY*t,deviderY*t+(deviderY));
            executor.submit(grayRunnable);
        }
        GrayRunnable grayRunnable = new GrayRunnable(src,gray,deviderY*(threads-1),height);
        executor.submit(grayRunnable);

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        return gray;
    }

    public static Bitmap medianFilter(final Bitmap src, int filterWidth, int filterHeight){
        Bitmap filtered = Bitmap.createBitmap(src.getWidth(),src.getHeight(), Bitmap.Config.RGB_565);
        int height = src.getHeight();
        int width = src.getWidth();
        int medianIdx = (filterHeight*filterWidth)/2;

        ArrayList<Integer> colors = new ArrayList<>(filterWidth*filterHeight);

        for(int y=0;y<height;y++){
            for(int x=0;x<width;x++){
                colors.clear();
                for(int filterY = 0; filterY < filterHeight; filterY++)
                    for(int filterX = 0; filterX < filterWidth; filterX++){
                        int imageX = (x - filterWidth / 2 + filterX + width) % width;
                        int imageY = (y - filterHeight / 2 + filterY + height) % height;

                        colors.add(Color.red(src.getPixel(imageX,imageY)));
                    }

                Collections.sort(colors);
                int medColor = colors.get(medianIdx);
                filtered.setPixel(x,y,Color.rgb(medColor,medColor,medColor));
            }
            if (y%100==0)
                Log.d(TAG.concat(".medianFilter"),String.format("%d/%d",y,height));
        }

        return filtered;
    }

    public static Bitmap medianFilterv2(final Bitmap src, int filterWidth, int filterHeight){
        Bitmap filtered = Bitmap.createBitmap(src.getWidth(),src.getHeight(), Bitmap.Config.RGB_565);
        int height = src.getHeight();
        int width = src.getWidth();
        int medianIdx = (filterHeight*filterWidth)/2;

        int threads = Runtime.getRuntime().availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        int deviderY = height/threads;

        for(int t=0;t<threads-1;t++){
            MedianFilter medianFilter = new MedianFilter(src,filtered,deviderY*t,deviderY*t+(deviderY),filterWidth,filterHeight);
            executor.submit(medianFilter);
        }
        MedianFilter medianFilter = new MedianFilter(src,filtered,deviderY*(threads-1),height,filterWidth,filterHeight);
        executor.submit(medianFilter);

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
        return filtered;
    }

    public static Bitmap convulutionThreaded(Bitmap src, KernelFilter kernelFilter){
        HashMap<Integer, double[][]> kernelMap = kernelFilter.getKernel();
        Set<Integer> arahKernel = kernelMap.keySet();
        Bitmap ret = Bitmap.createBitmap(src);
        for(int arah : arahKernel){
            double[][] kernel = kernelMap.get(arah);
            int filterWidth = kernel[0].length;
            int filterHeight = kernel.length;
            ret = convulutionFilterv2(src, arah, filterWidth, filterHeight, kernel, kernelFilter.getFactor(), kernelFilter.getBias());
            src = Bitmap.createBitmap(ret);
        }
        return ret;
    }

    private static Bitmap convulutionFilterv2(Bitmap src,int direction, int filterWidth, int filterHeight, double[][] filter,
                                              double factor, double bias){
        Bitmap filtered = Bitmap.createBitmap(src.getWidth(),src.getHeight(), Bitmap.Config.RGB_565);
        int height = src.getHeight();
        int width = src.getWidth();

        int threads = Runtime.getRuntime().availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        int devider = height/threads;

        if(direction==6)
            devider = height/threads;
        else if(direction==8)
            devider = width/threads;

        for(int t=0;t<threads-1;t++){
            ConvolutionRunnable convolutionR = new ConvolutionRunnable(src,filtered, direction,devider*t,devider*t+(devider),
                    filterWidth,filterHeight,filter,factor,bias);
            executor.submit(convolutionR);
        }
        ConvolutionRunnable ConvolutionR = new ConvolutionRunnable(src,filtered,direction,devider*(threads-1), (direction==6)?height:width,
                filterWidth,filterHeight,filter,factor,bias);
        executor.submit(ConvolutionR);

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
        return filtered;
    }

    private static class MedianFilter implements Runnable{
        private Bitmap src;
        private Bitmap filtered;
        private int startY;
        private int endY;
        private int filterWidth;
        private int filterHeight;

        public MedianFilter(Bitmap src, Bitmap filtered, int startY, int endY, int filterWidth, int filterHeight) {
            this.src = src;
            this.filtered = filtered;
            this.startY = startY;
            this.endY = endY;
            this.filterWidth = filterWidth;
            this.filterHeight = filterHeight;
            Log.d(TAG+".MedianFilter",String.format("median from %d to %d",startY,endY));
        }

        @Override
        public void run() {
            int width = src.getWidth();
            int height = src.getHeight();
            int medianIdx = (filterHeight*filterWidth)/2;

            ArrayList<Integer> colors = new ArrayList<>(filterWidth*filterHeight);

            for(int y=startY;y<endY;y++){
                for(int x=0;x<width;x++){
                    colors.clear();
                    for(int filterY = 0; filterY < filterHeight; filterY++)
                        for(int filterX = 0; filterX < filterWidth; filterX++){
                            int imageX = (x - filterWidth / 2 + filterX + width) % width;
                            int imageY = (y - filterHeight / 2 + filterY + height) % height;

                            colors.add(Color.red(src.getPixel(imageX,imageY)));
                        }

                    Collections.sort(colors);
                    int medColor = colors.get(medianIdx);
                    filtered.setPixel(x,y,Color.rgb(medColor,medColor,medColor));
                }
                if (y%100==0)
                    Log.d(TAG.concat(".medianFilter"),String.format("%s %d/%d",Thread.currentThread().getName(),y,height));
            }
        }
    }

    private static class GrayRunnable implements Runnable{
        private Bitmap src;
        private Bitmap grayed;
        private int startY;
        private int endY;

        public GrayRunnable(Bitmap src, Bitmap grayed, int startY, int endY) {
            this.src = src;
            this.grayed = grayed;
            this.startY = startY;
            this.endY = endY;
        }

        @Override
        public void run() {
            int width = src.getWidth();
            int height = src.getHeight();

            for(int y=startY;y<endY;y++){
                for(int x=0;x<width;x++){
                    int pixelColor = src.getPixel(x,y);
                    int r = Color.red(pixelColor);
                    int g = Color.green(pixelColor);
                    int b = Color.blue(pixelColor);
                    int grey = (r+g+b)/3;
                    grayed.setPixel(x,y,Color.rgb(grey,grey,grey));
                }
                if (y%100==0)
                    Log.d(TAG.concat(".medianFilter"),String.format("%s %d/%d",Thread.currentThread().getName(),y,height));
            }
        }
    }

    private static class ConvolutionRunnable implements Runnable{
        private Bitmap src;
        private Bitmap filtered;
        private int direction;
        private int start;
        private int end;
        private int filterWidth;
        private int filterHeight;
        private double[][] filter;
        private double factor;
        private double bias;
        private int width;
        private int height;

        public ConvolutionRunnable(Bitmap src, Bitmap filtered, int direction, int start, int end, int filterWidth, int filterHeight,
                                   double[][] filter, double factor, double bias) {
            this.src = src;
            this.filtered = filtered;
            this.direction = direction;
            this.start = start;
            this.end = end;
            this.filterWidth = filterWidth;
            this.filterHeight = filterHeight;
            this.filter = filter;
            this.factor = factor;
            this.bias = bias;
            this.width = src.getWidth();
            this.height = src.getHeight();
        }

        @Override
        public void run() {
            if(direction==6){
                for(int y = start; y< end; y++){
                    for(int x=0;x<width;x++) {
                        setFilteredColor(x, y);
                    }
                    if (y%100==0)
                        Log.d(TAG.concat(".medianFilter"),String.format("%s %d/%d",Thread.currentThread().getName(),y,height));
                }
            } else if(direction==8){
                for(int y = 0; y< height; y++){
                    for(int x=start;x<end;x++) {
                        setFilteredColor(x, y);
                    }
                    if (y%100==0)
                        Log.d(TAG.concat(".medianFilter"),String.format("%s %d/%d",Thread.currentThread().getName(),y,height));
                }
            }
        }

        private void setFilteredColor(int x, int y){
            double color = 0.0;
            for(int filterY = 0; filterY < filterHeight; filterY++) {
                for (int filterX = 0; filterX < filterWidth; filterX++) {
                    int imageX = (x - filterWidth / 2 + filterX + width) % width;
                    int imageY = (y - filterHeight / 2 + filterY + height) % height;

                    int pixelCol = src.getPixel(imageX,imageY);
                    pixelCol = Color.red(pixelCol);
                    color += pixelCol*filter[filterY][filterX];
                }
            }

            int filteredVal = (int)(factor * color + bias);
            int filteredValTruncated = Math.min(Math.max(filteredVal,0),255);

            filtered.setPixel(x,y,Color.rgb(filteredValTruncated,filteredValTruncated,filteredValTruncated));
        }
    }

    public static Mat bitmap2Mat(Bitmap src){
        Mat dst = new Mat();
        Utils.bitmapToMat(src, dst);
        return dst;
    }

    public static Bitmap mat2Bitmap(Mat src){
        Bitmap dst = Bitmap.createBitmap(src.cols(),  src.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,dst);
        return dst;
    }

    public static Mat toGrayscale(Mat color){
        Mat gray = new Mat();
        Imgproc.cvtColor(color,gray,Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    public static Mat getGrayHist(Mat image){
        List<Mat> images = new ArrayList<>();
        Core.split(image,images);

        // set the number of bins at 256
        MatOfInt histSize = new MatOfInt(256);
        // only one channel
        MatOfInt channels = new MatOfInt(0);
        // set the ranges
        MatOfFloat histRange = new MatOfFloat(0, 256);

        Mat hist_g = new Mat();

        // B component or gray image
        Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_g, histSize, histRange, false);

        return hist_g;
    }

    public static Mat otsuThreshold(Mat src){
        Mat dst = new Mat();
        Imgproc.threshold(src,dst,0,255,Imgproc.THRESH_OTSU);
        return dst;
    }

    public static List<MatOfPoint> findChainCode(Mat src){
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    public static Mat drawContours(List<MatOfPoint> contours,int rows,int cols){
        Mat dst = new Mat(rows,cols, CvType.CV_8UC3);
        for(int i=0;i<contours.size();i++){
            Imgproc.drawContours(dst,contours,i,new Scalar(255,0,0));
        }
        return dst;
    }

    public static Mat fastFourier(Mat I,List<Mat> planes, Mat complexI){
        Mat padded = new Mat();                     //expand input image to optimal size
        int m = Core.getOptimalDFTSize( I.rows() );
        int n = Core.getOptimalDFTSize( I.cols() ); // on the border add zero values
        Core.copyMakeBorder(I, padded, 0, m - I.rows(), 0, n - I.cols(), Core.BORDER_CONSTANT, Scalar.all(0));
        padded.convertTo(padded, CvType.CV_32F);
        planes.add(padded);
        planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        Core.merge(planes, complexI);         // Add to the expanded another plane with zeros
        Core.dft(complexI, complexI);         // this way the result may fit in the source matrix

        int height = complexI.height();
        int width = complexI.width();

        int tengah = height/15;

        for(int i=0;i<tengah;i++){
            for(int j=0;j<tengah;j++){
                double[] data = complexI.get(i, j); //Stores element in an array
                complexI.put(i,j,0,0);
                complexI.put(height-i,width-j,0,0);
                complexI.put(i,width-j,0,0);
                complexI.put(height-i,j,0,0);
            }
        }

        // compute the magnitude and switch to logarithmic scale
        // => log(1 + sqrt(Re(DFT(I))^2 + Im(DFT(I))^2))
        Core.split(complexI, planes);                               // planes.get(0) = Re(DFT(I)
        // planes.get(1) = Im(DFT(I))
        Core.magnitude(planes.get(0), planes.get(1), planes.get(0));// planes.get(0) = magnitude
        Mat magI = planes.get(0);
        Mat matOfOnes = Mat.ones(magI.size(), magI.type());
        Core.add(matOfOnes, magI, magI);         // switch to logarithmic scale
        Core.log(magI, magI);
        // crop the spectrum, if it has an odd number of rows or columns
        magI = magI.submat(new Rect(0, 0, magI.cols() & -2, magI.rows() & -2));
        // rearrange the quadrants of Fourier image  so that the origin is at the image center
        int cx = magI.cols()/2;
        int cy = magI.rows()/2;
        Mat q0 = new Mat(magI, new Rect(0, 0, cx, cy));   // Top-Left - Create a ROI per quadrant
        Mat q1 = new Mat(magI, new Rect(cx, 0, cx, cy));  // Top-Right
        Mat q2 = new Mat(magI, new Rect(0, cy, cx, cy));  // Bottom-Left
        Mat q3 = new Mat(magI, new Rect(cx, cy, cx, cy)); // Bottom-Right
        Mat tmp = new Mat();               // swap quadrants (Top-Left with Bottom-Right)
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);
        q1.copyTo(tmp);                    // swap quadrant (Top-Right with Bottom-Left)
        q2.copyTo(q1);
        tmp.copyTo(q2);
        magI.convertTo(magI, CvType.CV_8UC1);
        Core.normalize(magI, magI, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1); // Transform the matrix with float values
        return magI;
    }

    public static Mat fastFourierInverse(List<Mat> planes, Mat complexI){
        Core.idft(complexI, complexI);
        Mat restoredImage = new Mat();
        Core.split(complexI, planes);
        Core.normalize(planes.get(0), restoredImage, 0, 255, Core.NORM_MINMAX);
        restoredImage.convertTo(restoredImage, CvType.CV_8U);
        return restoredImage;
    }
}
