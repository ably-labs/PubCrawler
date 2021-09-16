package com.ablylabs.pubcrawler.pubservice.geo;


public class GeolocationBounds {
    public Geolocation southwest;
    public Geolocation northeast;

    public GeolocationBounds(Geolocation southwest, Geolocation northeast) {

        this.southwest = southwest;
        this.northeast = northeast;
    }
    /**from Google's GeolocationBounds clas **/
    public final boolean contains(Geolocation point) {
        double pointLat = point.lat;
        return this.southwest.lat <= pointLat && pointLat <= this.northeast.lat && this.zza(point.lng);
    }


    //try to find a meaningful name for this later
    private final boolean zza(double var1) {
        if (this.southwest.lng <= this.northeast.lng) {
            return this.southwest.lng <= var1 && var1 <= this.northeast.lng;
        } else {
            return this.southwest.lng <= var1 || var1 <= this.northeast.lng;
        }
    }

}
