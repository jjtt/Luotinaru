#!/usr/bin/python
# -*- coding: utf-8 -*-
#
import cgi
import cgitb; cgitb.enable() # for debugging

import os
import pynmea2

FILENAME="nmea.txt"
STORAGE="/var/www/nmea"

def findLatestOutpath():
  currentMax = max([0] + [int(d) for d in os.listdir(STORAGE) if os.path.isdir(os.path.join(STORAGE, d))])
  return (STORAGE + "/%05d") % (currentMax)

def reversed_lines(file):
    "Generate the lines of file in reverse order."
    part = ''
    for block in reversed_blocks(file):
        for c in reversed(block):
            if c == '\n' and part:
                yield part[::-1]
                part = ''
            part += c
    if part: yield part[::-1]

def reversed_blocks(file, blocksize=4096):
    "Generate blocks of file's contents in reverse order."
    file.seek(0, os.SEEK_END)
    here = file.tell()
    while 0 < here:
        delta = min(blocksize, here)
        here -= delta
        file.seek(here, os.SEEK_SET)
        yield file.read(delta)


print "Content-Type: text/plain\n"

nmeafile = os.path.join(findLatestOutpath(), FILENAME)

gpsmsg = None
depthmsg = None
depthtime = None

found = False

for line in reversed_lines(open(nmeafile, 'r')):
  if line.startswith("$SDDBT"):
    if depthmsg is None:
      depthmsg = pynmea2.parse(line)
  elif line.startswith("$GPGGA"):
    msg = pynmea2.parse(line)
    if msg.gps_qual != '0':
      if depthmsg is None:
        if gpsmsg is None:
          gpsmsg = msg
      else:
        if gpsmsg is None:
          gpsmsg = msg
        depthtime = msg.timestamp
  else:
    continue
  
  if not (gpsmsg is None or depthmsg is None or depthtime is None):
    # found latest info for gps and depth
    found = True
    break

if found:
  print "DATA"
else:
  print "NODATA"
print "GPS"
if gpsmsg is None:
  print None
  print None
  print None
  print None
  print None
  print None
else:
  print gpsmsg.timestamp
  print gpsmsg.latitude
  print gpsmsg.longitude
  print gpsmsg.gps_qual
  print gpsmsg.num_sats
  print gpsmsg.horizontal_dil
print "DEPTH"
print depthtime
if depthmsg is None:
  print None
else:
  print depthmsg.depth_meters
