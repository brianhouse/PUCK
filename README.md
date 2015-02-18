PUCK of Ubiquitous Contextual Knowledge
=======================================

### "Just PUCK-it"

PUCK is a system of low-power, distributed sensing units.

Running on the PUCK server:
- The PUCK Manager handles all the logic of the sensors and dumps incoming data into a redis db. (JAVA)
- The Hub waits for updates and then distributes to any clients subscribed over OSC. (Python)
- The HTTP server provides a web interface to see what's happening with the PUCKs. (Python)

Running remotely on the network:
- The Client subscribes to PUCK data from the Hub. (Python)
- The Locator is a remote unit that is not collecting information, but is watching for nearby PUCK signal strength. (Python)

-- NOTE: should standardize the setup of the locators to always have a location


Software Setup
==============

#### Install and run redis
    sudo port install redis
    sudo pip install redis
    sudo pip install hiredis
    cd redis
    start.sh

#### Clone repo
    hg clone https://bitbucket.org/nytlabs/PUCK
    cd PUCK

#### Get housepy
    hg clone https://code.google.com/p/housepy/ housepy
Make sure all dependencies are installed from housepy/README.txt

#### puck_manager: JAVA coordinator and internal puck logic
    cd puck_manager
    ./run.sh            # compiles and runs    

#### server: HTTP server for querying puck attributes
    python server

#### hub: Python Puck<->OSC bridge
    python hub.py




Materials
=========

## Remote PUCK
- [series 1 xbee](http://www.sparkfun.com/products/8665) (wire antenna slightly better range and better outdoors than chip): $23
- [xbee regulated breakout board](http://www.sparkfun.com/products/9132) $10
- [accelerometer](http://www.sparkfun.com/products/9269): $25
- headers for accelerometer
- [AAA case with switch](http://www.sparkfun.com/products/9543): $1.50
($60)

wire it like this: [http://www.electronics-lab.com/projects/sensors/009/index.html](http://www.electronics-lab.com/projects/sensors/009/index.html)
[http://www.youtube.com/watch?v=3RGcYiV4hEE](http://www.youtube.com/watch?v=3RGcYiV4hEE)


## Base Station
- [Xbee explorer](http://www.sparkfun.com/products/8687): $25
- [series 1 xbee](http://www.sparkfun.com/products/8665) (wire antenna slightly better range and better outdoors than chip): $23
($48)


### Questions
can the firmware be updated with the explorer?


XBee Settings
=============

#### NOTE: THIS IS SERIES 1
- install drivers (both Win and Mac): http://www.ftdichip.com/Drivers/VCP.htm
- use X-CTU, PC only, to upgrade firmware (Win: install VCP driver before connecting the device for the first time!)
- find connected devices (OSX): ls /dev/tty.*
- use CoolTerm to configure
- type AT command hit return to query the value, put a value right after the command (no space) to set it
- (http://easycalculation.com/hex-converter.php)

#### Basic
    
    +++     <-- to get it's attention    
    ATRE    <-- reset all parameters
    ATAP    <-- AT-mode(1) or API-mode(2), default: 1
    ATCE    <-- "coordinator enable", 1 for base, 0 for end devices, default: 0
    ATMY    <-- the 16-bit address of the node, default: 0

#### change network
    ATBD    <-- baud rate (0: 1200, 1: 2400, 2: 4800, 3: 9600, 4: 19200, 5: 38400, 6: 57600, 7: 115200)  default: 3/9600
    ATCH    <-- channel (default: 0C / 12)
    ATID    <-- PAN network ID (four digits hex) default: 3332
    ATDL    <-- send messages to this 16-bit address, or FFFF to broadcast

#### set up pin sampling
    ATIR    <-- when set, sample rate at which all inputs are sampled (64 is 100ms, 1f4 is 500ms, 3e8 1s)
    ATIT    <-- samples to collect before transmitting, set to 1
    ATD0    <-- (0: disabled, 2: analog, 3: digital?)
    ATD1    <-- (0: disabled, 2: analog, 3: digital?)
    ATD2    <-- (0: disabled, 2: analog, 3: digital?)

#### manage sleeping
    ATSM    <-- sleep mode (0 default: disabled, 1: sleep when pin 9 asserted, 2: sleep when pin 9 asserted (faster, more power consump), 4: cyclic sleep, 5: cyclic sleep w/ pin wake-up (de-assertion wakes, but then it stays awake until the cycle hits)
    ATSP    <-- sleep period, in 1/100th seconds, from 320ms to 28s (1s is 64hex, 5s is 1f4, 10s is 3e8) default: 0
    ATST    <-- idle time necessary after messages are transmitted to fall asleep (lower is more elegant, but you need all the commands to get accross. 1f4 is 500ms, 2s is 7d0) default: 5000 (5s)

#### save and bake
    ATWR    <-- save changes
    ATCN    <-- exit command mode


#### quick PUCK setup
    +++
    ATRE
    ATAP2
    ATMY2
    ATDLFFFF
    ATIR64
    ATIT1
    ATD02
    ATD12
    ATD22
    ATSP3E8
    ATSTFA
    ATWR
    ATCN

#### quick BASE setup
    +++
    ATRE
    ATAP2
    ATMY1
    ATCE1
    ATWR
    ATCN

#### addresses
- long address is the full 64-bit mac address printed on the back of the xbee, always starts with 0013A200 (for XBee brand)
- short address is 16-bit and is set with ATMY (series 2 assigns it automatically)




Resources
=========

- [http://code.google.com/p/xbee-api/](http://code.google.com/p/xbee-api/)
- [http://log.liminastudio.com/itp/xbee-series-1-handy-command-reference](http://log.liminastudio.com/itp/xbee-series-1-handy-command-reference)
- [http://www.libelium.com/squidbee/index.php?title=How_to_set_XBee_parameters](http://www.libelium.com/squidbee/index.php?title=How_to_set_XBee_parameters)
- [http://www.damonkohler.com/2008/11/xbee-znet-25-wireless-accelerometer.html](http://www.damonkohler.com/2008/11/xbee-znet-25-wireless-accelerometer.html)




Presentation
============

blur the lines between programming and product use

twine
ninja blocks
green goose


Notes
=====

ok, can I detect battery life via the xbee pin?

ideal: USB chargeable, with low-battery alerts.



