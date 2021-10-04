package com.ablylabs.pubcrawler.pubs.geo;

import com.ablylabs.pubcrawler.pubs.util.MapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeolocationTree {
    private RectNode root;
    private GeolocationBounds bounds;

    public class RectNode {
        static final int VERTICAL = 0;
        static final int HORIZONTAL = 1;

        GeolocationBounds rect;
        public GeoPoint point;
        RectNode left;
        RectNode right;
        private int orientation; // draw line orienttion 0 for vertical and 1 for horizontal
        private int count;

        private RectNode(GeolocationBounds rect, GeoPoint point, int orientation) {
            this.rect = rect;
            this.point = point;
            this.orientation = orientation;
            if (point != null) {
                count = 1;
            }
        }

        int reverseOrientation() {
            if (orientation == VERTICAL) return HORIZONTAL;
            else return VERTICAL;
        }

        @Override
        public String toString() {
            if (this.point == null) return "";
            return this.point.toString() + "<br/>l: " + this.left.toString() + "r: " + this.right.toString();
        }

    }

    public GeolocationTree()                               // construct an empty set of points
    {
        Geolocation ne = new Geolocation(59.519805, 2.545943);
        Geolocation sw = new Geolocation(49.588884, -12.689713);
        bounds = new GeolocationBounds(sw, ne);
        root = new RectNode(bounds, null, RectNode.VERTICAL);//create unit rect node
    }

    private int size(RectNode node) {
        if (node.point == null) return 0;
        return node.count;
    }


    public void insert(GeoPoint p) {
        if (p == null) throw new IllegalArgumentException("Coordinate point cannot be null");
        if (!contains(p)) {
            root = put(root, p);
        }
    }

    private RectNode put(RectNode rectNode, GeoPoint pointToAdd) {
        if (rectNode.point == null) {//set a point and return but also add empty left and right nodes
            rectNode.point = pointToAdd;
            rectNode.count = 1;
            GeolocationBounds rectLeft = null;
            GeolocationBounds rectRight = null;
            if (rectNode.orientation == RectNode.VERTICAL) {
                rectLeft = new GeolocationBounds(rectNode.rect.southwest, new Geolocation(pointToAdd.latitude(), rectNode.rect.northeast.lng));
                rectRight = new GeolocationBounds(new Geolocation(pointToAdd.latitude(), rectNode.rect.southwest.lng), rectNode.rect.northeast);
            } else if (rectNode.orientation == RectNode.HORIZONTAL) {
                rectLeft = new GeolocationBounds(rectNode.rect.southwest, new Geolocation(rectNode.rect.northeast.lat, pointToAdd.longitude()));
                rectRight = new GeolocationBounds(new Geolocation(rectNode.rect.southwest.lat, pointToAdd.longitude()), rectNode.rect.northeast);
            }
            rectNode.left = new RectNode(rectLeft, null, rectNode.reverseOrientation());
            rectNode.right = new RectNode(rectRight, null, rectNode.reverseOrientation());
            return rectNode;
        }

        if (rectNode.right.rect.contains(pointToAdd.getGeoLocation())) {
            rectNode.right = put(rectNode.right, pointToAdd);
        } else if (rectNode.left.rect.contains(pointToAdd.getGeoLocation())) {
            rectNode.left = put(rectNode.left, pointToAdd);
        }
        rectNode.count = 1 + size(rectNode.left) + size(rectNode.right);
        return rectNode;
    }


    public boolean contains(GeoPoint p)            // does the set contain point p?
    {
        if (p == null) throw new IllegalArgumentException("Coordinate point cannot be null");
        RectNode rectNode = root;
        while (rectNode.point != null && rectNode.rect.contains(p.getGeoLocation())) {
            if (rectNode.point.equals(p)) return true;
            int cmp = 0;
            if (rectNode.orientation == RectNode.VERTICAL) {
                cmp = GeoPoint.X_ORDER.compare(p, rectNode.point);
            } else if (rectNode.orientation == RectNode.HORIZONTAL) {
                cmp = GeoPoint.Y_ORDER.compare(p, rectNode.point);
            }
            if (cmp < 0) rectNode = rectNode.left;
            else if (cmp > 0) rectNode = rectNode.right;
            else rectNode = rectNode.right;//choose one
        }
        return false;
    }

    public Iterable<GeoPoint> nearest(GeoPoint point, int n) {
        if (point == null) throw new IllegalArgumentException("wrong");
        if (root.point == null) return null;
        List<GeoPoint> points = new ArrayList<>(n);
        findNearest(point, root, null, points, n);
        return points;
    }

    private RectNode findNearest(GeoPoint point, RectNode nodeToSearch, RectNode nearestNode, List<GeoPoint> pointsList, int maxSize) {
        if (pointsList.size() < maxSize) {
            pointsList.add(nodeToSearch.point);
        } else {
            final GeoPoint lastElement = pointsList.get(maxSize - 1);
            GeoPoint.DistanceOrder distanceOrder = new GeoPoint.DistanceOrder(point);
            if (distanceOrder.compare(nodeToSearch.point, lastElement) < 0) {
                pointsList.set(maxSize - 1, nodeToSearch.point);
                Collections.sort(pointsList, new GeoPoint.DistanceOrder(point));
            }
        }

        double championDistance;
        if (nearestNode == null) {
            nearestNode = nodeToSearch;
            championDistance = nearestNode.point.distanceSquaredTo(point);
        } else {
            championDistance = nearestNode.point.distanceSquaredTo(point);
            double nodeDistanceSquaredTo = nodeToSearch.point.distanceSquaredTo(point);

            if (Double.compare(nodeDistanceSquaredTo, championDistance) < 0) {
                nearestNode = nodeToSearch;
            }
        }

        double leftRectDistance = MapUtil.distanceToBounds(point.getGeoLocation(), nodeToSearch.left.rect); // nodeToSearch.left.rect.distanceSquaredTo(p);
        double rightRectDistance = MapUtil.distanceToBounds(point.getGeoLocation(), nodeToSearch.right.rect);// nodeToSearch.right.rect.distanceSquaredTo(p);

        //should first check the one on query point
        boolean shouldCheckLeft = Double.compare(leftRectDistance, championDistance) < 0 && nodeToSearch.left.point != null;
        boolean shouldCheckRight = Double.compare(rightRectDistance, championDistance) < 0 && nodeToSearch.right.point != null;
        if (shouldCheckLeft && shouldCheckRight) {
            if (nodeToSearch.left.rect.contains(point.getGeoLocation())) {
                nearestNode = findNearest(point, nodeToSearch.left, nearestNode, pointsList, maxSize);
                nearestNode = findNearest(point, nodeToSearch.right, nearestNode, pointsList, maxSize);
            } else {
                nearestNode = findNearest(point, nodeToSearch.right, nearestNode, pointsList, maxSize);
                nearestNode = findNearest(point, nodeToSearch.left, nearestNode, pointsList, maxSize);
            }

        } else if (shouldCheckLeft) {
            nearestNode = findNearest(point, nodeToSearch.left, nearestNode, pointsList, maxSize);
        } else if (shouldCheckRight) {
            nearestNode = findNearest(point, nodeToSearch.right, nearestNode, pointsList, maxSize);
        }
        return nearestNode;

    }

}
