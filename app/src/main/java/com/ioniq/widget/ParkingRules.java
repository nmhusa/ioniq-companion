package com.ioniq.widget;

/** No Android dependencies: conservative, single-attempt parking state machine. */
final class ParkingRules {
 enum Event { NONE, COUNTDOWN, LOCK, RETURN, EXPIRED }
 final int radius;
 private final long parkedAt;
 private long outsideSince = -1;
 private boolean attempted, returned;
 ParkingRules(int radius, long now) { this.radius = radius; parkedAt = now; }
 void interrupt() { outsideSince = -1; }
 Event sample(long now, double distance, double uncertainty, boolean usable, double speed) {
  if (now - parkedAt > 24*60*60*1000L) return Event.EXPIRED;
  if (!usable || speed > 2.5 || distance > 500 && !attempted) { interrupt(); return Event.NONE; }
  if (attempted) {
   if (!returned && distance + uncertainty < 60) { returned = true; return Event.RETURN; }
   return Event.NONE;
  }
  // Never send a delayed lock long after parking; session must be armed again.
  if (now - parkedAt > 15*60*1000L) return Event.EXPIRED;
  if (distance - uncertainty < radius) { interrupt(); return Event.NONE; }
  if (outsideSince < 0) { outsideSince = now; return Event.COUNTDOWN; }
  if (now - outsideSince >= 20000) { attempted = true; return Event.LOCK; }
  return Event.COUNTDOWN;
 }
}
