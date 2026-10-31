# 0.9.7 — surround-view test build

View surroundings is beside the location controls. Load latest snapshot reads Hyundai's saved image without requesting a fresh capture. Request new snapshot sends one capture request after confirmation, then checks for a changed capture timestamp for up to three minutes. Requests are rate-limited locally and are never automatically resent. Keep the app open while waiting; the viewer can be left and reopened. Android may interrupt work if the app is backgrounded or killed; use Load latest afterward.

Five views (Overview, Front, Rear, Left, Right) are cropped only when image metadata matches the documented US composite layout. Otherwise the full image is displayed without camera labels. Side/front/rear views retain their original fisheye appearance; no synthetic stitching or dewarping is performed. Swipe to switch and tap to enlarge. Capture time is displayed as returned by Hyundai; door/trunk status is explicitly at capture time. No map/address is shown. Images remain in process memory and are not saved to disk. Changing the selected account/vehicle clears the viewer on next access.

Requires a car/account with Hyundai remote surround-view support. Live retrieval has not been tested with the owner's vehicle. Begin with Load latest and compare labels and capture time with MyHyundai before requesting a fresh image. This is not a live camera or a driving safety aid.

Build with the existing signing key. Output: release-output/0.9.7/ioniq-companion-0.9.7.apk. No watch APK update is required.

Protocol layout reference: https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api/blob/master/hyundai_kia_connect_api/svm_image.py
