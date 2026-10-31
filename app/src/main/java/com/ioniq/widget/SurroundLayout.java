package com.ioniq.widget;
/** US composite format documented by Hyundai-Kia-Connect svm_image.py. */
final class SurroundLayout {
 // UI order: overview, front, rear, left, right. Coordinates remain in source pixels.
 static int[] rectangle(int[] sizes,int width,int height,int view){
  if(sizes==null||sizes.length<5||view<0||view>4||width<=0||height<=0)return null;
  int camera=sizes[2],top=sizes[4];
  if(camera<=0||top<=0||sizes[0]!=width||sizes[1]!=height||4L*camera+top!=width)return null;
  return new int[]{view==0?4*camera:(view-1)*camera,0,view==0?top:camera,height};
 }
}
