package com.ioniq.widget;
public class ModelNameTest {
 public static void main(String[] args){
  String[] inputs={"IONIQ 5","Ioniq5","IONIQ_5","IONIQ-5 N","IONIQ5N","IONIQ 6","ioniq6","IONIQ 6 Limited","IONIQ 50","IONIQ 9","IONIQ9","IONIQ_9","IONIQ 90","KONA","",null};
  int[] expected={5,5,5,5,5,6,6,6,0,9,9,9,0,0,0,0};
  for(int i=0;i<inputs.length;i++)if(ModelName.number(inputs[i],"")!=expected[i])throw new AssertionError("Model mismatch: "+inputs[i]);
  if(ModelName.number("UNKNOWN","IONIQ 5")!=5)throw new AssertionError("Model-name fallback");
  if(!ModelName.title(0).equals("IONIQ Companion"))throw new AssertionError("Unknown must not default to 6");
  System.out.println("Passed 18 model identification checks.");
 }
}
