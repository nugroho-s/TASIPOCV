package com.example.nugsky.imageproc.filter;

import java.util.HashMap;

public class PrewittKernel implements KernelFilter {
    @Override
    public HashMap<Integer, double[][]> getKernel() {
        return new HashMap<Integer, double[][]>(){{
            put(6,new double[][]{
                    {-1, 0, 1},
                    {-1, 0, 1},
                    {-1, 0, 1}
            });
            put(8,new double[][]{
                    {-1, -1, -1},
                    {0, 0, 0},
                    {1, 1, 1}
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
