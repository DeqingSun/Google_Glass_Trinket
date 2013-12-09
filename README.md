Google_Glass_Trinket
====================

Using micro-usb port on Google Glass to connect hardware

This uses USB OTG on Google Glass. In XE11 GDK does not have permission to use USB host by default.  

\<feature name="android.hardware.usb.host"/\>     

needs to be added in /etc/permissions/hardware.xml 

Reference: http://stackoverflow.com/questions/11183792/android-usb-host-and-hidden-devices
