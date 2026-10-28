package com.ioniq.widget;
public final class WidgetFeedbackTest {
 private static int checks;
 private static void check(String stored,boolean own,boolean other,WidgetFeedback.State expected){if(WidgetFeedback.state(stored,own,other)!=expected)throw new AssertionError("Wrong state for "+stored+" / "+own+" / "+other);checks++;}
 public static void main(String[] args){
  check("",false,false,WidgetFeedback.State.IDLE);
  check("Tap to send",false,false,WidgetFeedback.State.IDLE);
  check("Queued…",true,false,WidgetFeedback.State.QUEUED);
  check("Sending…",true,false,WidgetFeedback.State.SENDING);
  check("Queued…",false,false,WidgetFeedback.State.UNCONFIRMED);
  check("Sending…",false,false,WidgetFeedback.State.UNCONFIRMED);
  check("Confirmed",false,false,WidgetFeedback.State.CONFIRMED);
  check("Unconfirmed",false,false,WidgetFeedback.State.UNCONFIRMED);
  check("Check app",false,false,WidgetFeedback.State.ERROR);
  check("Confirmed",false,true,WidgetFeedback.State.BUSY);
  check("",false,true,WidgetFeedback.State.BUSY);
  check("Sending…",false,true,WidgetFeedback.State.BUSY);
  check("Sending…",true,true,WidgetFeedback.State.SENDING);
  if(!"Last OK".equals(WidgetFeedback.badge(WidgetFeedback.State.CONFIRMED,"Lock")))throw new AssertionError("Success must describe a past command, not locked state");checks++;
  if(!WidgetFeedback.description(WidgetFeedback.State.UNCONFIRMED).contains("before retrying"))throw new AssertionError("Unknown outcome needs a check");checks++;
  if(!WidgetFeedback.description(WidgetFeedback.State.BUSY).contains("No additional command"))throw new AssertionError("Busy does not accept a new command");checks++;
  System.out.println(checks+" widget-feedback checks passed");
 }
}
