package org.geotools.tutorial.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.operation.TransformException;

import java.awt.geom.Point2D;


/**
 * Created by Alexey on 03/10/2017.
 */
public class GeodeticLab {

    public static double getDistance(Point point1, Point point2) throws TransformException {
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        geodeticCalculator.setStartingGeographicPoint(point1.getX(), point1.getY());
        geodeticCalculator.setDestinationGeographicPoint(point2.getX(), point2.getY());

        return geodeticCalculator.getOrthodromicDistance();
    }


    public static double getAzimuth(Point point1, Point point2) throws TransformException {
        Coordinate coordinate1 = new Coordinate(point1.getX(), point1.getY());
        Coordinate coordinate2 = new Coordinate(point2.getX(), point2.getY());

        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        geodeticCalculator.setStartingPosition(JTS.toDirectPosition(coordinate1, DefaultGeographicCRS.WGS84));
        geodeticCalculator.setDestinationPosition(JTS.toDirectPosition(coordinate2, DefaultGeographicCRS.WGS84));

        return geodeticCalculator.getAzimuth();
    }

    public static Point solveDirectProblem(Point startPosition, double angle, double distance) throws java.text.ParseException {
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        geodeticCalculator.setStartingGeographicPoint(startPosition.getX(), startPosition.getY());
        geodeticCalculator.setDirection(angle, distance);
        Point2D point2D = geodeticCalculator.getDestinationGeographicPoint();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();


        return geometryFactory.createPoint(new Coordinate(point2D.getX(), point2D.getY()));
    }
}
