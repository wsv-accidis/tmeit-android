TMEIT for Android
=================

This is a mobile application for members of [TMEIT](http://tmeit.se). TMEIT is a social group
and part of the [Chapter for Information- and Nanotechnology](http://insektionen.se) for
students at The Royal Institute of Technology (KTH).

_Det här är en mobilapp för medlemmar i [TMEIT](http://tmeit.se). TMEIT är en studiesocial
nämnd i [Sektionen för Informations- och Nanoteknik](http://insektionen.se) för studenter
på Kungliga Tekniska Högskolan, KTH._

## Status
Version 1.5 has been published to Google Play. This version adds a simple feature to check if
someone is over 18 and thus of legal age to be served alcohol.

## Development plan
This app is still under development but several features are functional and the app is intended
to be used in its current state.

1. DONE - Show push notifications regarding new activities being published on the TMEIT web site
2. DONE - Show general information for TMEIT members, such as the member list
3. DONE - Handle signups for external events
4. Handle signups for working at TMEIT events
5. Allow members to take and upload photos of each other
6. Handle reporting of TMEIT events for team leaders
7. More stuff.

This app is intended to function as a complement to TMEIT.se but eventually, it could be
a full replacement for those who prefer a smartphone or tablet instead of a computer.

This project is open-source to facilitate porting to other platforms.

## Permissions
This app asks for a few Android permissions. While you can browse the source to find out what they
are used for, here is a brief summary.

* ACCESS_NETWORK_STATE - For checking if we are connected to a network and putting up a warning
  if we're not, since the app won't work without Internet access.
* INTERNET - For accessing the TMEIT web site and services.
* WAKE_LOCK - For (very briefly) keeping the phone awake when a push notification is received.
* WRITE_CONTACTS - For creating a contact in the phone when the user tries to add a contact
  from someone in the list of members, or the member info page.
* WRITE_EXTERNAL_STORAGE - For storing images in external storage when taking a photo.

## Licensing
TMEIT for Android is distributed according to the terms of the **Apache License version 2.0**.

## Acknowledgements
TMEIT for Android uses the following third-party libraries and resources, in addition to Android
SDKs provided by Google:

* [OkHttp HTTP & SPDY client for Android and Java](https://github.com/square/okhttp)
* [Picasso Image downloading and caching for Android](http://square.github.io/picasso/)
* [Material Design Icons by Google](https://github.com/google/material-design-icons)

TMEIT for Android integrates code from the
[image cropper library by SoundCloud](https://github.com/jdamcd/android-crop) (which is based on
[AOSP](https://source.android.com/)).
