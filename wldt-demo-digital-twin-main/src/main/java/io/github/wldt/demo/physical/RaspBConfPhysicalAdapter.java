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
import com.pi4j.util.Console;

import ch.qos.logback.core.joran.conditional.IfAction;



public class RaspBConfPhysicalAdapter extends ConfigurablePhysicalAdapter<RaspBPhysicalAdapterConfiguration> {

    /*private final static String ON_OFF_PROPERTY_KEY = "ON/OFF-property-key";
    private final static String ON_OFF_EVENT_KEY = "ON/OFF-action-key";
    private final static String SET_ON_OFF_ACTION_KEY = "set-ON/OFF-action-key";*/

    private final static String LED_ON_OFF_PROPERTY_KEY = "LED-property-key";
    private final static String LED_ON_OFF_EVENT_KEY = "LED-action-key";
    private final static String LED_ON_OFF_ACTION_KEY = "set-LED-ON/OFFaction-key";
    
    private static int pressCount = 0;
    
    private static final int PIN_LED = 27; // PIN 15 = BCM 22 //PIN 13 = GPIO27



    private PhysicalAssetRelationship<String> insideInRelationship = null;

    public RaspBConfPhysicalAdapter(String id, RaspBPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    //METODO CHE SPECIFICA COSA FARE NON APPENA SI RICEVE UN'AZIONE
    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(LED_ON_OFF_ACTION_KEY)
                ) {
                //potrebbe essere utile uno switch case(?)
                //specificare cosa succede quando ricevo questa azione
                System.out.println("[RaspDemoPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                + "with Body: " + physicalAssetActionWldtEvent.getBody());
            }
            else { //altre azini possibili, magari da gestire con uno switch
                System.err.println("[RaspDemoPhysicalAdapter] -> Wrong action received!");
            }


            //ORIGINAL
            /*if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(SET_ON_OFF_ACTION_KEY)
                ) {
                //potrebbe essere utile uno switch case(?)
                //specificare cosa succede quando ricevo questa azione
                System.out.println("[RaspDemoPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                + "with Body: " + physicalAssetActionWldtEvent.getBody());
            }
            else { //altre azini possibili, magari da gestire con uno switch
                System.err.println("[RaspDemoPhysicalAdapter] -> Wrong action received!");
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

    //PUBBLICA LA DESCRIPTION DEL PHYSICAL ADAPTER, CON TUTTE LE SUE PROPRIETA' E AZIONI CHE PUO' SVOLGERE
    private Runnable publishPhysicalAssetDescription() {
        return () -> {
            try {
                System.out.println("[DemoRaspPhysicalAdapter] -> Sleeping before Publishing Physical Asset Description...");
                Thread.sleep(2000); //sleep di 2 secondi
                System.out.println("[DemoRaspPhysicalAdapter] -> Publishing Physical Asset Description...");
                PhysicalAssetDescription pad = new PhysicalAssetDescription();

                //Add a new Property associated to the target PAD with a key and a default value
                //Add the declaration of a new type of generated event associated to a event key
                //and the content type of the generated payload
                //Declare the availability of a target action characterized by a key, an action type
                //and the expected content type and the request body

                //valore di default della property = 0
                PhysicalAssetProperty<Integer> LEDProperty = new PhysicalAssetProperty<>(LED_ON_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(LEDProperty);
                PhysicalAssetEvent turningON_OFFEvent = new PhysicalAssetEvent(LED_ON_OFF_EVENT_KEY, "text/plain");
                pad.getEvents().add(turningON_OFFEvent);
                PhysicalAssetAction setONOFFLEDAction = new PhysicalAssetAction(LED_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setONOFFLEDAction);

                //original
                /*PhysicalAssetProperty<Integer> ONOFFProperty = new PhysicalAssetProperty<>(ON_OFF_PROPERTY_KEY, 0);
                pad.getProperties().add(ONOFFProperty);
                PhysicalAssetEvent turningON_OFFEvent = new PhysicalAssetEvent(ON_OFF_EVENT_KEY, "text/plain");
                pad.getEvents().add(turningON_OFFEvent);
                PhysicalAssetAction setONOFFAction = new PhysicalAssetAction(SET_ON_OFF_ACTION_KEY, "ON/OFF.actuation", "text/plain");
                pad.getActions().add(setONOFFAction);*/
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
        //QUI SI DOVRA' FARE IL COLLEGAMENTO COL RASPBERRY PER RICEVERE I DATI VERI E PROPRI
        return () -> {
            try{
                
                var pi4j = Pi4J.newAutoContext();

                Scanner scanner = new Scanner(System.in);
                
                System.out.println("[DemoRaspPhysicalAdapter] -> Sleeping before Starting emulation...");
                Thread.sleep(10000);
                System.out.println("[DemoRaspPhysicalAdapter] -> Starting physical device emulation...");

                //Evento per una condizione di base, se può cambiare
                publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(LED_ON_OFF_EVENT_KEY, "OFF"));

                //publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(ON_OFF_EVENT_KEY, "OFF"));
                //simulazione di ricezione di valori per generazione di eventi


                var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("LED Flasher")
                .address(PIN_LED)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
                
                var led = pi4j.create(ledConfig);
        
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
                
                
                /*int acceso = 0;
                int cambiato = 1;
                for(int i = 0; i < getConfiguration().getMessageUpdateNumber(); i++){
                    Thread.sleep(getConfiguration().getMessageUpdateTime());
                        
                        if(led.equals(DigitalState.LOW)){
                            if (acceso == 1){
                                acceso = 0;
                                cambiato = 1;
                            } else {
                                acceso = 0;
                                cambiato = 0;
                            }
                        } else{
                            if (acceso == 0){
                                acceso = 1;
                                cambiato = 1;
                            } else {
                                acceso = 1;
                                cambiato = 0;
                            }
                        }
                        if (cambiato == 1){
                            PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, acceso);
                            publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                        }

                    /*if (cambiato == 1) {
                        if(led.equals(DigitalState.LOW)){
                            if (acceso == 1){
                                acceso = 0;
                                cambiato = 1;
                            } else {
                                acceso = 0;
                            }
                        } else{
                            if (acceso == 0){
                                acceso = 1;
                                cambiato = 1;
                            } else {
                                acceso = 1;
                            }
                        }
                        PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, acceso);
                        publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                        }*/


                    /*System.out.println("Daaaai sto aspettanto che mi scrivi qualcosaaaaa");
                    int randomON_OFF = Integer.parseInt(scanner.nextLine());

                    PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(LED_ON_OFF_PROPERTY_KEY, randomON_OFF);
                    publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);*/
                //}
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
