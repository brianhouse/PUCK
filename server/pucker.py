#!/usr/bin/env python

import redis, json, random, time
from housepy import config, log, osc, strings, science


class Puck(object):

    def __init__(self, r, id):
        self.id = int(id)
        self.redis = r
        self._rssi = None
        self._active = self.redis.sismember('pucks:active', self.id)
        self._mode = self.redis.get('puck:%s:mode' % self.id)
        self._attributes = self.redis.hgetall('puck:%s:attributes' % self.id)
        self._data = {'a0': 0, 'a1': 0, 'a2': 0}
        self._delta = 0
        self.update()

    @property
    def rssi(self):
        return self._rssi

    @property
    def active(self):
        return self._active

    @property
    def mode(self):
        return self._mode

    @mode.setter
    def mode(self, value):
        value = strings.safestr(value)
        log.info("Puck.mode set %s [%s]" % (value, self.id))
        self.redis.set('puck:%s:mode' % self.id, value)
        self._mode = value

    @property
    def attributes(self):
        return self._attributes

    @attributes.setter
    def attributes(self, value):
        assert type(value) is dict
        self.redis.set('puck:%s:attributes', value)
        self._attributes = value

    @property
    def vector(self):
        return self._data['a0'], self._data['a1'], self._data['a2']

    @property
    def delta(self):
        return self._delta

    def update(self):
        self._data = json.loads(self.redis.zrange('puck:%s:data' % self.id, -1, -1)[0])
        self._rssi = self._data['rssi']
        self._delta = self._data['delta']

    def get_history(self, records=10):
        ## put this in the puck object
        data = []
        for item in self.redis.zrange('puck:%s:data' % self.id, (-1 * records), -1, withscores=True):
            try:
                d = json.loads(item[0])
                d['t'] = item[1]
                data.append(d)
            except Exception as e:
                log.warning(log.exc(e))
                pass
        return data

    def get_info(self):
        return {'id': self.id,
                'active': self.active,
                'mode': self.mode,
                'rssi': self.rssi,
                'data': self.get_history(10),
                'attributes': self.attributes
                }

    def __repr__(self):     ## should be json, so can print directly? or a different to_json method?
        return "[%s] %s %s %s %s %s %s %f %s" % (self.id, self.rssi, "+" if self.active else " ", self.mode, self.vector[0], self.vector[1], self.vector[2], self._delta, "<%s>" % self.attributes if self.attributes else "")

