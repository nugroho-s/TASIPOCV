package com.example.nugsky.imageproc.filter;

import java.util.HashMap;

/**
 * arah
 * 1 2 3
 * 4 5 6
 * 7 8 9
 */

public interface KernelFilter {
    HashMap<Integer,double[][]> getKernel();
    double getFactor();
    double getBias();
}
