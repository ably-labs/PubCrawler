package com.ablylabs.pubcrawler.pubservice.geo;

import com.ablylabs.pubcrawler.pubservice.util.MapUtil;

import java.util.Comparator;

//this is especially for KD tree
public class GeoPoint implements Comparable<GeoPoint> {
    private final Geolocation geoLocation;
    private final LocationItem associatedLocation;

    public static final Comparator<GeoPoint> X_ORDER = new XOrder();
    public static final Comparator<GeoPoint> Y_ORDER = new YOrder();

    public GeoPoint(Geolocation geoLocation, LocationItem associatedLocation) {
        this.geoLocation = geoLocation;
        this.associatedLocation = associatedLocation;
    }
    public static GeoPoint from(Geolocation geoLocation, LocationItem locationItem){
        return new GeoPoint(geoLocation, locationItem);
    }

    public double latitude() {
        return geoLocation.lat;
    }

    public double longitude() {
        return geoLocation.lng;
    }

    public Geolocation getGeoLocation() {
        return geoLocation;
    }

    public LocationItem getAssociatedLocation() {
        return associatedLocation;
    }



    @Override
    public String toString() {
        return geoLocation.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof GeoPoint)) {
            return false;
        }
        GeoPoint that = (GeoPoint) obj;
        if (that.associatedLocation != null && this.associatedLocation != null) {
            return this.associatedLocation.equals(that.associatedLocation);
        }
        if (Double.compare(this.latitude(), that.latitude()) == 0
                && Double.compare(this.longitude(), that.longitude()) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(GeoPoint that) {
        final int direction = MapUtil.direction(this.geoLocation, that.geoLocation);
        if (direction == MapUtil.DIRECTION_N || direction == MapUtil.DIRECTION_NE || direction == MapUtil.DIRECTION_NW
                || direction == MapUtil.DIRECTION_E)
            return -1;
        if (direction == MapUtil.DIRECTION_S || direction == MapUtil.DIRECTION_SE || direction == MapUtil.DIRECTION_SW
                || direction == MapUtil.DIRECTION_W)
            return +1;

        return 0;
    }

    public double distanceSquaredTo(GeoPoint point) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(point.latitude() - this.latitude());
        double lonDistance = Math.toRadians(point.longitude() - this.longitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(this.latitude()))
                * Math.cos(Math.toRadians(point.latitude())) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return Math.pow(distance, 2);

    }

    static class DistanceOrder implements Comparator<GeoPoint>{
        private GeoPoint pointToMeasureDistanceTo;//this is the point to measure distance to

        public DistanceOrder(GeoPoint pointToMeasureDistanceTo) {
            this.pointToMeasureDistanceTo = pointToMeasureDistanceTo;
        }


        @Override
        public int compare(GeoPoint p1, GeoPoint p2) {
            final double firstDistance = MapUtil.distanceBetweenTwoPointsInMeters(p1.geoLocation, this.pointToMeasureDistanceTo.geoLocation);
            final double secondDistance = MapUtil.distanceBetweenTwoPointsInMeters(p2.geoLocation, this.pointToMeasureDistanceTo.geoLocation);
            return Double.compare(firstDistance,secondDistance);
        }
    }
    private static class XOrder implements Comparator<GeoPoint> {
        public int compare(GeoPoint p, GeoPoint q) {
            // final int direction = MapUtil.direction(p.geoLocation, q.geoLocation);
            final int direction = MapUtil.directionAlternative(p.geoLocation, q.geoLocation);

            if (direction == MapUtil.DIRECTION_E || direction == MapUtil.DIRECTION_SE
                    || direction == MapUtil.DIRECTION_NE) {// to east
                return -1;
            } else if (direction == MapUtil.DIRECTION_W || direction == MapUtil.DIRECTION_SW
                    || direction == MapUtil.DIRECTION_NW) {
                return +1;
            }
            return 0;
        }
    }

    private static class YOrder implements Comparator<GeoPoint> {
        public int compare(GeoPoint p, GeoPoint q) {
            // final int direction = MapUtil.direction(p.geoLocation, q.geoLocation);
            final int direction = MapUtil.directionAlternative(p.geoLocation, q.geoLocation);

            if (direction == MapUtil.DIRECTION_N || direction == MapUtil.DIRECTION_NE
                    || direction == MapUtil.DIRECTION_NW) {// to north
                return -1;
            } else if (direction == MapUtil.DIRECTION_S || direction == MapUtil.DIRECTION_SE
                    || direction == MapUtil.DIRECTION_SW) {
                return +1;
            }
            return 0;
        }
    }
}

