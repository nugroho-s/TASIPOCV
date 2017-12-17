package com.example.nugsky.tasip.utils;

import android.media.FaceDetector;
import android.util.SparseArray;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

public class FaceDetectorWrapper extends Detector {
    private Detector mDelegate;

    public FaceDetectorWrapper(Detector delegate) {
        super();
        mDelegate = delegate;
    }

    public SparseArray<FaceDetector.Face> detect(Frame frame) {
        return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}