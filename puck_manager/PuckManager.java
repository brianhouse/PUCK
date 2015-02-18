import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.rapplogic.xbee.api.*;
import com.rapplogic.xbee.api.wpan.*;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import redis.clients.jedis.Jedis;
import org.yaml.snakeyaml.Yaml;

public class PuckManager {
    
    public static XBee xbee;
    public static Jedis redis;

    public static void main (String[] args) throws XBeeException, IOException {

        PropertyConfigurator.configure("log4j.properties");

        Map<Object, Object> puck_config;
        try {
            Yaml yaml = new Yaml();
            String yaml_data = PuckManager.readFile("../pucks.yaml");    
            puck_config = (Map<Object, Object>)yaml.load(yaml_data);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        String port = (String)((Map<Object, Object>)puck_config.get("base")).get("port");
        Map<Object, Object> pucks_data = (Map<Object, Object>)puck_config.get("pucks");

        Puck[] pucks = new Puck[pucks_data.size()];
        int i = 0;
        for (Map.Entry<Object, Object> puck_data: pucks_data.entrySet()) {
            int address = ((Number)puck_data.getKey()).intValue();
            String mac = (String)puck_data.getValue();
            pucks[i] = new Puck(new XBeeAddress64(mac), new XBeeAddress16(0, address));    // what happens when you have 10 nodes?
            i++;
        }

        // get connection to Redis
        try {
            redis = new Jedis("localhost");
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        
        // open connection
        // serial data coming in will screw up the connection, careful.
        try {   
            System.out.println("Opening connection to " + port + " at 9600");
            xbee = new XBee(new XBeeConfiguration().withMaxQueueSize(100).withStartupChecks(false));    // turn off checks
            xbee.open("/dev/tty.usbserial-A70061ew", 9600);
            System.out.println("--> opened");
        } catch (Exception e) {
            System.out.println(e.toString());
            xbee.close();
            return;
        }
         
        for (Puck puck: pucks) {  
            puck.queryActive();
        }

        // main event loop
        while (true) {
            Puck puck = null;
            // try {                
                
                // listen for data 
                XBeeResponse response;   
                try {
                    response = xbee.getResponse(250);
                } catch (XBeeTimeoutException e) {                    
                    continue;
                }
                if (response.isError()) {
                    System.out.println("Bad data: " + response);
                    continue;
                }
                if (response.getApiId() == ApiId.REMOTE_AT_RESPONSE) {
                    RemoteAtResponse at_response = (RemoteAtResponse)response;
                    if (!at_response.isOk()) {
                        System.out.println("Bad AT response: " + response);
                        continue;
                    }
                    puck = Puck.find((XBeeAddress)at_response.getRemoteAddress16());
                    puck.onCommandResponse(at_response);
                } else if (response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
                    RxResponseIoSample rx_response = (RxResponseIoSample)response;
                    puck = Puck.find(rx_response.getSourceAddress());
                    puck.rssi = Math.abs(rx_response.getRssi());
                    for (IoSample sample: rx_response.getSamples()) {                                      
                        puck.onData(sample);
                    }
                } else {                        
                    System.out.println("Received unhandled response type:" + response);
                }
                
                // send data
                // OSC?
                // if (puck != null) {
                //     String result = PuckManager.readFile("interact.txt").replaceAll("\\s","");
                //     if (result.length() != 0) {
                //         if (result.equals("active")) {                            
                //             puck.queryActive();                            
                //         } else if (result.equals("wake")) {
                //             puck.setActive(true);                            
                //         } else if (result.equals("sleep")) {
                //             puck.setActive(false);
                //         } else if (result.equals("set")) {
                //             // int[] byte_value = ByteUtils.convertInttoMultiByte(250);        
                //             // puck.sendCommand("ST", byte_value);                            
                //             // int[] byte_value = ByteUtils.convertInttoMultiByte(100);        
                //             // puck.sendCommand("IR", byte_value);
                //             puck.sendCommand("MY");
                //         } else {
                //             System.out.println("--> no command '" + result + "'");
                //         }
                //         PuckManager.writeFile("interact.txt", "");
                //     }                     
                // }
                                        
            // } catch (Exception e) {
            //     System.out.println(e.getStackTrace().toString());
            // } finally {
            //     xbee.close();
            // }                    
        }

    }

    private static String readFile (String path) throws IOException {

        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }

    }   
    
    // private static void writeFile (String filename, String content) {

    //     BufferedWriter writer = null;
    //     try {
    //         writer = new BufferedWriter(new FileWriter(filename));
    //         writer.write(content);
    //     } catch (IOException e) {
    //         System.out.println(e);
    //     } finally {
    //         try {
    //             if (writer != null)
    //                 writer.close();
    //             } catch (IOException e) {
    //         }
    //     }    
    
    // } 

}

class Puck {

    public static ArrayList<Puck> pucks = new ArrayList<Puck>();

    public XBeeAddress64 mac;
    public XBeeAddress16 address;
    public int rssi = 0;
    public boolean active = true;
    public int rate = 0;
    public int[] vector = {0, 0, 0};
    public int sleep_count = 0;

    public static Puck find (XBeeAddress address) {

        for (int i=0; i<Puck.pucks.size(); i++) {
            if (Puck.pucks.get(i).address.equals(address)) {
                return Puck.pucks.get(i);
            }
        }
        return null;

    }

    public Puck (XBeeAddress64 arg_mac, XBeeAddress16 arg_address) {

        this.mac = arg_mac;
        this.address = arg_address;
        Puck.pucks.add(this);
        System.out.println("Puck " + this.address);        

    }

    public void queryActive () {

        System.out.println("Puck.queryActive " + this.address);
        this.sendCommand("SM", null);

    }

    public void setActive (boolean arg_active) {

        System.out.println("Puck.setActive " + this.address);
        System.out.println("--> setting to " + arg_active);
        int value = arg_active ? 0 : 5;
        int[] byte_value = ByteUtils.convertInttoMultiByte(value);        
        this.sendCommand("SM", byte_value);

    }

    public void queryRate () {

        System.out.println("Puck.queryRate " + this.address);
        this.sendCommand("IR", null);

    }

    public void setRate (int value) {

        System.out.println("Puck.setRate " + this.address);
        System.out.println("--> setting to " + value);
        int[] byte_value = ByteUtils.convertInttoMultiByte(value);        
        this.sendCommand("IR", byte_value);

    }

    public boolean sendCommand (String command) {

        return this.sendCommand(command, null);

    }

    public boolean sendCommand (String command, int[] value) {

        RemoteAtRequest request;
        try {
            if (value != null) {
                request = new RemoteAtRequest(this.mac, command, value);
            } else {
                request = new RemoteAtRequest(this.mac, command);
            }
            System.out.println("Sending: " + request);
            PuckManager.xbee.sendAsynchronous(request);
        } catch (Exception e) {
            System.out.println("--> failed: " + e.toString());
            return false;
        }
        return true;

    }

    public void onData (IoSample sample) {

        System.out.println("Puck.onData " + this.address);
        if (this.vector[0] == 0 && this.vector[1] == 0 && this.vector[2] == 0) {
            this.vector[0] = sample.getAnalog0();
            this.vector[1] = sample.getAnalog1();
            this.vector[2] = sample.getAnalog2();
            return;
        }
        int[] new_vector = {sample.getAnalog0(), sample.getAnalog1(), sample.getAnalog2()};
        double delta = Math.pow((Math.pow((vector[0] - new_vector[0]), 2) + Math.pow((vector[1] - new_vector[1]), 2) + Math.pow((vector[2] - new_vector[2]), 2)), 1.0/3);
        this.vector = new_vector;

        // push data to redis
        String puck_id = Integer.toString(ByteUtils.convertMultiByteToInt(this.address.getAddress()));        
        String data_string = "{\"rssi\":" + this.rssi + ",\"active\":" + this.active + ",\"delta\":" + delta + ",\"a0\":" + this.vector[0] + ",\"a1\":" + this.vector[1] + ",\"a2\":" + this.vector[2] + "}";
        System.out.println(data_string);
        Double t = System.currentTimeMillis() / 1000.0;
        try {
            PuckManager.redis.zadd("puck:" + puck_id + ":data", t, data_string);
            PuckManager.redis.publish("puck_update", puck_id);
        } catch (Exception e) {
            System.out.println(e);
        }

        // manage sleeping
        if (this.sleep_count != 50) {
            System.out.println("--> sleep in " + (50 - this.sleep_count));
        }
        if (delta > 3.0) {
            this.sleep_count = 0;
            if (!this.active) {        
                this.setActive(true);
            }
        } else if (this.active) {
            if (this.sleep_count >= 50) {
                // this.setActive(false);
            } else {
                this.sleep_count++;
            }
        }

    }

    public void onCommandResponse (AtCommandResponse response) {

        String command = response.getCommand();
        System.out.println("Received: " + response);
        if (command.equals("SM")) {
            this.onActiveResponse(response);
        } else if (command.equals("IR")) {
            this.onRateResponse(response);
        } else {
            System.out.println("Received unhandled AT response: " + response);
        }

    }

    public void onActiveResponse (AtCommandResponse response) {

        System.out.println("Puck.onActiveResponse " + this.address);
        if (ByteUtils.toBase16(response.getValue()).length() == 0) {
            System.out.println("--> OK, confirming...");
            this.queryActive();
            return;
        }
        int value = ByteUtils.convertMultiByteToInt(response.getValue());        
        this.active = value == 0;
        System.out.println("--> " + this.active);
        String puck_id = Integer.toString(ByteUtils.convertMultiByteToInt(this.address.getAddress()));        
        try {
            if (this.active) {
                PuckManager.redis.sadd("pucks:active", puck_id);
                PuckManager.redis.srem("pucks:inactive", puck_id);
            } else {
                PuckManager.redis.srem("pucks:active", puck_id);
                PuckManager.redis.sadd("pucks:inactive", puck_id);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    
    }    

    public void onRateResponse (AtCommandResponse response) {

        System.out.println("Puck.onRateResponse " + this.address);
        if (ByteUtils.toBase16(response.getValue()).length() == 0) {
            System.out.println("--> OK, confirming...");
            this.queryRate();
            return;
        }
        this.rate = ByteUtils.convertMultiByteToInt(response.getValue());        
        System.out.println("--> " + this.rate);
    
    }     


}
