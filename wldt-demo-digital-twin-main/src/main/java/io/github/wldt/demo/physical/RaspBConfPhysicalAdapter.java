package io.github.wldt.demo.physical;

import com.pi4j.io.gpio.digital.*;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RaspBConfPhysicalAdapter extends ConfigurablePhysicalAdapter<RaspBPhysicalAdapterConfiguration> {

    private final static String LED_ON_OFF_PROPERTY_KEY = "LED-property-key";
    private final static String LED_ON_OFF_ACTION_KEY = "set-LED-ON/OFFaction-key";
    
    private final static String LED_PIR_ON_OFF_PROPERTY_KEY = "LED-PIR-property-key";
    private final static String LED_PIR_ON_OFF_ACTION_KEY = "set-LED-PIR-ON/OFFaction-key";
    
    private final static String PIR_EVENT_KEY = "PIR-event-key";

    private final static String LED_OFF_PROPERTY_KEY = "LED-off-property-key";
    private final static String LED_OFF_ACTION_KEY = "set-off-LED-ON/OFFaction-key";
    
    private final static String BUTTON_EVENT_KEY = "BUTTON-event-key";


    public RaspBConfPhysicalAdapter(String id, RaspBPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if (physicalAssetActionWldtEvent != null && getConfiguration().getMapOutput().values().stream().anyMatch(obj -> obj.contains(physicalAssetActionWldtEvent.getActionKey()))) {
                getConfiguration().getMapOutput().forEach((k, v) -> {

                    if(v.contains(physicalAssetActionWldtEvent.getActionKey())) {
                        notifyLedPropertyEvent(physicalAssetActionWldtEvent, (DigitalOutput) v.get(0), (String) v.get(1));
                    }
                });
            }else if (physicalAssetActionWldtEvent != null && getConfiguration().getMapInput().values().stream().anyMatch(obj -> obj.contains(physicalAssetActionWldtEvent.getActionKey()))) {
                getConfiguration().getMapInput().forEach((k, v) -> {
                    if(v.contains(physicalAssetActionWldtEvent.getActionKey())) {
                        //TODO
                    }
                });
            } else {
                System.err.println("[RaspPhysicalAdapter] -> Wrong action received!");
            }


            /*if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(LED_ON_OFF_ACTION_KEY)//Action regarding the ON/OFF property of the Button led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, getConfiguration().getLed(), LED_ON_OFF_PROPERTY_KEY);
            } else if (physicalAssetActionWldtEvent != null
                    && physicalAssetActionWldtEvent.getActionKey().equals(LED_PIR_ON_OFF_ACTION_KEY) //Action regarding the ON/OFF property of the green PIR led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, getConfiguration().getLed_Pir(), LED_PIR_ON_OFF_PROPERTY_KEY);
            } else if (physicalAssetActionWldtEvent != null
                        && physicalAssetActionWldtEvent.getActionKey().equals(LED_OFF_ACTION_KEY) //Action regarding the ON/OFF property of the red PIR led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, getConfiguration().getLedOff(), LED_OFF_PROPERTY_KEY);
            } else {
                System.err.println("[RaspPhysicalAdapter] -> Wrong action received!");
            }*/

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onAdapterStart() {
        try {
            new Thread(publishPhysicalAssetDescription()).start();
            new Thread(deviceEmulation()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onAdapterStop() {

    }

    private Runnable publishPhysicalAssetDescription() {
        return () -> {
            try {
                System.out.println("[RaspPhysicalAdapter] -> Sleeping before Publishing Physical Asset Description...");
                Thread.sleep(2000); //sleep di 2 secondi
                System.out.println("[RaspPhysicalAdapter] -> Publishing Physical Asset Description...");
                this.notifyPhysicalAdapterBound(getConfiguration().createPhysicalAssetDescription());
            } catch(Exception e) {
                e.printStackTrace();
            }
        };
    }

    private Runnable deviceEmulation() {
        return () -> {
            try{
                System.out.println("[RaspPhysicalAdapter] -> Sleeping before Starting PI...");
                Thread.sleep(10000);//emulation of startup time
                System.out.println("[RaspPhysicalAdapter] -> Starting physical device (PI)...");
                System.out.println("[RaspPhysicalAdapter] -> Printing PI4J Registry of Sensors:");
                System.out.println(getConfiguration().getPI4J().registry().all() + "\n");

                this.addListenerButton(getConfiguration().getInputSensorByName("BUTTON"), getConfiguration().getSensorEvent("BUTTON"));
                this.addListenerPir(getConfiguration().getInputSensorByName("PIR"), getConfiguration().getSensorEvent("PIR"));
                
                //pi4j.shutdown();
                
            }catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private void addListenerButton(DigitalInput button, String event){
        button.addListener(s -> {
            try{
                if (s.state() == DigitalState.LOW) {
                    System.out.println("BUTTON PRESSED");
                    publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(event, "Pressed"));
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addListenerPir(DigitalInput pir, String event) {
        pir.addListener(s -> {
            try{
                if (s.state() == DigitalState.LOW) {
                    System.out.println("MOVEMENT DETECTED");
                    publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(event, "Moved"));
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void notifyLedPropertyEvent(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent, DigitalOutput led, String PROPERTY_KEY){
        try {
            System.out.println("[RaspPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                    + "with Body: " + physicalAssetActionWldtEvent.getBody() + "\n");
            if (physicalAssetActionWldtEvent.getBody().equals(1)) {
                led.high();
                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(PROPERTY_KEY, 1);
                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
            } else {
                led.low();
                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(PROPERTY_KEY, 0);
                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*private void publishPhysicalRelationshipInstance(){
        try{
            String relatoinshipTarget = "building-hq";
            Map<String, Object> relationshipMetadata = new HashMap<>();
            relationshipMetadata.put("floor", "f0");
            relationshipMetadata.put("room", "r0");
            PhysicalAssetRelationshipInstance<String> relInstance = this.insideInRelationship.createRelationshipInstance(relatoinshipTarget, relationshipMetadata);
            PhysicalAssetRelationshipInstanceCreatedWldtEvent<String> relInstanceEvent = new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(relInstance);
            publishPhysicalAssetRelationshipCreatedWldtEvent(relInstanceEvent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }*/
}
