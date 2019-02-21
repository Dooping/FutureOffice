package com.office.future.futureoffice;

import java.io.Serializable;

/**
 * Created by Dooping on 15/05/2016.
 */
public class MyPreferences implements Serializable {
    private static final long serialVersionUID = 0L;
    private int luminosity;
    private boolean ac;

    public MyPreferences(){}

    public int getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(int luminosity) {
        this.luminosity = luminosity;
    }

    public boolean isAc() {
        return ac;
    }

    public void setAc(boolean ac) {
        this.ac = ac;
    }
}
