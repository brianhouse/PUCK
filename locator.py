#!/usr/bin/env python

import sys, os
sys.path.append(os.path.join(os.path.dirname(__file__), ".."))
from housepy import xbee, log, config

log.info("PUCK locator")

def on_message(message):
    try:
        puck_id = message.address_16
        rssi = message.rssi
        log.info("%s: %s" % (puck_id, rssi))
    except Exception as e:
        log.error(log.exc(e))

xbee.Receiver("/dev/tty.usbserial-A100RUNQ", message_handler=on_message, blocking=True)
