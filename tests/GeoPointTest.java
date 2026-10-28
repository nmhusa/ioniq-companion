package com.ioniq.widget;
public final class GeoPointTest {
 public static void main(String[] args){int checks=0;
  for(double[] xy:new double[][]{{0,0},{44.44,-73.1},{-90,-180},{90,180}}){GeoPoint p=GeoPoint.parse(Double.toString(xy[0]),Double.toString(xy[1]));if(p.latitude!=xy[0]||p.longitude!=xy[1])throw new AssertionError("Coordinate order");checks++;}
  for(String[] xy:new String[][]{{"NaN","0"},{"0","Infinity"},{"91","0"},{"0","181"},{"-91","0"},{"0","-181"},{"","0"},{"null","0"},{"0","1&query=other"}}){try{GeoPoint.parse(xy[0],xy[1]);throw new AssertionError("Invalid location accepted");}catch(IllegalArgumentException expected){}checks++;}
  GeoPoint p=new GeoPoint(44.44,-73.1);if(!p.mapUrl().equals("https://www.google.com/maps/search/?api=1&query=44.44,-73.1"))throw new AssertionError("Wrong map destination");checks++;
  if(!p.walkUrl().equals("https://www.google.com/maps/dir/?api=1&destination=44.44,-73.1&travelmode=walking"))throw new AssertionError("Wrong walking destination/mode");checks++;
  System.out.println(checks+" location validation checks passed");
 }
}
