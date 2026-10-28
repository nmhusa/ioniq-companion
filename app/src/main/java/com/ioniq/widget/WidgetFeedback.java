package com.ioniq.widget;
/** Presentation only: never interprets a command acknowledgement as live car state. */
final class WidgetFeedback {
 enum State { IDLE, QUEUED, SENDING, CONFIRMED, UNCONFIRMED, ERROR, BUSY }
 static boolean pending(String stored){return "Queued…".equals(stored)||"Sending…".equals(stored);}
 static State state(String stored,boolean thisActive,boolean anotherActive){
  if(thisActive)return "Queued…".equals(stored)?State.QUEUED:State.SENDING;
  if(anotherActive)return State.BUSY;
  if(pending(stored)||"Unconfirmed".equals(stored))return State.UNCONFIRMED;
  if("Confirmed".equals(stored))return State.CONFIRMED;
  if("Check app".equals(stored))return State.ERROR;
  return State.IDLE;
 }
 static String badge(State state,String idle){switch(state){
  case QUEUED:return "Queued";case SENDING:return "Sending…";case CONFIRMED:return "Last OK";
  case UNCONFIRMED:return "Unknown";case ERROR:return "Check app";case BUSY:return "Busy";default:return idle;
 }}
 static String description(State state){switch(state){
  case QUEUED:return "Waiting for the app’s current request to finish.";
  case SENDING:return "Sending command / waiting for Hyundai confirmation.";
  case CONFIRMED:return "Hyundai confirmed the last command. This is not a live vehicle-state reading.";
  case UNCONFIRMED:return "Last command outcome is unknown. Check the official app before retrying.";
  case ERROR:return "Last request needs attention. Read the result before retrying.";
  case BUSY:return "Another widget command is in progress. No additional command will be sent.";
  default:return "Tap the icon to send this command.";
 }}
}
