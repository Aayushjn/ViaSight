# ViaSight

ViaSight is an accessibility Android launcher meant for visually 
challenged individuals. Its purpose is to allow such individuals to use 
their smartphones in a similar way to others by employing 
_sensory substitution_.

Sensory substitution is done here by providing different vibration 
patterns. Each pattern holds a different purpose. It helps train the 
user to rely on haptic feedback during certain events. These events (in 
case of the app) are incoming notifications, positive TTS responses and 
negative TTS responses. 

It has support for swipe gestures:
* Swipe up to enable voice commands (as mentioned below)
* Swipe down once to read notifications
* Swipe down again to stop reading notifications

ViaSight currently supports voice commands like the following:
* **_open_** <app_name>
* **_what_** is the _date_/_time_/_battery percentage_?
* **_call_** <contact_name>
* **_remove_** _notifications_
* **_play_** _tutorial_
* **_set_** _volume_ _silent_/_vibrate_/_normal_

#### Permissions required:
* Vibrate
* Record Audio
* Call phone
* Read contacts