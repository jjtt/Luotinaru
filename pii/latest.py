#!/usr/bin/python
# -*- coding: utf-8 -*-
#
import cgi
import cgitb; cgitb.enable() # for debugging

import os
import pynmea2

FILENAME="nmea.txt"
TIMESTAMPFILE="timestamp.txt"
KPLEX="kplex serial:filename=/dev/ttyUSB0,direction=in serial:filename=/dev/rfcomm0,direction=in file:filename=%s,direction=out"
STORAGE="/var/www/nmea"

def findLatestOutpath():
  currentMax = max([0] + [int(d) for d in os.listdir(STORAGE)]) 
  return (STORAGE + "/%05d") % (currentMax)


print "Content-Type: text/plain\n"

nmeafile = os.path.join(findLatestOutpath(), FILENAME)

f = open(nmeafile, "r")

curLat = None
curLng = None
curDepth = None
newPos = False
newDepth = False

for line in f.readlines():
  if line.startswith("$SDDBT"):
    msg = pynmea2.parse(line)
    curDepth = msg.depth_meters
    newDepth = True
  elif line.startswith("$GPGGA"):
    msg = pynmea2.parse(line)
    curLat = msg.latitude
    curLng = msg.longitude
    newPos = True
  else:
    continue
    
  if newPos and newDepth:
    print "%f,%f,%f" % (curLat, curLng, curDepth)
    newPos = False
    newDepth = False 
