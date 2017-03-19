package org.geotools.tutorial.util;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.referencing.operation.TransformException;

import java.text.ParseException;
import java.util.Arrays;


public class Perpedicular {
    private LineString perpendicular;
    private LineString baseLine;
    private Point pointA;
    private Point pointB;
    private Point pointC;
    private double azimuthAB;
    private double distanceAB;
    private GeometryLab geometryLab = new GeometryLab();


    public Perpedicular() {
    }

    public Perpedicular(Point pointA, Point pointB, Point pointC) throws TransformException, ParseException {
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
        this.azimuthAB = GeodeticLab.getAzimuth(pointA, pointB);
        this.distanceAB = GeodeticLab.getDistance(pointA, pointB);
        this.getBaseLine();
    }

    public LineString getPerpendicular() throws TransformException, ParseException {
        double radius = longestGeodeticLine();
        Polygon circle1 = (Polygon) createCircle(pointC.getX(), pointC.getY(), radius);
        LineString lineString1 = this.convertPolygon(circle1);

        Geometry geometry1 = lineString1.intersection(baseLine);

        Point pointOnBase = geometry1.getCentroid();

        double perpendicularsAzimuth = GeodeticLab.getAzimuth(pointC, pointOnBase);
        Point deltaPerPoint = GeodeticLab.solveDirectProblem(pointC, perpendicularsAzimuth, -10);
        Point deltaPointOnBase = GeodeticLab.solveDirectProblem(pointOnBase, perpendicularsAzimuth, 10);
        perpendicular = geometryLab.getLineFromPoints(deltaPerPoint, deltaPointOnBase);

        return perpendicular;
    }

    public void setPerpendicular(LineString perpendicular) {
        this.perpendicular = perpendicular;
    }

    private LineString getBaseLine() throws TransformException, ParseException {
        double deltaLength = longestLine();
        Point deltaA = GeodeticLab.solveDirectProblem(pointA, azimuthAB, -deltaLength);
        Point deltaB = GeodeticLab.solveDirectProblem(pointB, azimuthAB, deltaLength);
        baseLine = geometryLab.getLineFromPoints(deltaA, deltaB);
        return baseLine;
    }

    private double longestLine() throws TransformException {
        double[] distances = new double[3];
        distances[0] = GeodeticLab.getDistance(pointA, pointB);
        distances[1] = GeodeticLab.getDistance(pointA, pointC);
        distances[2] = GeodeticLab.getDistance(pointB, pointC);
        Arrays.sort(distances);
        return distances[2];
    }

    private double longestGeodeticLine(){
        double[] distances = new double[3];
        distances[0] = pointA.distance(pointB);
        distances[1] = pointA.distance(pointC);
        distances[2] = pointB.distance(pointC);
        Arrays.sort(distances);
        return distances[2];
    }



    public void setBaseLine(LineString baseLine) {
        this.baseLine = baseLine;
    }

    private static Geometry createCircle(double x, double y, final double RADIUS){
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(32);
        shapeFactory.setCentre(new Coordinate(x, y));
        shapeFactory.setSize(RADIUS * 2);
        return shapeFactory.createCircle();
    }

    private LineString convertPolygon(Polygon polygon){
        Coordinate[] coordinates = polygon.getCoordinates();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        return geometryFactory.createLineString(coordinates);
    }

}
