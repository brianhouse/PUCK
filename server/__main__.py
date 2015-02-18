#!/usr/bin/env python

import sys, os, base64, time, tornado, json, yaml
sys.path.append(os.path.join(os.path.dirname(__file__), ".."))
from housepy import tornado_server, strings, log, util
from housepy.tornado_server import Handler
from pucker import Puck
from hub import Hub


def main():

    handlers = [             
        (r"/", Home),
        (r"/?([^/]*).json", PuckHandler),
        (r"/?([^/]*)", Chart),
    ]

    # def on_data(puck):
    #     Chart.post_new_data(puck)
    # Hub(callback=on_data).start()

    # so is this scaleable? does it slow down based on the number of clients?
    # would queueing recent data from all pucks work better? yeah.

    # lose hub. each poll, just return history.

    tornado_server.start(handlers)


class Home(Handler):

    def get(self):
        log.info("Home.get list")
        active_pucks = list(self.redis.smembers("pucks:active"))
        inactive_pucks = list(self.redis.smembers("pucks:inactive"))
        return self.render("home.html", active_pucks=active_pucks, inactive_pucks=inactive_pucks)
        

class PuckHandler(Handler):

    def get(self, puck_id):
        log.info("Home.get puck %s" % puck_id)
        puck = Puck(self.redis, puck_id)

        # actions
        mode = self.get_argument("mode", None)
        if mode is not None:
            puck.mode = mode
            return self.redirect("/%s" % puck_id)

        return self.json(puck.get_info())


class Chart(Handler):
    
    callbacks = []
        
    def get(self, puck_id):
        log.info("Chart.get %s" % puck_id)
        puck = Puck(self.redis, puck_id)
        return self.render("chart.html", puck=puck)

    def post(self, puck_id):
        log.info("Chart.post %s" % puck_id)  
        puck = Puck(self.redis, puck_id)
        return self.json(puck.get_history(25))

    # @tornado.web.asynchronous
    # def post(self, puck_id):
    #     log.info("Chart.post %s" % puck_id)  
    #     Chart.callbacks.append((puck_id, self.async_callback(self._on_data)))

    # @classmethod
    # def post_new_data(cls, puck):
    #     if not len(cls.callbacks):
    #         return
    #     for callback in cls.callbacks:
    #         puck_id, function = callback
    #         if puck.id == int(puck_id):   
    #             function(puck)
    #     cls.callbacks = []            

    # def _on_data(self, puck):
    #     self.json(puck.get_history(25))
    #     try:
    #         self.finish()    
    #     except IOError, e:
    #         log.warning(log.exc(e))


if __name__ == "__main__":
    main()
    
    
