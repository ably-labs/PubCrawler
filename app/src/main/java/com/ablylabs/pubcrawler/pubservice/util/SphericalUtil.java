package com.ablylabs.pubcrawler.pubservice.util;



import com.ablylabs.pubcrawler.pubservice.geo.Geolocation;

import java.util.Iterator;
import java.util.List;

public class SphericalUtil {
    private SphericalUtil() {
    }

    public static double computeHeading(Geolocation from, Geolocation to) {
        double fromLat = Math.toRadians(from.lat);
        double fromLng = Math.toRadians(from.lng);
        double toLat = Math.toRadians(to.lat);
        double toLng = Math.toRadians(to.lng);
        double dLng = toLng - fromLng;
        double heading = Math.atan2(Math.sin(dLng) * Math.cos(toLat), Math.cos(fromLat) * Math.sin(toLat) - Math.sin(fromLat) * Math.cos(toLat) * Math.cos(dLng));
        return MathUtil.wrap(Math.toDegrees(heading), -180.0D, 180.0D);
    }

    public static Geolocation computeOffset(Geolocation from, double distance, double heading) {
        distance /= 6371009.0D;
        heading = Math.toRadians(heading);
        double fromLat = Math.toRadians(from.lat);
        double fromLng = Math.toRadians(from.lng);
        double cosDistance = Math.cos(distance);
        double sinDistance = Math.sin(distance);
        double sinFromLat = Math.sin(fromLat);
        double cosFromLat = Math.cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(heading);
        double dLng = Math.atan2(sinDistance * cosFromLat * Math.sin(heading), cosDistance - sinFromLat * sinLat);
        return new Geolocation(Math.toDegrees(Math.asin(sinLat)), Math.toDegrees(fromLng + dLng));
    }

    public static Geolocation computeOffsetOrigin(Geolocation to, double distance, double heading) {
        heading = Math.toRadians(heading);
        distance /= 6371009.0D;
        double n1 = Math.cos(distance);
        double n2 = Math.sin(distance) * Math.cos(heading);
        double n3 = Math.sin(distance) * Math.sin(heading);
        double n4 = Math.sin(Math.toRadians(to.lat));
        double n12 = n1 * n1;
        double discriminant = n2 * n2 * n12 + n12 * n12 - n12 * n4 * n4;
        if (discriminant < 0.0D) {
            return null;
        } else {
            double b = n2 * n4 + Math.sqrt(discriminant);
            b /= n1 * n1 + n2 * n2;
            double a = (n4 - n2 * b) / n1;
            double fromLatRadians = Math.atan2(a, b);
            if (fromLatRadians < -1.5707963267948966D || fromLatRadians > 1.5707963267948966D) {
                b = n2 * n4 - Math.sqrt(discriminant);
                b /= n1 * n1 + n2 * n2;
                fromLatRadians = Math.atan2(a, b);
            }

            if (fromLatRadians >= -1.5707963267948966D && fromLatRadians <= 1.5707963267948966D) {
                double fromLngRadians = Math.toRadians(to.lng) - Math.atan2(n3, n1 * Math.cos(fromLatRadians) - n2 * Math.sin(fromLatRadians));
                return new Geolocation(Math.toDegrees(fromLatRadians), Math.toDegrees(fromLngRadians));
            } else {
                return null;
            }
        }
    }

    public static Geolocation interpolate(Geolocation from, Geolocation to, double fraction) {
        double fromLat = Math.toRadians(from.lat);
        double fromLng = Math.toRadians(from.lng);
        double toLat = Math.toRadians(to.lat);
        double toLng = Math.toRadians(to.lng);
        double cosFromLat = Math.cos(fromLat);
        double cosToLat = Math.cos(toLat);
        double angle = computeAngleBetween(from, to);
        double sinAngle = Math.sin(angle);
        if (sinAngle < 1.0E-6D) {
            return from;
        } else {
            double a = Math.sin((1.0D - fraction) * angle) / sinAngle;
            double b = Math.sin(fraction * angle) / sinAngle;
            double x = a * cosFromLat * Math.cos(fromLng) + b * cosToLat * Math.cos(toLng);
            double y = a * cosFromLat * Math.sin(fromLng) + b * cosToLat * Math.sin(toLng);
            double z = a * Math.sin(fromLat) + b * Math.sin(toLat);
            double lat = Math.atan2(z, Math.sqrt(x * x + y * y));
            double lng = Math.atan2(y, x);
            return new Geolocation(Math.toDegrees(lat), Math.toDegrees(lng));
        }
    }

    private static double distanceRadians(double lat1, double lng1, double lat2, double lng2) {
        return MathUtil.arcHav(MathUtil.havDistance(lat1, lat2, lng1 - lng2));
    }

    static double computeAngleBetween(Geolocation from, Geolocation to) {
        return distanceRadians(Math.toRadians(from.lat), Math.toRadians(from.lng), Math.toRadians(to.lat), Math.toRadians(to.lng));
    }

    public static double computeDistanceBetween(Geolocation from, Geolocation to) {
        return computeAngleBetween(from, to) * 6371009.0D;
    }

    public static double computeLength(List<Geolocation> path) {
        if (path.size() < 2) {
            return 0.0D;
        } else {
            double length = 0.0D;
            Geolocation prev = (Geolocation)path.get(0);
            double prevLat = Math.toRadians(prev.lat);
            double prevLng = Math.toRadians(prev.lng);

            double lng;
            for(Iterator var8 = path.iterator(); var8.hasNext(); prevLng = lng) {
                Geolocation point = (Geolocation)var8.next();
                double lat = Math.toRadians(point.lat);
                lng = Math.toRadians(point.lng);
                length += distanceRadians(prevLat, prevLng, lat, lng);
                prevLat = lat;
            }

            return length * 6371009.0D;
        }
    }

    public static double computeArea(List<Geolocation> path) {
        return Math.abs(computeSignedArea(path));
    }

    public static double computeSignedArea(List<Geolocation> path) {
        return computeSignedArea(path, 6371009.0D);
    }

    static double computeSignedArea(List<Geolocation> path, double radius) {
        int size = path.size();
        if (size < 3) {
            return 0.0D;
        } else {
            double total = 0.0D;
            Geolocation prev = (Geolocation)path.get(size - 1);
            double prevTanLat = Math.tan((1.5707963267948966D - Math.toRadians(prev.lat)) / 2.0D);
            double prevLng = Math.toRadians(prev.lng);

            double lng;
            for(Iterator var11 = path.iterator(); var11.hasNext(); prevLng = lng) {
                Geolocation point = (Geolocation)var11.next();
                double tanLat = Math.tan((1.5707963267948966D - Math.toRadians(point.lat)) / 2.0D);
                lng = Math.toRadians(point.lng);
                total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
                prevTanLat = tanLat;
            }

            return total * radius * radius;
        }
    }

    private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2.0D * Math.atan2(t * Math.sin(deltaLng), 1.0D + t * Math.cos(deltaLng));
    }
}


