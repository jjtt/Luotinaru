= asennus =

* kplex deb-paketista
* apt install lighttpd python-pip python-cheetah
* pip install pynmea2
* cp luotinaru.py /root/
* cp luotinaru /etc/init.d/
* update-rc.d luotinaru defaults
* cp lighttpd.conf /etc/lighttpd/
* service lighttpd restart
* rmdir /var/www/html
* mkdir /var/www/cgi-bin
* cp index.py info_latest.py latest.py shutdown.c stop.py /var/www/cgi-bin
* gcc -o shutdown.cgibin shutdown.c
* mkdir /var/www/nmea
