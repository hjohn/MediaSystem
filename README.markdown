MediaSystem
===========

Front-end application for video libraries.

Features
--------
* Select video to view from a nicely styled list
* Groups videos by season and series
* Downloads meta information about videos from TMDB (www.themoviedb.org) and TVDB (thetvdb.com)
* Capable of downloading subtitles from Sublight (www.sublight.si) and OpenSubtitles.org (www.opensubtitles.org)

Requirements
------------
* Java Runtime Environment 1.7+ installed
* JavaFX 2.2 installed
* PostgreSQL database installed
* VLC player 2.0+ installed



Getting started
---------------
This program is currently under heavy development and as such getting it to run means you need to do some manual setting up.

1. First make sure you have all the requirements installed.
2. Create a database in PostgreSQL called 'mediasystem' with create rights.
3. Look at 'mediasystem-example.ini', make a copy of it in the root of the project and rename it 'mediasystem.ini'.  Edit it as needed.
4. Run the hs.mediasystem.MediaSystem class from Eclipse.


Third Party Dependencies
========================

vlcj, VLC for Java
------------------
by Mark Lee of Caprica Software  
License: GNU GPL, version 3 or later  
https://github.com/caprica/vlcj

javatvdbapi, The Java TVDB API
------------------
License: GNU GPL, version 3  
http://code.google.com/p/javatvdbapi/

themoviedbapi, The MovieDB API
------------------
License: GNU GPL, version 3
http://code.google.com/p/themoviedbapi/  
