package org.geotools.tutorial.model;


import com.vividsolutions.jts.geom.LineString;

public class Interval {
    private LineString lineString;
    private String pN;
    private double length;

    public Interval() {
    }

    public LineString getLineString() {
        return lineString;
    }

    public void setLineString(LineString lineString) {
        this.lineString = lineString;
    }

    public String getpN() {
        return pN;
    }

    public void setpN(String pN) {
        this.pN = pN;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "Interval{" +
                "lineString=" + lineString +
                ", pN='" + pN + '\'' +
                ", length=" + length +
                '}';
    }


}
