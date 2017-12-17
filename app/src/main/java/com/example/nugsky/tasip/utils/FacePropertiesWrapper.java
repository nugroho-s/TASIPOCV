package com.example.nugsky.tasip.utils;

public class FacePropertiesWrapper {
    public String label;
    public double eyesDistance;
    public double leftEyeNoseDistance;
    public double rightEyeNoseDistance;

    public FacePropertiesWrapper(String label, double mouthNoseDistance,double eyesDistance,
                                 double leftEyeNose, double rightEyeNoseDistance){
        this.label = label;
        this.eyesDistance = eyesDistance/mouthNoseDistance;
        this.leftEyeNoseDistance = leftEyeNose/mouthNoseDistance;
        this.rightEyeNoseDistance = rightEyeNoseDistance/mouthNoseDistance;
    }

    public double difference(FacePropertiesWrapper another){
        double eyeDiff = Math.abs(eyesDistance-another.eyesDistance);
        double leftEyeNoseDiff = Math.abs(leftEyeNoseDistance-another.leftEyeNoseDistance);
        double rightEyeNoseDiff = Math.abs(rightEyeNoseDistance-another.rightEyeNoseDistance);

        return eyeDiff+leftEyeNoseDiff+rightEyeNoseDiff;
    }
}
