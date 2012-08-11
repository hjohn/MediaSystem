MediaSystem
===========

Front-end application for video libraries.

Features
--------
* Select video to view from a nicely styled list
* Groups videos by series and season
* Downloads meta information about videos from TMDB (www.themoviedb.org) and TVDB (thetvdb.com)
* Shows plot summaries, ratings and actor photos for known videos
* Keeps track of watched videos
* Resumes playback from last position
* Capable of downloading subtitles from Sublight (www.sublight.si) and OpenSubtitles.org (www.opensubtitles.org)
* Supports plugins for different styled views, video playback, subtitle providers and new video collections

Technical Features
------------------
* Uses OSGi for plugins
* Supports PostgreSQL as a database back-end, falling back to Apache Derby when left unconfigured

Requirements
------------
* Java Runtime Environment 1.7+ installed
* JavaFX 2.2 installed
* VLC player 2.0+ installed

Note: It's important the version of VLC player installed matches the architecture of the Java version you have installed 
(32-bit or 64-bit), otherwise it won't be possible to access VLC's libraries for video playback. 

Getting started
---------------
This program is still under development and as such getting it to run means you need to do some manual setting up:

1. First make sure you have all the requirements installed.
2. Run the hs.mediasystem.MediaSystem class from Eclipse.


Third Party Dependencies
========================

VLC for Java (vlcj)
------------------
by Mark Lee of Caprica Software  
License: GNU GPL, version 3 or later  
https://github.com/caprica/vlcj

The Java TVDB API (javatvdbapi)
-------------------------------
License: GNU GPL, version 3  
http://code.google.com/p/javatvdbapi/

The MovieDB API (themoviedbapi)
-------------------------------
License: GNU GPL, version 3  
http://code.google.com/p/themoviedbapi/

Apache Felix
------------
License: Apache License, version 2.0  
http://felix.apache.org/site/index.html

Apache Derby
------------
License: Apache License, version 2.0  
http://db.apache.org/derby/