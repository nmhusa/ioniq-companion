package com.ioniq.widget;
public class MileageReadingTest {
 private static int checks;
 private static void check(boolean ok){checks++;if(!ok)throw new AssertionError("Mileage check "+checks+" failed");}
 public static void main(String[] args){
  MileageReading m=MileageReading.parse("17332",3,"20251228094801");check(m!=null&&m.value==17332&&m.unit==3);
  check(MileageReading.parse("0",3,"").value==0);
  check(MileageReading.parse(" 42.5 ",1,"").value==42.5);
  for(String bad:new String[]{null,"","null","NaN","Infinity","-1","unknown"})check(MileageReading.parse(bad,3,"")==null);
  check(MileageReading.parse("42",99,"")==null);
  check(MileageReading.reportLabel("20251228094801").equals("2025-12-28 09:48:01 (Hyundai time)"));
  check(MileageReading.reportLabel("").equals("Update time unavailable"));
  check(MileageReading.reportLabel("2026-10-28T12:00:00Z").equals("2026-10-28T12:00:00Z"));
  System.out.println("Passed "+checks+" mileage checks.");
 }
}
