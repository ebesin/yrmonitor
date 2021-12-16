/**
 * Copyright (c) 2014 Jilk Systems, Inc.
 * 
 * This file is part of the Java ROSBridge Client.
 *
 * The Java ROSBridge Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Java ROSBridge Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Java ROSBridge Client.  If not, see http://www.gnu.org/licenses/.
 * 
 */
package com.jilk.ros;

import com.jilk.ros.message.Clock;
import com.jilk.ros.message.Log;
import com.jilk.ros.rosapi.message.Empty;
import com.jilk.ros.rosapi.message.GetTime;
import com.jilk.ros.rosapi.message.MessageDetails;
import com.jilk.ros.rosapi.message.Type;
import com.jilk.ros.rosbridge.ROSBridgeClient;

public class Example {
    
    public Example() {}
    
    public static void main(String[] args) {
        ROSBridgeClient client = new ROSBridgeClient("ws://162.243.238.80:9090");
        client.connect();
        //testTopic(client);
        try {
        testService(client);
        }
        catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        finally {
            client.disconnect();
        }
    }            
    
    public static void testService(ROSBridgeClient client) {
        try {
            Service<Empty, GetTime> timeService =
                    new Service<Empty, GetTime>("/rosapi/get_time", Empty.class, GetTime.class, client);
            timeService.verify();
            //System.out.println("Time (secs): " + timeService.callBlocking(new Empty()).time.sec);
            
            Service<com.jilk.ros.rosapi.message.Service, Type> serviceTypeService =
                    new Service<com.jilk.ros.rosapi.message.Service, Type>("/rosapi/service_type",
                        com.jilk.ros.rosapi.message.Service.class, Type.class, client);
            serviceTypeService.verify();
            String type = serviceTypeService.callBlocking(new com.jilk.ros.rosapi.message.Service("/rosapi/service_response_details")).type;
            
            Service<Type, MessageDetails> serviceDetails =
                    new Service<Type, MessageDetails>("/rosapi/service_response_details",
                        Type.class, MessageDetails.class, client);
            serviceDetails.verify();
            //serviceDetails.callBlocking(new Type(type)).print();
            
            com.jilk.ros.Topic<Log> logTopic =
                    new com.jilk.ros.Topic<Log>("/rosout", Log.class, client);
            logTopic.verify();

        }
        catch (InterruptedException ex) {
            System.out.println("Process was interrupted.");
        }
    }
    
    public static void testTopic(ROSBridgeClient client) {
        com.jilk.ros.Topic<Clock> clockTopic = new com.jilk.ros.Topic<Clock>("/clock", Clock.class, client);
        clockTopic.subscribe();
        try {
            Thread.sleep(20000);} catch(InterruptedException ex) {}
        Clock cl = null;
        try {
            cl = clockTopic.take(); // just gets one
        }
        catch (InterruptedException ex) {}
        cl.print();
        cl.clock.nsecs++;
        clockTopic.unsubscribe();
        clockTopic.advertise();
        clockTopic.publish(cl);
        clockTopic.unadvertise();
    }
}
