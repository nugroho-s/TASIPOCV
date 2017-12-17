package com.example.nugsky.imageproc;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import com.example.nugsky.imageproc.filter.KernelFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
}
