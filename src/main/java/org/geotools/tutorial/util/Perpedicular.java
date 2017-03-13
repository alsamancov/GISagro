package org.geotools.tutorial.util;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.referencing.operation.TransformException;

import java.text.ParseException;


public class Perpedicular {
    private LineString perpendicular;
    private LineString baseLine;
    private Point startPoint;
    private Point endPoint;
    private Point deltaEndPoint;
    private double azimuth;
    private double distance;


    public Perpedicular() {
    }

    public Perpedicular(Point startPoint, Point endPoint) throws TransformException, ParseException {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.azimuth = GeodeticLab.getAzimuth(startPoint, endPoint);
        this.distance = GeodeticLab.getDistance(startPoint, endPoint);
        this.deltaEndPoint = GeodeticLab.solveDirectProblem(startPoint, azimuth, (distance * 2));
    }

    public LineString getPerpendicular() throws TransformException, ParseException {
        double radius = startPoint.distance(deltaEndPoint);
        Polygon circle1 = (Polygon) createCircle(startPoint.getX(), startPoint.getY(), (radius/1.9));
        Polygon circle2 = (Polygon) createCircle(deltaEndPoint.getX(), deltaEndPoint.getY(), (radius/1.9));
        LineString lineString1 = this.convertPolygon(circle1);
        LineString lineString2 = this.convertPolygon(circle2);
        Geometry tempGeometry = lineString1.intersection(lineString2);
        Coordinate[] points = tempGeometry.getCoordinates();
        GeometryLab geometryLab = new GeometryLab();
        LineString resultLine = geometryLab.getLineFromCoordinates(points);
        Point perpendicularStartPoint = resultLine.getStartPoint();
        LineString newBaseLine = geometryLab.getLineFromPoints(startPoint, deltaEndPoint);
        Geometry intersect = resultLine.intersection(newBaseLine);
        double perpendicularsAzimuth = GeodeticLab.getAzimuth(perpendicularStartPoint, (Point) intersect);
        double perpendicularsDistance = GeodeticLab.getDistance(perpendicularStartPoint, (Point) intersect);
        Point newIntersect = GeodeticLab.solveDirectProblem(perpendicularStartPoint, perpendicularsAzimuth, perpendicularsDistance + 5);
        LineString newResult = geometryLab.getLineFromPoints(perpendicularStartPoint, newIntersect);
        return newResult;
    }

    public void setPerpendicular(LineString perpendicular) {
        this.perpendicular = perpendicular;
    }

    public LineString getBaseLine() {
        return baseLine;
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
