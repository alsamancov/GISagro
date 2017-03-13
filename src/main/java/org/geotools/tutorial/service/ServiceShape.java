package org.geotools.tutorial.service;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.tutorial.model.Interval;
import org.geotools.tutorial.util.GeodeticLab;
import org.geotools.tutorial.util.GeometryLab;
import org.geotools.tutorial.util.Perpedicular;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;


public class ServiceShape {
    private ArrayList<Point> points = new ArrayList<Point>();
    private ArrayList<Interval> intervals = new ArrayList<>();
    private SimpleFeature feature;

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }



    public ServiceShape(ArrayList<Point> points, SimpleFeature feature) {
        this.points = points;
        this.feature = feature;
    }

    public ArrayList<Interval> getIntervals() throws TransformException, ParseException, IOException {
        Point p1 = points.get(0);
        Point p2 = points.get(1);


        Perpedicular perpedicular = new Perpedicular(p1, p2);
        Geometry line = perpedicular.getPerpendicular();


        double azimuthP2P1 = GeodeticLab.getAzimuth(p2, p1);


        MultiLineString curve = this.getLineString(feature);


        GeometryLab geometryLab = new GeometryLab();
        ArrayList<LineString> lineStrings = new ArrayList<LineString>();

        double shift = 2.0;
        for(;;){
            LineString parallel = geometryLab.offset((LineString)line, shift, azimuthP2P1);
            lineStrings.add(parallel);
            shift += 2.0;
            if(!parallel.intersects(curve)){
                break;
            }
        }
        ArrayList<Interval> lines = new ArrayList<Interval>();

        for(LineString lineString : lineStrings){
            Geometry geometry = lineString.intersection(curve);
            int j = 1;
            for(int i = 1; i < geometry.getNumGeometries() - 1; i += 2){
                Interval interval = new Interval();
                Point currentPoint = (Point) geometry.getGeometryN(i);
                Point nextPoint = (Point) geometry.getGeometryN(i + 1);
                LineString pline = geometryLab.getLineFromPoints(currentPoint, nextPoint);
                interval.setpN("P" + j);
                j++;
                double length = GeodeticLab.getDistance(currentPoint, nextPoint);
                double result = Math.rint(100.0 * length) / 100.0;
                interval.setLength(result);
                interval.setLineString(pline);
                intervals.add(interval);
            }
        }
        return intervals;
    }

    private MultiLineString getLineString(SimpleFeature feature) throws IOException {
        MultiLineString lineString = (MultiLineString)feature.getDefaultGeometryProperty().getValue();
        return lineString;
    }




}
