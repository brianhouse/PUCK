#!/usr/bin/env python

import sys, os
sys.path.append(os.path.join(os.path.dirname(__file__), ".."))
import redis, json, random, time, pucker, threading
from housepy import config, log, osc
from pucker import Puck

osc.verbose = False

class Hub(threading.Thread):

    def __init__(self, r=None, callback=None):
        threading.Thread.__init__(self)
        self.daemon = True
        if r is None:
            r = redis.StrictRedis()
        self.redis = r
        self.ps = self.redis.pubsub()
        self.ps.subscribe('puck_update')
        self.callback = callback
        self.sender = osc.Sender()            

    def run(self):
        log.info("Listening for updates...")  
        osc.Receiver(23232, self.on_message)          
        for message in self.ps.listen():
            puck = Puck(self.redis, message['data'])
            puck.update()
            log.info(puck)            
            self.sender.send("/puck/%s" % puck.id, puck.delta)
            if self.callback is not None:
                self.callback(puck)

    def on_message(self, location, address, data):
        if address == "/subscribe":
            location = location, data[0]
            self.sender.add_target(*location)


if __name__ == "__main__":
    r = redis.StrictRedis()    
    Hub(r).start()
    while True:
        time.sleep(1)
