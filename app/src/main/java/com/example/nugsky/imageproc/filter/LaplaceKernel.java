package com.example.nugsky.imageproc.filter;

import java.util.HashMap;

public class LaplaceKernel implements KernelFilter {
    @Override
    public HashMap<Integer, double[][]> getKernel() {
        return new HashMap<Integer, double[][]>(){{
            put(6,new double[][]{
                    {-1, 1},
                    {0, 0}
            });
            put(8,new double[][]{
                    {-1, 0},
                    {1, 0}
            });
        }};
    }

    @Override
    public double getFactor() {
        return 1.0;
    }

    @Override
    public double getBias() {
        return 0;
    }
}
