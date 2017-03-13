package org.geotools.tutorial.util;

import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;


public class PointsCollector {
    private static ArrayList<Point> points = new ArrayList<Point>();

    public static void collect(Point point){
        points.add(point);
    }

    public static void printResult(){
        for(Point point : points){
            System.out.println(points.toString());
        }
    }

    public static ArrayList<Point> getPoints() {
        return points;
    }

    public static void setPoints(ArrayList<Point> points) {
        PointsCollector.points = points;
    }
}
