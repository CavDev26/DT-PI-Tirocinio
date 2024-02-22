package io.github.wldt.demo.physical;

import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;

//import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.util.Console;

import ch.qos.logback.core.joran.conditional.IfAction;



public class RaspBConfPhysicalAdapter extends ConfigurablePhysicalAdapter<RaspBPhysicalAdapterConfiguration> {

    private final static String LED_ON_OFF_PROPERTY_KEY = "LED-property-key";
    private final static String LED_ON_OFF_EVENT_KEY = "LED-action-key";
    private final static String LED_ON_OFF_ACTION_KEY = "set-LED-ON/OFFaction-key";
    
    private final static String LED_PIR_ON_OFF_PROPERTY_KEY = "LED-PIR-property-key";
    private final static String LED_PIR_ON_OFF_EVENT_KEY = "LED-PIR-action-key";
    private final static String LED_PIR_ON_OFF_ACTION_KEY = "set-LED-PIR-ON/OFFaction-key";
    
    private final static String PIR_PROPERTY_KEY = "PIR-property-key";
    private final static String PIR_EVENT_KEY = "PIR-action-key";
    private final static String PIR_ACTION_KEY = "set-PIR-action-key"; // serve?
    
    
    private static int pressCount = 0;
    
    private static final int PIN_LED = 27; //PIN 13 = BCM 27
    private static final int PIN_PIR = 4; //PIN 7 = BCM 4
    private static final int PIN_LED_PIR = 17; //PIN 11 = BCM 17



    private PhysicalAssetRelationship<String> insideInRelationship = null;

    public RaspBConfPhysicalAdapter(String id, RaspBPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    //METODO CHE SPECIFICA COSA FARE NON APPENA SI RICEVE UN'AZIONE (Un'azione è intesa come una richiesta dal device fisico di modificare qualcosa)
    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(LED_ON_OFF_ACTION_KEY)
                ) {

                //specificare cosa succede quando ricevo questa azione
                System.out.println("[RaspPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                + "with Body: " + physicalAssetActionWldtEvent.getBody() + "\n");
            }
            else {
                System.err.println("[RaspPhysicalAdapter] -> Wrong action received!");
            }

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
                PhysicalAssetDescription pad = new PhysicalAssetDescription();

                //Add a new Property associated to the target PAD with a key and a default value
                //Add the declaration of a new type of generated event associated to a event key
                //and the content type of the generated payload
                //Declare the availability of a target action characterized by a key, an action type
                //and the expected content type and the request body

                //valore di default della LED property = 0
                PhysicalAssetProperty<Integer> LEDProperty = new PhysicalAssetProperty<>(LED_ON_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDProperty);
                PhysicalAssetEvent turningON_OFFEvent = new PhysicalAssetEvent(LED_ON_OFF_EVENT_KEY, "text/plain");
                pad.getEvents().add(turningON_OFFEvent);
                PhysicalAssetAction setONOFFLEDAction = new PhysicalAssetAction(LED_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setONOFFLEDAction);
                
                PhysicalAssetProperty<Integer> LEDpirProperty = new PhysicalAssetProperty<>(LED_PIR_ON_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDpirProperty);
                PhysicalAssetEvent turningON_OFF_PIR_LEDEvent = new PhysicalAssetEvent(LED_PIR_ON_OFF_EVENT_KEY, "text/plain");
                pad.getEvents().add(turningON_OFF_PIR_LEDEvent);
                PhysicalAssetAction setONOFFLEDPIRAction = new PhysicalAssetAction(LED_PIR_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setONOFFLEDPIRAction);
                
                //create Test relationship to describe that the Physical Device is inside a building
                this.insideInRelationship = new PhysicalAssetRelationship<>("insideId");
                pad.getRelationships().add(insideInRelationship);

                //notify the new pad to the DT's Shadowing Function
                this.notifyPhysicalAdapterBound(pad);

            } catch(Exception e) {
                e.printStackTrace();
            }
        };
    }

    private Runnable deviceEmulation() {
        return () -> {
            try{
                
                var pi4j = Pi4J.newAutoContext();

                Scanner scanner = new Scanner(System.in);
                
                System.out.println("[RaspPhysicalAdapter] -> Sleeping before Starting PI...");
                Thread.sleep(10000);//emulation of startup time
                System.out.println("[RaspPhysicalAdapter] -> Starting physical device (PI)...");
    
                var pirConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("PIR")
                .name("Pir-mov")
                .address(PIN_PIR)
                .provider("pigpio-digital-input");
                var pir = pi4j.create(pirConfig);
                
                var ledConfigPir = DigitalOutput.newConfigBuilder(pi4j)
                .id("ledPir")
                .name("LED Flasher-Pir")
                .address(PIN_LED_PIR)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
                var led_Pir = pi4j.create(ledConfigPir);

                var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("LED Flasher")
                .address(PIN_LED)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
                var led = pi4j.create(ledConfig);
                
                pir.addListener(s -> {
                    try{
                        if (s.state() == DigitalState.LOW) {
                            System.out.println("You moved");
                            led_Pir.high();
                            PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 1);
                            publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                            Thread.sleep(500);
                            led_Pir.low();
                            PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEventP = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 0);
                            publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEventP);
                            
                            /* (led_Pir.equals(DigitalState.HIGH)) {
                                System.out.println("LED low");
                                led_Pir.low();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 0);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                            } else {
                                System.out.println("LED high");
                                led_Pir.high();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 1);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                            }*/
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                    
                while (pressCount < 5) {

                    System.out.println("scrivi 1 per accendere/spegnere il led:");
                    
                    int buttonInt = Integer.parseInt(scanner.nextLine());
                    
                    if (buttonInt == 1){
                        if (led.equals(DigitalState.HIGH)) {
                                System.out.println("LED low");
                                led.low();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, 0);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                        } else {
                                System.out.println("LED high");
                                led.high();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, 1);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                        }
                    }
                }
                
            }catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private void publishPhysicalRelationshipInstance(){
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
    }
}