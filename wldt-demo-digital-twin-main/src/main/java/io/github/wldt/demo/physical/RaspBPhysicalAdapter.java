package io.github.wldt.demo.physical;

import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
public class RaspBPhysicalAdapter extends PhysicalAdapter{

    /*private final static String ON_OFF_PROPERTY_KEY = "ON/OFF-property-key";
    private final static String ON_OFF_EVENT_KEY = "ON/OFF-event-key";
    private final static String SET_ON_OFF_ACTION_KEY = "set-ON/OFF-action-key";*/

    private final static String LED_ON_OFF_PROPERTY_KEY = "LED-property-key";
    private final static String LED_ON_OFF_EVENT_KEY = "LED-action-key";
    private final static String LED_ON_OFF_ACTION_KEY = "set-LED-ON/OFFaction-key";
    
    private final static String PIR_PROPERTY_KEY = "PIR-property-key";
    private final static String PIR_EVENT_KEY = "PIR-action-key";
    private final static String PIR_ACTION_KEY = "set-PIR-action-key"; // serve?


    private final static int MESSAGE_UPDATE_TIME = 10000;
    private final static int MESSAGE_UPDATE_NUMBER = 1000;

    private PhysicalAssetRelationship<String> insideInRelationship = null;


    public RaspBPhysicalAdapter(String id) {
        super(id);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{
            if (physicalAssetActionWldtEvent != null
                    && physicalAssetActionWldtEvent.getActionKey().equals(LED_ON_OFF_ACTION_KEY)
                    && physicalAssetActionWldtEvent.getBody() instanceof Double) {
                System.out.println("[RaspBPhysicalAdaper] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                    + " with body: " + physicalAssetActionWldtEvent.getBody());
            }
            else {
                System.out.println("[RaspBPhysicalAdapter] -> wrong Action Received ]");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStart() {
        try {
            new Thread(publishPhysicalAssetDescription()).start();

            new Thread(deviceEmulation()).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStop() {

    }

    private Runnable deviceEmulation() {
        return () -> {
            try {


                System.out.println("[DemoPhysicalAdapter] -> Sleeping before Starting Physical Device Emulation ...");

                //Sleep 5 seconds to emulate device startup
                Thread.sleep(10000);

                System.out.println("[DemoPhysicalAdapter] -> Starting Physical Device Emulation ...");

                //Create a new random object to emulate temperature variations
                Random r = new Random();

                //mio tentativo
                publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(LED_ON_OFF_EVENT_KEY, "ON"));
                for (int i = 0; i < MESSAGE_UPDATE_NUMBER; i++) {
                    Thread.sleep(MESSAGE_UPDATE_TIME);
                    int randomON_OFF = r.nextInt(2);
                    PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, randomON_OFF);
                    publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                }

                publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(LED_ON_OFF_EVENT_KEY, "OFF"));
            } catch (EventBusException | InterruptedException e) {
                e.printStackTrace();
            }
        };

    }

        private Runnable publishPhysicalAssetDescription(){
            return () -> {
                try {
                    System.out.println("[RaspBPhysicalAdapter] -> Sleeping before publishing Physical Asset Description... ");
                    Thread.sleep(5000);
                    System.out.println("[RaspBPhysicalAdapter] -> Publishing physical Asset Description...");
                    PhysicalAssetDescription pad = new PhysicalAssetDescription();

                    PhysicalAssetProperty<Integer> ONOFFProperty = new PhysicalAssetProperty<>(LED_ON_OFF_PROPERTY_KEY, 0);
                    pad.getProperties().add(ONOFFProperty);
                    PhysicalAssetEvent turningON_OFFEvent = new PhysicalAssetEvent(LED_ON_OFF_EVENT_KEY, "text/plain");
                    pad.getEvents().add(turningON_OFFEvent);
                    PhysicalAssetAction setONOFFAction = new PhysicalAssetAction(LED_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                    pad.getActions().add(setONOFFAction);
                    this.insideInRelationship = new PhysicalAssetRelationship<>("insideId");
                    pad.getRelationships().add(insideInRelationship);

                    //Add a new Property associated to the target PAD with a key and a default value
                    //Add the declaration of a new type of generated event associated to a event key
                    // and the content type of the generated payload
                    //Declare the availability of a target action characterized by a Key, an action type
                    // and the expected content type and the request body
                    //Create Test Relationship to describe that the Physical Device is inside a building
                    //Notify the new PAD to the DT's Shadowing Function

                    this.notifyPhysicalAdapterBound(pad);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }


        private void publishPhysicalRelationShipInstance () {
            try {
                String relationshipTareget = "building-hq";
                    Map<String, Object> relationshipMetaData = new HashMap<>();
                    relationshipMetaData.put("floor", "f0");
                    relationshipMetaData.put("room", "r0");
                    PhysicalAssetRelationshipInstance<String> relInstance = this.insideInRelationship.createRelationshipInstance(relationshipTareget, relationshipMetaData);
                    PhysicalAssetRelationshipInstanceCreatedWldtEvent<String> relInstanceEvent = new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(relInstance);
                    publishPhysicalAssetRelationshipCreatedWldtEvent(relInstanceEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

