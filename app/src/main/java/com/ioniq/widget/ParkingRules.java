package com.ioniq.widget;

/** No Android dependencies: conservative, single-attempt parking state machine. */
final class ParkingRules {
 enum Event { NONE, COUNTDOWN, LOCK, RETURN, EXPIRED }
 final int radius;
 private final long parkedAt;
 private long outsideSince = -1;
 private boolean attempted, returned;
 ParkingRules(int radius, long now) { this.radius = radius; parkedAt = now; }
 long countdownMillis(){return radius<=30?5000:20000;}
 int returnRadius(){return radius>=100?60:radius/2;}
 void interrupt() { outsideSince = -1; }
 Event sample(long now, double distance, double uncertainty, boolean usable, double speed) {
  if (now - parkedAt > 24*60*60*1000L) return Event.EXPIRED;
  if (!usable || speed > 2.5 || distance > 500 && !attempted) { interrupt(); return Event.NONE; }
  if (attempted) {
   if (!returned && distance + uncertainty < returnRadius()) { returned = true; return Event.RETURN; }
   return Event.NONE;
  }
  // Never send a delayed lock long after parking; session must be armed again.
  if (now - parkedAt > 15*60*1000L) return Event.EXPIRED;
  if (distance - uncertainty < radius) { interrupt(); return Event.NONE; }
  if (outsideSince < 0) { outsideSince = now; return Event.COUNTDOWN; }
  if (now - outsideSince >= countdownMillis()) { attempted = true; return Event.LOCK; }
  return Event.COUNTDOWN;
 }
}
