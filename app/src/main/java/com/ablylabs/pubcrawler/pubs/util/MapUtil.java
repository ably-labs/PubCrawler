package com.ablylabs.pubcrawler.pubs.util;


import com.ablylabs.pubcrawler.pubs.geo.Geolocation;
import com.ablylabs.pubcrawler.pubs.geo.GeolocationBounds;

public class MapUtil {

    public static final int DIRECTION_N =0;
    public static final int DIRECTION_NE =1;
    public static final int DIRECTION_SE =2;
    public static final int DIRECTION_S =3;
    public static final int DIRECTION_SW =4;
    public static final int DIRECTION_W =5;
    public static final int DIRECTION_NW =6;
    public static final int DIRECTION_E =7;

    public static int direction(Geolocation from, Geolocation to) {
        double lat1 = from.lat;
        double lng1 = from.lng;
        double lat2 = to.lat;
        double lng2 = to.lng;
        double dLon = (lng2 - lng1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double bearing = Math.toDegrees((Math.atan2(y, x)));
        bearing = (360 - ((bearing + 360) % 360));
        return bearingToDirection(bearing);
    }

    private static int bearingToDirection(double bearing) {
        if (bearing == 0){
            return DIRECTION_N;
        }else if(bearing<90){
            return DIRECTION_NE;
        }else if(bearing == 90){
            return DIRECTION_E;
        }else if(bearing>90 && bearing <180){
            return  DIRECTION_SE;
        }else if(bearing == 180){
            return DIRECTION_S;
        }else if(bearing >180 && bearing < 270){
            return DIRECTION_SW;
        }else if(bearing == 270){
            return DIRECTION_W;
        }else if(bearing>270){
            return DIRECTION_NW;
        }
        return DIRECTION_N;
    }

    public static int directionAlternative(Geolocation latlng1, Geolocation latlng2) {
        double delta = 22.5;
        int direction = DIRECTION_N;
        double heading = SphericalUtil.computeHeading(latlng1, latlng2);

        if ((heading >= 0 && heading < delta) || (heading < 0 && heading >= -delta)) {
            direction = DIRECTION_N;
        } else if (heading >= delta && heading < 90 - delta) {
            direction = DIRECTION_NE;
        } else if (heading >= 90 - delta && heading < 90 + delta) {
            direction = DIRECTION_E;
        } else if (heading >= 90 + delta && heading < 180 - delta) {
            direction = DIRECTION_SE;
        } else if (heading >= 180 - delta || heading <= -180 + delta) {
            direction = DIRECTION_S;
        } else if (heading >= -180 + delta && heading < -90 - delta) {
            direction = DIRECTION_SW;
        } else if (heading >= -90 - delta && heading < -90 + delta) {
            direction = DIRECTION_W;
        } else if (heading >= -90 + delta && heading < -delta) {
            direction = DIRECTION_NW;
        }

        return direction;
    }

    public static double distanceToBounds(Geolocation latLng, GeolocationBounds bounds){
        double dx = 0.0, dy = 0.0;
        if(latLng.lat < bounds.southwest.lat) dx = latLng.lat - bounds.southwest.lat;
        else if (latLng.lat > bounds.northeast.lat) dx = latLng.lat -bounds.northeast.lat;
        if (latLng.lng < bounds.southwest.lng) dy = latLng.lng - bounds.southwest.lng;
        else if (latLng.lng > bounds.northeast.lng) dy = latLng.lng - bounds.northeast.lng;
        return dx*dx + dy*dy;

    }

    public static boolean boundsIntersects(GeolocationBounds bounds1, GeolocationBounds bounds2){
        final Geolocation sw1 = bounds1.southwest;
        final Geolocation ne1 = bounds1.northeast;

        final Geolocation sw2 = bounds2.southwest;
        final Geolocation ne2 = bounds2.northeast;

        boolean latIntersects = ne2.lat >= sw1.lat && sw2.lat <= ne1.lat;
        boolean lngIntersects = ne2.lng >= sw1.lng && sw2.lng <= ne1.lng;
        return latIntersects && lngIntersects;
    }

    public static GeolocationBounds toBounds(Geolocation center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        Geolocation southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        Geolocation northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new GeolocationBounds(southwestCorner, northeastCorner);
    }


    public static long metersInMiles(float mile){
        return (long) (mile * 1609.344f);
    }



    public static double distanceBetweenTwoPointsInMiles(Geolocation first, Geolocation second) {
        if (first == null || second == null) return 0;
        double distanceInMeters = SphericalUtil.computeDistanceBetween(first, second);
        return distanceInMeters / 1609.344;//convert meters to miles
    }

    public static double distanceBetweenTwoPointsInMeters(Geolocation first, Geolocation second) {
        if (first == null || second == null) return 0;
        return SphericalUtil.computeDistanceBetween(first, second);
    }

    public static String formattedMiles(int meters) {
        double totalMiles = (double) meters / 1609.344;
        if (totalMiles > 99) {
            return String.format("%dmi", Math.round(totalMiles));
        }
        return String.format("%.1fmi", totalMiles);
    }
    public static double mileInMeters(long meters){
        return (double) meters / 1609.344;
    }

    public static String formattedMilesWithEstimation(int meters) {
        double totalMiles = (double) meters / 1609.344;
        return String.format("%.1fmi(est)", totalMiles);
    }

    public static boolean isInLondon(Geolocation point) {
        Geolocation londonCentrePoint = new Geolocation(51.4412945, -0.4395152);
        double distance = distanceBetweenTwoPointsInMiles(londonCentrePoint, point);
        //at least 18 miles
        if (distance < 18) {
            return true;
        }
        return false;
    }

    public static String formattedKilometers(int distanceInMeters) {
        double totalKilometers = (double) distanceInMeters / 1000;

        if (totalKilometers > 99) {
            return String.format("%dkm", Math.round(totalKilometers));
        }
        return String.format("%.1fkm", totalKilometers);
    }
}
