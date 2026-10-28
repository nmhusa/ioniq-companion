package com.ioniq.widget;
final class GeoPoint {
 final double latitude,longitude;
 GeoPoint(double lat,double lon){if(!Double.isFinite(lat)||!Double.isFinite(lon)||lat < -90||lat>90||lon < -180||lon>180)throw new IllegalArgumentException("Invalid location coordinates");latitude=lat;longitude=lon;}
 static GeoPoint parse(String lat,String lon){return new GeoPoint(Double.parseDouble(lat),Double.parseDouble(lon));}
 String coordinates(){return Double.toString(latitude)+","+Double.toString(longitude);}
 String mapUrl(){return "https://www.google.com/maps/search/?api=1&query="+coordinates();}
 String walkUrl(){return "https://www.google.com/maps/dir/?api=1&destination="+coordinates()+"&travelmode=walking";}
}
