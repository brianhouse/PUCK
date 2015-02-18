#!/usr/bin/env python

"""
sudo port install redis
sudo pip install redis
sudo pip install hiredis

redis-server /opt/local/etc/redis.conf

"""

import redis, json, random, time

r = redis.StrictRedis()

puck_id = 2

# r.set('puck:%s:mac' % puck_id, "A_MAC_ADDRESS")
# r.set('puck:%s:address' % puck_id, "0x00")
# r.set('puck:%s:name' % puck_id, "mirror")

# t = time.time()
# data = {'t': t, 'A0': random.random()}
# data_string = json.dumps(data)

# r.zadd('puck:%s:zdata' % puck_id, t, data_string)

# print r.get('puck:%s:mac' % puck_id)
# print r.get('puck:%s:address' % puck_id)
# print r.get('puck:%s:name' % puck_id)
# print

# print
# print r.zrangebyscore('puck:%s:data' % puck_id, t-2, t+1)

# print r.zrange('puck:%s:data' % puck_id, -10, -1, withscores=True)

r.hmset('puck:%s:attributes' % puck_id, {'name': "house", 'instrument': "bass"})

print r.hgetall('puck:%s:attributes' % puck_id)

# figure out how to take snapshots
