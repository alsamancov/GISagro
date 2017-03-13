package org.geotools.tutorial.util;

import com.vividsolutions.jts.geom.*;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.referencing.operation.TransformException;

import java.text.ParseException;

/**
 * Created by Alexey on 03/10/2017.
 */
public class GeometryLab {
    private GeometryFactory geometryFactory;
    public GeometryLab() {
        this.geometryFactory = JTSFactoryFinder.getGeometryFactory();
    }


    public LineString getLineFromPoints(Point point1, Point point2){
        Coordinate[] coordinates = new Coordinate[]{point1.getCoordinate(), point2.getCoordinate()};
        LineString lineString = geometryFactory.createLineString(coordinates);
        return lineString;
    }

    public LineString getLineFromCoordinates(Coordinate[] coordinates){
        LineString lineString = geometryFactory.createLineString(coordinates);
        return lineString;
    }



    public  LineString offset(LineString line, double distance, double azimuth) throws TransformException, ParseException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Point startPoint = line.getStartPoint();
        Point endPoint = line.getEndPoint();
        Point newStartPoint = GeodeticLab.solveDirectProblem(startPoint, azimuth, distance);
        Point newEndPoint = GeodeticLab.solveDirectProblem(endPoint, azimuth, distance);
        LineString offset = geometryFactory.createLineString(new Coordinate[]{new Coordinate(newStartPoint.getX(), newStartPoint.getY()), new Coordinate(newEndPoint.getX(), newEndPoint.getY())});
        return offset;
    }
}
