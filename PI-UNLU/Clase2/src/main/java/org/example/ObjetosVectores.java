package org.example;

import java.io.Serializable;

public class ObjetosVectores {
    int[] v1;
    int[] v2;
    int[] v3;

    public ObjetosVectores(int[] v1, int[] v2, int[] v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public ObjetosVectores() {

    }

    public int[] getV1() {
        return v1;
    }

    public void setV1(int[] v1) {
        this.v1 = v1;
    }

    public int[] getV2() {
        return v2;
    }

    public void setV2(int[] v2) {
        this.v2 = v2;
    }

    public int[] getV3() {
        return v3;
    }

    public void setV3(int[] v3) {
        this.v3 = v3;
    }
}
