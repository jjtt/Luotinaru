#!/usr/bin/python
# -*- coding: utf-8 -*-
#
import cgi
import cgitb; cgitb.enable() # for debugging

import os

STORAGE="/var/www/nmea"
STOPFILE="stop"

print "Content-Type: text/plain\n"

f = open(os.path.join(STORAGE, STOPFILE), "w")
f.close()

print "OK"
