#!/usr/bin/env python

import json, random, time
from housepy import config, log, osc

def on_message(location, address, data):    
    if "/puck/" not in address:
        return    
    try:
        puck_id = int(address.replace("/puck/", ""))
    except Exception as e:
        log.error(log.exc(e))

receiver = osc.Receiver(12121, on_message)
sender = osc.Sender(23232)

# keep alive
while True:
    sender.send('/subscribe', 12121)
    time.sleep(10)
