#!/usr/bin/python
# -*- coding: utf-8 -*-
#
import cgi
import cgitb; cgitb.enable() # for debugging

import os
import pynmea2
import datetime
import decimal

FILENAME="nmea.txt"
TIMESTAMPFILE="timestamp.txt"
KPLEX="kplex serial:filename=/dev/ttyUSB0,direction=in serial:filename=/dev/rfcomm0,direction=in file:filename=%s,direction=out"
STORAGE="/var/www/nmea"

def findLatestOutpath():
  currentMax = max([0] + [int(d) for d in os.listdir(STORAGE) if os.path.isdir(os.path.join(STORAGE, d))])
  return (STORAGE + "/%05d") % (currentMax)

def findOutpath(idstr):
  if idstr is None:
    return findLatestOutpath()
  else:
    return STORAGE + "/" + idstr


print "Content-Type: text/plain\n"

form = cgi.FieldStorage()
idstr = form.getvalue("id", None)

nmeafile = os.path.join(findOutpath(idstr), FILENAME)

f = open(nmeafile, "r")

curLat = None
curLng = None
curDepth = None
newPos = False
newDepth = False
time = None

for line in f.readlines():
  try:
    if line.startswith("$SDDBT"):
      msg = pynmea2.parse(line)
      if not msg.depth_meters is None:
        curDepth = msg.depth_meters
        newDepth = True
    elif line.startswith("$GPGGA"):
      msg = pynmea2.parse(line)
      if msg.gps_qual != '0':
        curLat = msg.latitude
        curLng = msg.longitude
        newPos = True
    elif line.startswith("$GPRMC"):
      msg = pynmea2.parse(line)
      if not ((msg.datestamp is None) or (msg.timestamp is None)):
        time = datetime.datetime.combine(msg.datestamp, msg.timestamp)
    else:
      continue
  except (pynmea2.nmea.ChecksumError, pynmea2.nmea.ParseError, decimal.InvalidOperation) as e:
    pass
    
  if newPos and newDepth:
    print "%f,%f,%f,%s" % (curLat, curLng, curDepth, time)
    newPos = False
    newDepth = False 
