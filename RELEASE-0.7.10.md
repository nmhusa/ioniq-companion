# IONIQ Companion 0.7.10 — widget command feedback

- Small action widgets retain their raised icons and now show a compact status badge.
- Amber Queued/Sending means the command is in progress; green Last OK means Hyundai confirmed the last widget command. Unknown/Check app means review the result. Other widgets dim and show Busy while one command runs.
- Tap the icon to send; tap its status badge to open the full per-command result and timestamp without sending another command.
- Repeated taps during an active widget command do not send additional requests.
- Interrupted pending feedback becomes Unknown when widgets next repaint. A local-only recovery job requests a repaint after a pending command; Android scheduling can delay it. No vehicle polling or automatic command retry is added.

These are command outcomes, not live door/climate/charging sensor readings. Small-cell readability, Samsung launcher feedback and notification behavior need device testing. Use the same permanent release key to install over the signed app. Public redistribution authorization remains unresolved.
