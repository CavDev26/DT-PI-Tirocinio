package io.github.wldt.demo.physical;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.Pi4J;


public class RaspBConfPhysicalAdapter extends ConfigurablePhysicalAdapter<RaspBPhysicalAdapterConfiguration> {

    private final static String LED_ON_OFF_PROPERTY_KEY = "LED-property-key";
    private final static String LED_ON_OFF_ACTION_KEY = "set-LED-ON/OFFaction-key";
    
    private final static String LED_PIR_ON_OFF_PROPERTY_KEY = "LED-PIR-property-key";
    private final static String LED_PIR_ON_OFF_ACTION_KEY = "set-LED-PIR-ON/OFFaction-key";
    
    private final static String PIR_EVENT_KEY = "PIR-event-key";

    private final static String LED_OFF_PROPERTY_KEY = "LED-off-property-key";
    private final static String LED_OFF_ACTION_KEY = "set-off-LED-ON/OFFaction-key";
    
    private final static String BUTTON_EVENT_KEY = "BUTTON-event-key";




    //Context pi4j = Pi4J.newAutoContext();

    private final static Context pi4j = Pi4J.newAutoContext();


    private DigitalInput createAndConfigDigitalInputPI4j(String id, String SensorName, int PIN) {
        DigitalInputConfigBuilder builder = DigitalInput.newConfigBuilder(pi4j)
                .id(id)
                .name(SensorName)
                .address(PIN)
                .provider("pigpio-digital-input");
       return  pi4j.create(builder);
    }
    private DigitalOutput createAndConfigDigitalOutputPI4j(String id, String SensorName, int PIN) {
        DigitalOutputConfigBuilder builder = DigitalOutput.newConfigBuilder(pi4j)
                .id(id)
                .name(SensorName)
                .address(PIN)
                .provider("pigpio-digital-output");
        return pi4j.create(builder);
    }

    DigitalInput pir = createAndConfigDigitalInputPI4j("PIR", "Pir",getConfiguration().getPin_pir());
    DigitalInput button = createAndConfigDigitalInputPI4j("BUTTON", "Button", getConfiguration().getPin_button());
    DigitalOutput led_Pir = createAndConfigDigitalOutputPI4j("LED-PIR", "LED-PIR", getConfiguration().getPin_led_pir());
    DigitalOutput ledOff = createAndConfigDigitalOutputPI4j("LED-OFF", "LED-OFF", getConfiguration().getPin_led_off());
    DigitalOutput led = createAndConfigDigitalOutputPI4j("LED-ON", "LED-ON", getConfiguration().getPin_led());

    /*DigitalInputConfigBuilder pirConfig = DigitalInput.newConfigBuilder(pi4j)
            .id("PIR")
            .name("Pir-mov")
            .address(getConfiguration().getPin_pir())
            .provider("pigpio-digital-input");
    DigitalInput pir = pi4j.create(pirConfig);*/

    /*DigitalOutputConfigBuilder ledConfigPir = DigitalOutput.newConfigBuilder(pi4j)
            .id("ledPir")
            .name("LED Flasher-Pir")
            .address(getConfiguration().getPin_led_pir())
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.LOW)
            .provider("pigpio-digital-output");
    DigitalOutput led_Pir = pi4j.create(ledConfigPir);*/

    /*DigitalOutputConfigBuilder ledOFFConfig = DigitalOutput.newConfigBuilder(pi4j)
            .id("ledR")
            .name("LED-Flasher-OFF")
            .address(getConfiguration().getPin_led_off())
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.HIGH)
            .provider("pigpio-digital-output");
    DigitalOutput ledOff = pi4j.create(ledOFFConfig);*/

    /*DigitalOutputConfigBuilder ledConfig = DigitalOutput.newConfigBuilder(pi4j)
            .id("led")
            .name("LED Flasher")
            .address(getConfiguration().getPin_led())
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.LOW)
            .provider("pigpio-digital-output");
    DigitalOutput led = pi4j.create(ledConfig);*/

    /*DigitalInputConfigBuilder buttonConfig = DigitalInput.newConfigBuilder(pi4j)
            .id("BUTTON")
            .name("Button-attuator")
            .address(getConfiguration().getPin_button())
            .provider("pigpio-digital-input");
    DigitalInput button = pi4j.create(buttonConfig);*/

    private PhysicalAssetRelationship<String> insideInRelationship = null;

    public RaspBConfPhysicalAdapter(String id, RaspBPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(LED_ON_OFF_ACTION_KEY)//Action regarding the ON/OFF property of the Button led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, led, LED_ON_OFF_PROPERTY_KEY);
            } else if (physicalAssetActionWldtEvent != null
                    && physicalAssetActionWldtEvent.getActionKey().equals(LED_PIR_ON_OFF_ACTION_KEY) //Action regarding the ON/OFF property of the green PIR led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, led_Pir, LED_PIR_ON_OFF_PROPERTY_KEY);
            } else if (physicalAssetActionWldtEvent != null
                        && physicalAssetActionWldtEvent.getActionKey().equals(LED_OFF_ACTION_KEY) //Action regarding the ON/OFF property of the red PIR led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, ledOff, LED_OFF_PROPERTY_KEY);
            } else {
                System.err.println("[RaspPhysicalAdapter] -> Wrong action received!");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void notifyLedPropertyEvent(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent,DigitalOutput led, String PROPERTY_KEY){
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

                PhysicalAssetProperty<Integer> LEDProperty = new PhysicalAssetProperty<>(LED_ON_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDProperty);
                PhysicalAssetAction setONOFFLEDAction = new PhysicalAssetAction(LED_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setONOFFLEDAction);
                
                PhysicalAssetProperty<Integer> LEDOFFProperty = new PhysicalAssetProperty<>(LED_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDOFFProperty);
                PhysicalAssetAction setOFFLEDAction = new PhysicalAssetAction(LED_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setOFFLEDAction);
                
                PhysicalAssetProperty<Integer> LEDpirProperty = new PhysicalAssetProperty<>(LED_PIR_ON_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDpirProperty);
                PhysicalAssetAction setONOFFLEDPIRAction = new PhysicalAssetAction(LED_PIR_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setONOFFLEDPIRAction);

                PhysicalAssetEvent ButtonEvent = new PhysicalAssetEvent(BUTTON_EVENT_KEY, "text/plain");
                pad.getEvents().add(ButtonEvent);
                PhysicalAssetEvent PIREvent = new PhysicalAssetEvent(PIR_EVENT_KEY, "text/plain");
                pad.getEvents().add(PIREvent);
                
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
                            System.out.println("MOVEMENT DETECTED");
                            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(PIR_EVENT_KEY, "Moved"));
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                button.addListener(s -> {
                    try{
                        if (s.state() == DigitalState.LOW) {
                            System.out.println("BUTTON PRESSED");
                            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(BUTTON_EVENT_KEY, "Pressed"));
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
