package org.geotools;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.geotools.tutorial.model.Interval;
import org.geotools.tutorial.service.ServiceShape;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.operation.TransformException;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class Application{
    private File file;
    private StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private FilterFactory2 filterFactory2 = CommonFactoryFinder.getFilterFactory2();
    ArrayList<com.vividsolutions.jts.geom.Point> points = new ArrayList<com.vividsolutions.jts.geom.Point>();
    ArrayList<Interval> intervals = new ArrayList<Interval>();
    MapContent map = new MapContent();
    DefaultFeatureCollection pointCollection;
    Layer pointLayer;
    SimpleFeatureTypeBuilder pointFeatureTypeBuilder = new SimpleFeatureTypeBuilder();
    SimpleFeatureType pointType = null;



    private enum GeomType{
        POINT,
        LINE,
        POLYGON
    }

    private static final Color LINE_COLOUR = Color.BLUE;
    private static final Color FILL_COLOUR = Color.CYAN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;

    Style pointStyle = SLD.createPointStyle("Circle",Color.BLUE, Color.BLACK, 0.5f, POINT_SIZE);

    private JMapFrame mapFrame;
    private SimpleFeatureSource featureSource;

    private String geometryAttributeName;
    private GeomType geometryType;

    private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();


    public static void main(String[] args) throws Exception {
        Application application = new Application();
        application.displayShapefile();
    }

    private void displayShapefile() throws Exception {
        file = JFileDataStoreChooser.showOpenFile("shp", null);
        if(file == null){
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);

        featureSource = store.getFeatureSource();

        setGeometry();


        map.setTitle("Selection entity");
        Style style = createDefaultStyle();
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
        mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);
        mapFrame.enableLayerTable(true);

        JToolBar toolBar = mapFrame.getToolBar();
        JButton button = new JButton("Choose two points and press Save");
        toolBar.addSeparator();
        toolBar.add(button);
        toolBar.add(new JButton(new ExportShapefileAction("Save result in shp")));

        createPointLayer();
        map.addLayer(pointLayer);

        button.addActionListener(e -> mapFrame.getMapPane().setCursorTool(
                new CursorTool() {
                    @Override
                    public void onMouseClicked(MapMouseEvent ev) {
                        try {
                            selectFeatures(ev);
                        } catch (TransformException e1) {
                            e1.printStackTrace();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }  catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }));

        mapFrame.setSize(800, 800);
        mapFrame.setVisible(true);
    }

    private void createPointLayer() {
       if(pointType == null){
           pointFeatureTypeBuilder.setName("Point");
           pointFeatureTypeBuilder.setCRS(featureSource.getSchema().getCoordinateReferenceSystem());
           pointFeatureTypeBuilder.add("geom", Point.class);
           pointType = pointFeatureTypeBuilder.buildFeatureType();
           pointCollection = new DefaultFeatureCollection(null, pointType);
       }

       pointLayer = new FeatureLayer(pointCollection, pointStyle);
       map.addLayer(pointLayer);
       mapFrame.getMapPane();
    }


    private void selectFeatures(MapMouseEvent ev) throws TransformException, ParseException, IOException {
        DirectPosition2D position2D = ev.getMapPosition();

        com.vividsolutions.jts.geom.Point geoPoint = geometryFactory.createPoint(new Coordinate(position2D.getX(), position2D.getY()));
        points.add(geoPoint);
        //System.out.println("Selected point: " + geoPoint.toString());
        //drawGeoPoint(points);
        pointCollection.add(SimpleFeatureBuilder.build(pointType, new Object[]{geoPoint}, null));

        MapLayerEvent mapLayerEvent = new MapLayerEvent(pointLayer, MapLayerEvent.DATA_CHANGED);
        MapLayerListEvent mapLayerListEvent = new MapLayerListEvent(map, pointLayer, map.layers().indexOf(pointLayer), mapLayerEvent);

        mapFrame.getMapPane().layerChanged(mapLayerListEvent);


//        Point screenPos = ev.getPoint();
//        Rectangle screenRect = new Rectangle(screenPos.x-2, screenPos.y-2, 5, 5);
//
//        AffineTransform screenToWorld = mapFrame.getMapPane().getScreenToWorldTransform();
//        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
//        ReferencedEnvelope bbox = new ReferencedEnvelope(
//                worldRect,
//                mapFrame.getMapContent().getCoordinateReferenceSystem()
//        );
//
//        Filter filter = filterFactory2.intersects(filterFactory2.property(geometryAttributeName), filterFactory2.literal(bbox));
//
//        try{
//            SimpleFeatureCollection selectedFeatures = featureSource.getFeatures(filter);
//            Set<FeatureId> IDs = new HashSet<>();
//            try(SimpleFeatureIterator iterator = selectedFeatures.features()){
//                while(iterator.hasNext()){
//                    SimpleFeature feature = iterator.next();
//                    IDs.add(feature.getIdentifier());
//                }
//            }
//            if(IDs.isEmpty()){
//                System.out.println(" no feature selected");
//            }
//            displaySelectedFeatures(IDs);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private Layer getPointLayer(com.vividsolutions.jts.geom.Point geoPoint){
        final SimpleFeatureType POINT_TYPE = createFeatureTypePoint();

        DefaultFeatureCollection features = new DefaultFeatureCollection("internal", POINT_TYPE);

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(POINT_TYPE);
        featureBuilder.add(geoPoint);
        featureBuilder.add("abc");
        SimpleFeature feature = featureBuilder.buildFeature(null);
        features.add(feature);
        Style style = SLD.createPointStyle("circle", Color.BLUE, Color.BLACK, 0.3f, 15);
        return new FeatureLayer(features, style);
    }

    private SimpleFeatureType createFeatureTypePoint() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");
        builder.setCRS(DefaultGeographicCRS.WGS84);

        builder.add("the_geom", Point.class);
        builder.length(15).add("Name", String.class);
        builder.add("number", Integer.class);

        final SimpleFeatureType LOCATION = builder.buildFeatureType();
        return LOCATION;
    }


//    private void drawGeoPoint(ArrayList<com.vividsolutions.jts.geom.Point> geoPoints) throws SchemaException, IOException {
//
//        final SimpleFeatureType TYPE = DataUtilities.createType("Location",
//                "the_geom:Point:srid=4326," +
//                        "name:String," +
//                        "number:Integer");
//
//        List<SimpleFeature> features = new ArrayList<>();
//        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
//        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);
//
//
//        for(int i = 0; i < geoPoints.size(); i++){
//            String name = "pnt";
//            int number = i;
//            com.vividsolutions.jts.geom.Point point = geoPoints.get(i);
//
//            featureBuilder.add(point);
//            featureBuilder.add(name);
//            featureBuilder.add(number);
//            SimpleFeature feature = featureBuilder.buildFeature(null);
//            featureCollection.add(feature);
//        }
//        Style style = SLD.createPointStyle("Point", Color.BLACK, Color.BLUE, 0.3f, 15);
//        Layer pointLayer = new FeatureLayer(featureCollection, style);
//        pointLayer.setTitle("Choosed Point");
//        map.addLayer(pointLayer);
//        ((FeatureLayer) pointLayer).setStyle(style);
//        pointLayer.setVisible(true);
//        GTRenderer gtRenderer = new StreamingRenderer();
//        JMapPane mapPane = new JMapPane(map);
//        mapFrame.getMapPane().repaint();
//
//
//
//        //        final SimpleFeatureType TYPE = DataUtilities.createType("Flag", "Location:Point, Name:String");
////        //SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
////        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(TYPE);
////
////        builder.add(geoPoint);
////        SimpleFeature feature = builder.buildFeature("fid.1");
////
////        //SimpleFeature feature = featureBuilder.buildFeature("FeaturePoint");
////        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);
////        featureCollection.add(feature);
////        Style style = SLD.createPointStyle("Point", Color.BLACK, Color.BLUE, 0.3f, 15);
////        Layer pointLayer = new FeatureLayer(featureCollection, style);
////        pointLayer.setTitle("Choosed Point");
////        map.addLayer(pointLayer);
////        ((FeatureLayer) pointLayer).setStyle(style);
////        pointLayer.setVisible(true);
////        mapFrame.getMapPane().repaint();
//
//    }

    private void displaySelectedFeatures(Set<FeatureId> IDs) {
        Style style;
        if(IDs.isEmpty()){
            style = createDefaultStyle();
        } else{
            style = createSelectedStyle(IDs);
        }

        Layer layer = mapFrame.getMapContent().layers().get(0);
        ((FeatureLayer) layer).setStyle(style);
        mapFrame.getMapPane().repaint();
    }


    private Style createDefaultStyle() {
        Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);

        FeatureTypeStyle featureTypeStyle = styleFactory.createFeatureTypeStyle();
        featureTypeStyle.rules().add(rule);

        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);
        return style;
    }

    private Style createSelectedStyle(Set<FeatureId> IDs) {
        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR);
        selectedRule.setFilter(filterFactory2.id(IDs));

        Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR);
        otherRule.setElseFilter(true);

        FeatureTypeStyle featureTypeStyle = styleFactory.createFeatureTypeStyle();
        featureTypeStyle.rules().add(selectedRule);
        featureTypeStyle.rules().add(otherRule);

        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);
        return style;
    }

    private Rule createRule(Color lineColour, Color fillColour) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = styleFactory.createStroke(filterFactory2.literal(lineColour),
                filterFactory2.literal(LINE_WIDTH));

        switch (geometryType){
            case POLYGON:
                fill = styleFactory.createFill(filterFactory2.literal(fillColour),
                        filterFactory2.literal(OPACITY));
                symbolizer = styleFactory.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
                break;

            case LINE:
                symbolizer = styleFactory.createLineSymbolizer(stroke, geometryAttributeName);
                break;

            case POINT:
                fill = styleFactory.createFill(filterFactory2.literal(fillColour), filterFactory2.literal(OPACITY));

                Mark mark = styleFactory.getCircleMark();
                mark.setFill(fill);
                mark.setStroke(stroke);

                Graphic graphic = styleFactory.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(filterFactory2.literal(POINT_SIZE));

                symbolizer = styleFactory.createPointSymbolizer(graphic, geometryAttributeName);
        }

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

    private void setGeometry() {
        GeometryDescriptor geometryDescriptor = featureSource.getSchema().getGeometryDescriptor();
        geometryAttributeName = geometryDescriptor.getLocalName();

        Class<?> clazz = geometryDescriptor.getType().getBinding();

        if(Polygon.class.isAssignableFrom(clazz) ||
                MultiPolygon.class.isAssignableFrom(clazz)){
            geometryType = GeomType.POLYGON;
        } else if(LineString.class.isAssignableFrom(clazz) ||
                MultiLineString.class.isAssignableFrom(clazz)){
            geometryType = GeomType.LINE;
        } else{
            geometryType = GeomType.POINT;
        }
    }

    private class ExportShapefileAction extends SafeAction {
        public ExportShapefileAction(String name) {
            super("Save...");
            putValue(Action.SHORT_DESCRIPTION, "Save result in shapefile...");
        }

        @Override
        public void action(ActionEvent actionEvent) throws Throwable {
            exportToShapefile();
        }

        private void exportToShapefile() throws TransformException, ParseException, IOException {

            Map<String, Object> map = new HashMap<>();
            map.put("url", file.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.
                    getFeatureSource(typeName);
            Filter filter = Filter.INCLUDE;

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
            try(FeatureIterator<SimpleFeature> features = collection.features()){
                while(features.hasNext()){
                    SimpleFeature feature = features.next();
                    ServiceShape serviceShape = new ServiceShape(points, feature);
                    intervals = serviceShape.getIntervals();
                }
            }

            final SimpleFeatureType TYPE = createFeatureType();
            List<SimpleFeature> newFeatures = new ArrayList<>();
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

            for(Interval interval : intervals){
                LineString localLine = interval.getLineString();
                featureBuilder.add(interval.getpN());
                featureBuilder.add(interval.getLength());
                featureBuilder.add(localLine);

                SimpleFeature feature = featureBuilder.buildFeature(null);
                newFeatures.add(feature);
            }
            File newFile = getNewShapeFile(file);

            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            Map<String, Serializable> params = new HashMap<>();
            params.put("url", newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);

            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

            newDataStore.createSchema(TYPE);

            Transaction transaction = new DefaultTransaction("create");
            String newTypeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(newTypeName);
            SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

            System.out.println("SHAPE: " + SHAPE_TYPE);

            if(featureSource instanceof SimpleFeatureStore){
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                SimpleFeatureCollection collection1 = new ListFeatureCollection(TYPE, newFeatures);
                featureStore.setTransaction(transaction);
                try{
                    featureStore.addFeatures(collection1);
                    transaction.commit();
                } catch (Exception e){
                    e.printStackTrace();
                    transaction.rollback();
                }finally {
                    transaction.close();
                }
                //System.exit(0);
            } else {
                System.out.println(typeName + " does not support read/write access");
                System.exit(1);
            }

        }

        private File getNewShapeFile(File file) {
            String path = file.getAbsolutePath();
            String newPath = path.substring(0, path.length() - 4) + "_inter.shp";

            JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
            chooser.setDialogTitle("Save shapefile");
            chooser.setSelectedFile(new File(newPath));

            int returnVal = chooser.showSaveDialog(null);
            if(returnVal != JFileDataStoreChooser.APPROVE_OPTION){
                System.exit(0);
            }
            File newFile= chooser.getSelectedFile();
            if(newFile.equals(file)){
                System.out.println("Error: cannot replace " + file);
                System.exit(0);
            }
            return newFile;
        }

        private SimpleFeatureType createFeatureType() {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("Location");
            builder.setCRS(DefaultGeographicCRS.WGS84);


            builder.length(254).add("P-N", String.class);
            builder.add("LENGTH", String.class);
            builder.add("the_geom", LineString.class);

            final SimpleFeatureType LOCATION = builder.buildFeatureType();
            return LOCATION;
        }

    }
}