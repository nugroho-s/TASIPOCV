package com.example.nugsky.tasip.utils;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.*;

public class FaceClassifier {
    static ArrayList<FacePropertiesWrapper> wajahOrang = new ArrayList<FacePropertiesWrapper>(){{
        add(new FacePropertiesWrapper("Nugroho",144,264.0303,244.1311,238.73));
        add(new FacePropertiesWrapper("Majid",144.0555,248.03226,221.7747,206.4946));
        add(new FacePropertiesWrapper("Febi",124,220.1454,199.2787,190.0316));
    }};

    public static String classifyFace(Face face){
        List<Landmark> landmarks = face.getLandmarks();
        HashMap<Integer,Landmark> isPresent = new HashMap<>();
        for (Landmark landmark :
                landmarks) {
            isPresent.put(landmark.getType(), landmark);
        }
        if (!isEligibleImage(isPresent))
            return "unknown";
        Landmark leftEye = isPresent.get(Landmark.LEFT_EYE);
        Landmark rightEye = isPresent.get(Landmark.RIGHT_EYE);
        Landmark nose = isPresent.get(Landmark.NOSE_BASE);
        Landmark mouth = isPresent.get(Landmark.BOTTOM_MOUTH);
        double eyeDist = Math.hypot((leftEye.getPosition().x-rightEye.getPosition().x),
                (leftEye.getPosition().y-rightEye.getPosition().y));
        double noseMouthDist = Math.hypot((nose.getPosition().x-mouth.getPosition().x),
                (nose.getPosition().y-mouth.getPosition().y));
        double leftEyeNoseDist = Math.hypot((leftEye.getPosition().x-nose.getPosition().x),
                (leftEye.getPosition().y-nose.getPosition().y));
        double rightEyeNoseDist = Math.hypot((rightEye.getPosition().x-nose.getPosition().x),
                (rightEye.getPosition().y-nose.getPosition().y));
        FacePropertiesWrapper orang = new FacePropertiesWrapper("temp",noseMouthDist,eyeDist,leftEyeNoseDist,rightEyeNoseDist);

        ArrayList<DifferenceWrapper> diffs = new ArrayList<>();

        for(FacePropertiesWrapper x:wajahOrang){
            diffs.add(new DifferenceWrapper(x.label,x.difference(orang)));
        }

        Collections.sort(diffs,new DifferenceWrapperComparator());

        return diffs.get(0).label;
    }

    private static boolean isEligibleImage(HashMap<Integer, Landmark> isPresent){
        boolean eyePresent, nosePresent, mouthPresent;
        eyePresent = isPresent.containsKey(Landmark.RIGHT_EYE)&&isPresent.containsKey(Landmark.LEFT_EYE);
        nosePresent = isPresent.containsKey(Landmark.NOSE_BASE);
        mouthPresent = isPresent.containsKey(Landmark.BOTTOM_MOUTH);
        return (eyePresent&&mouthPresent&&nosePresent);
    }

    static class DifferenceWrapper{
        String label;
        double diff;

        public DifferenceWrapper(String label, double diff) {
            this.label = label;
            this.diff = diff;
        }
    }

    public static class DifferenceWrapperComparator implements Comparator<DifferenceWrapper> {
        @Override
        public int compare(DifferenceWrapper o1, DifferenceWrapper o2) {
            if(o1.diff==o2.diff)
                return 0;
            if(o1.diff>o2.diff)
                return 1;
            return -1;
        }
    }
}