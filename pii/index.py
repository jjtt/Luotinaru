#!/usr/bin/python
# -*- coding: utf-8 -*-
#
import cgi
import cgitb; cgitb.enable() # for debugging

import os
from Cheetah.Template import Template

FILENAME="nmea.txt"
TIMESTAMPFILE="timestamp.txt"
STORAGE="/var/www/nmea"

def listDirs():
  return sorted([d for d in os.listdir(STORAGE) if os.path.isdir(os.path.join(STORAGE, d))])

def fileData(idstr):
  filedata = None

  try:
    path = os.path.join(STORAGE, idstr)

    timestampfile = open(os.path.join(path, TIMESTAMPFILE))
    timestamp = timestampfile.readline()
    timestampfile.close()

    nmeafile = open(os.path.join(path, FILENAME))
    rows = len(nmeafile.readlines())
    nmeafile.close()

    filedata = {
      'id': idstr,
      'name': idstr,
      'timestamp': timestamp.replace('T', ' '),
      'rows': rows
    }
  except IOError:
    filedata = None

  return filedata

  


print "Content-Type: text/html; charset=utf-8\n"

tmpl = u"""
<html>
  <head>
    <title>Index</title>
  </head>
  <body>
    <a href="latest.py">Latest</a>

    <table>
      <thead>
        <tr>
          <th>Linkki</th>
          <th>Aikaleima</th>
          <th>Rivejä</th>
        </tr>
      </thead>
      <tbody>
#for $file in $files
        <tr>
          <td><a href="latest.py?id=$file.id">$file.name</a></td>
          <td>$file.timestamp</td>
          <td style="text-align: right;">$file.rows</td>
        </tr>
#end for
      </tbody>
 </body>
</html>
"""

files = []

for idstr in listDirs():
  filedata = fileData(idstr)
  if not (filedata is None):
    files.append(filedata)

context = {'files': files}

t = Template(tmpl, searchList=[context], filter='WebSafe')

print t
