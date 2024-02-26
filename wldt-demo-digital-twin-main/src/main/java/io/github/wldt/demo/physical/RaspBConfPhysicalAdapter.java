package io.github.wldt.demo.physical;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;

//import java.io.Serial;
import java.rmi.registry.Registry;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.Pi4J;


public class RaspBConfPhysicalAdapter extends ConfigurablePhysicalAdapter<RaspBPhysicalAdapterConfiguration> {

    private final static String LED_ON_OFF_PROPERTY_KEY = "LED-property-key";
    private final static String LED_ON_OFF_EVENT_KEY = "LED-action-key";
    private final static String LED_ON_OFF_ACTION_KEY = "set-LED-ON/OFFaction-key"; //??
    
    private final static String LED_PIR_ON_OFF_PROPERTY_KEY = "LED-PIR-property-key";
    private final static String LED_PIR_ON_OFF_EVENT_KEY = "LED-PIR-action-key";
    private final static String LED_PIR_ON_OFF_ACTION_KEY = "set-LED-PIR-ON/OFFaction-key";//??
    
    private final static String PIR_PROPERTY_KEY = "PIR-property-key";
    private final static String PIR_EVENT_KEY = "PIR-event-key";
    private final static String PIR_ACTION_KEY = "set-PIR-action-key"; //??
    
    private final static String LED_OFF_PROPERTY_KEY = "LED-off-property-key";
    private final static String LED_OFF_EVENT_KEY = "LED-off-action-key";
    private final static String LED_OFF_ACTION_KEY = "set-off-LED-ON/OFFaction-key"; //??
    
    //private final static String BUTTON_PROPERTY_KEY = "BUTTON-property-key";
    private final static String BUTTON_EVENT_KEY = "BUTTON-event-key";
    //private final static String BUTTON_ACTION_KEY = "set-BUTTON-action-key"; //??
    
    private static int pressCount = 0;
    
    private static final int PIN_LED = 27; //PIN 13 = BCM 27
    private static final int PIN_PIR = 4; //PIN 7 = BCM 4
    private static final int PIN_LED_PIR = 17; //PIN 11 = BCM 17
    private static final int PIN_LED_OFF = 23; //PIN 16 = BCM 23
    private static final int PIN_BUTTON = 22; //PIN 15 = BCM 22



    Context pi4j = Pi4J.newAutoContext();

    DigitalInputConfigBuilder pirConfig = DigitalInput.newConfigBuilder(pi4j)
            .id("PIR")
            .name("Pir-mov")
            .address(PIN_PIR)
            .provider("pigpio-digital-input");
    DigitalInput pir = pi4j.create(pirConfig);

    DigitalOutputConfigBuilder ledConfigPir = DigitalOutput.newConfigBuilder(pi4j)
            .id("ledPir")
            .name("LED Flasher-Pir")
            .address(PIN_LED_PIR)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.LOW)
            .provider("pigpio-digital-output");
    DigitalOutput led_Pir = pi4j.create(ledConfigPir);

    DigitalOutputConfigBuilder ledOFFConfig = DigitalOutput.newConfigBuilder(pi4j)
            .id("ledR")
            .name("LED-Flasher-OFF")
            .address(PIN_LED_OFF)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.HIGH)
            .provider("pigpio-digital-output");
    DigitalOutput ledOff = pi4j.create(ledOFFConfig);

    DigitalOutputConfigBuilder ledConfig = DigitalOutput.newConfigBuilder(pi4j)
            .id("led")
            .name("LED Flasher")
            .address(PIN_LED)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.LOW)
            .provider("pigpio-digital-output");
    DigitalOutput led = pi4j.create(ledConfig);

    DigitalInputConfigBuilder buttonConfig = DigitalInput.newConfigBuilder(pi4j)
            .id("BUTTON")
            .name("Button-attuator")
            .address(PIN_BUTTON)
            .provider("pigpio-digital-input");
    DigitalInput button = pi4j.create(buttonConfig);

    private PhysicalAssetRelationship<String> insideInRelationship = null;

    public RaspBConfPhysicalAdapter(String id, RaspBPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    //METODO CHE SPECIFICA COSA FARE NON APPENA SI RICEVE UN'AZIONE (Un'azione è intesa come una richiesta dal device Digitale di modificare qualcosa)
    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(LED_ON_OFF_ACTION_KEY)//Action regarding the ON/OFF property of the Button led
                ) {
                System.out.println("[RaspPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                + "with Body: " + physicalAssetActionWldtEvent.getBody() + "\n");
                if (physicalAssetActionWldtEvent.getBody().equals(1)) {
                    led.high();
                    PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, 1);
                    publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                } else {
                    led.low();
                    PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, 0);
                    publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                }
            } else if (physicalAssetActionWldtEvent != null
                    && physicalAssetActionWldtEvent.getActionKey().equals(LED_PIR_ON_OFF_ACTION_KEY) //Action regarding the ON/OFF property of the PIR led
                ) {
                    System.out.println("[RaspPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                            + "with Body: " + physicalAssetActionWldtEvent.getBody() + "\n");
                    if (physicalAssetActionWldtEvent.getBody().equals(1)) {
                        led.high();
                        PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 1);
                        publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                    } else {
                        led.low();
                        PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 0);
                        publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                    }
            } else {
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
                
                PhysicalAssetProperty<Integer> LEDOFFProperty = new PhysicalAssetProperty<>(LED_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDOFFProperty);
                PhysicalAssetEvent turningOFFEvent = new PhysicalAssetEvent(LED_OFF_EVENT_KEY, "text/plain");
                pad.getEvents().add(turningOFFEvent);
                PhysicalAssetAction setOFFLEDAction = new PhysicalAssetAction(LED_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setOFFLEDAction);
                
                PhysicalAssetProperty<Integer> LEDpirProperty = new PhysicalAssetProperty<>(LED_PIR_ON_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDpirProperty);
                PhysicalAssetEvent turningON_OFF_PIR_LEDEvent = new PhysicalAssetEvent(LED_PIR_ON_OFF_EVENT_KEY, "text/plain");
                pad.getEvents().add(turningON_OFF_PIR_LEDEvent);
                PhysicalAssetAction setONOFFLEDPIRAction = new PhysicalAssetAction(LED_PIR_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setONOFFLEDPIRAction);

                PhysicalAssetEvent ButtonEvent = new PhysicalAssetEvent(BUTTON_EVENT_KEY, "text/plain");
                pad.getEvents().add(ButtonEvent);

                
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
                System.out.println("[RaspPhysicalAdapter] -> Sleeping before Starting PI...");
                Thread.sleep(10000);//emulation of startup time
                System.out.println("[RaspPhysicalAdapter] -> Starting physical device (PI)...");

                pir.addListener(s -> {
                    try{
                        if (s.state() == DigitalState.LOW) {
                            System.out.println("You moved");

                            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(PIR_EVENT_KEY, "Moved"));

                            /*led_Pir.high();
                            PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 1);
                            publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                            Thread.sleep(500);
                            led_Pir.low();
                            PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEventP = new PhysicalAssetPropertyWldtEvent<>(LED_PIR_ON_OFF_PROPERTY_KEY, 0);
                            publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEventP);*/
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                button.addListener(s -> {
                    try{
                        if (s.state() == DigitalState.LOW) {
                            System.out.println("BUTTON PRESSED");

                            //Tentativo di gestione pressione bottone tramite Evento

                            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(BUTTON_EVENT_KEY, "Pressed"));

                            /*if (led.equals(DigitalState.HIGH)) {
                                led.low();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, 0);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                                ledOff.high();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent2 = new PhysicalAssetPropertyWldtEvent<>(LED_OFF_PROPERTY_KEY, 1);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent2);
                            } else {
                                led.high();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, 1);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                                ledOff.low();
                                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent2 = new PhysicalAssetPropertyWldtEvent<>(LED_OFF_PROPERTY_KEY, 0);
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent2);
                            }*/
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                //pi4j.shutdown();
                
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
