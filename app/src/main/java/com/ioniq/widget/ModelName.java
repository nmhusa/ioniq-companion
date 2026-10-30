package com.ioniq.widget;
final class ModelName {
 static int number(String code,String name){
  for(String raw:new String[]{code,name}){
   if(raw==null)continue;
   java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?i)\\bIONIQ\\s*([569])(?:\\b|(?=N\\b))").matcher(raw.replace('_',' ').replace('-',' '));
   if(m.find())return Integer.parseInt(m.group(1));
  }
  return 0;
 }
 static String title(int number){return number==5?"IONIQ 5":number==6?"IONIQ 6":number==9?"IONIQ 9":"IONIQ Companion";}
}
