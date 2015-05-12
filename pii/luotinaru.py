#!/usr/bin/python
# -*- coding: utf-8 -*-


import logging

import sys
import os
import datetime
import subprocess

# Setup logging
logging.basicConfig(level=logging.DEBUG)

FILENAME="nmea.txt"
TIMESTAMPFILE="timestamp.txt"
KPLEX="kplex serial:filename=/dev/ttyUSB0,direction=in serial:filename=/dev/rfcomm0,direction=in file:filename=%s,direction=out file:filename=-,direction=out"
STORAGE="/var/www/nmea"
STOPFILE="stop"

def startKplex(outpath):
  stopfile = os.path.join(STORAGE, STOPFILE)
  outfile = os.path.join(outpath, FILENAME)
  #p = os.popen(KPLEX % outfile,'r')
  p = subprocess.Popen(KPLEX % outfile, shell=True, stdout=subprocess.PIPE)
  line = p.stdout.readline()
  nostop = True
  while line and nostop:
    if os.path.isfile(stopfile):
      nostop = False
    line = p.stdout.readline()
 
  p.kill()
  os.remove(stopfile)

def findNextOutpath():
  currentMax = max([0] + [int(d) for d in os.listdir(STORAGE) if os.path.isdir(os.path.join(STORAGE, d))])
  return (STORAGE + "/%05d") % (currentMax+1)




def main(argv=None):
  if argv is None:
    argv = sys.argv
  logging.debug("main")

  path = findNextOutpath()
  
  os.mkdir(path)

  # add timestamp
  f = open(os.path.join(path, TIMESTAMPFILE), "w")
  f.write(datetime.datetime.now().isoformat())
  f.close()

  startKplex(path)



if __name__ == "__main__":
  main()

