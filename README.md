TMEIT for Android
=================

This is a mobile application for members of [TMEIT](http://tmeit.se). TMEIT is a social group
and part of the [Chapter for Information- and Nanotechnology](http://insektionen.se) for
students at The Royal Institute of Technology (KTH).

_Det här är en mobilapp för medlemmar i [TMEIT](http://tmeit.se). TMEIT är en studiesocial
nämnd i [Sektionen för Informations- och Nanoteknik](http://insektionen.se) för studenter
på Kungliga Tekniska Högskolan, KTH._

## Status
Version 2.1 has been published to Google Play. This version changed the filters for the members
list.

## Development plan
This app is still under development but several features are functional and the app is intended
to be used in its current state. It is intended to function as a complement to TMEIT.se but once
enough features are implemented it could replace the website completely for many members.

See the project [Issues](https://github.com/wsv-accidis/tmeit-android/issues) for the current
development backlog. This includes bugs as well as enhancements and upcoming features.

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

## Licensing
TMEIT for Android is distributed according to the terms of the **Apache License version 2.0**.

## Acknowledgements
TMEIT for Android uses the following third-party libraries and resources, in addition to Android
SDKs provided by Google:

* [OkHttp3 HTTP & SPDY client for Android and Java](https://github.com/square/okhttp)
* [Picasso Image downloading and caching for Android](https://github.com/square/picasso)
* [OkHttp3 Downloader for Picasso](https://github.com/JakeWharton/picasso2-okhttp3-downloader)
* [Android Range Seek Bar](https://github.com/anothem/android-range-seek-bar)
* [Material Design Icons by Google](https://github.com/google/material-design-icons)

TMEIT for Android integrates code from the
[Image cropping library by SoundCloud](https://github.com/jdamcd/android-crop) (which is based on
[Android CropImage](https://github.com/lvillani/android-cropimage), which is based on
[AOSP](https://source.android.com/)).
