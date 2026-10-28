package com.ioniq.widget;
// Hyundai US heating1 enum, documented in Hacksore/bluelinky PR #294.
final class ClimateHeatMode {
 static int encode(boolean rear,boolean wheel){return rear?(wheel?1:2):(wheel?3:0);}
}
