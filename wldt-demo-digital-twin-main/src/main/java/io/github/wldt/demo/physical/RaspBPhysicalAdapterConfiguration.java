package io.github.wldt.demo.physical;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.Pi4J;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RaspBPhysicalAdapterConfiguration {


    private static final int MAXIMUM_EVENTS = 10;
    private static final int PIN_LED = 27; //PIN 13 = BCM 27
    private static final int PIN_PIR = 4; //PIN 7 = BCM 4
    private static final int PIN_LED_PIR = 17; //PIN 11 = BCM 17
    private static final int PIN_LED_OFF = 23; //PIN 16 = BCM 23
    private static final int PIN_BUTTON = 22; //PIN 15 = BCM 22

    private final static String LED_ON_OFF_PROPERTY_KEY = "LED-property-key";
    private final static String LED_ON_OFF_ACTION_KEY = "set-LED-ON/OFFaction-key";

    private final static String LED_PIR_ON_OFF_PROPERTY_KEY = "LED-PIR-property-key";
    private final static String LED_PIR_ON_OFF_ACTION_KEY = "set-LED-PIR-ON/OFFaction-key";

    private final static String PIR_EVENT_KEY = "PIR-event-key";

    private final static String LED_OFF_PROPERTY_KEY = "LED-off-property-key";
    private final static String LED_OFF_ACTION_KEY = "set-off-LED-ON/OFFaction-key";

    private final static String BUTTON_EVENT_KEY = "BUTTON-event-key";

    private DigitalInput pir = createAndConfigDigitalInputPI4j("PIR", "PIR", PIN_PIR, 0L);
    private DigitalInput button = createAndConfigDigitalInputPI4j("BUTTON", "BUTTON", PIN_BUTTON, 3000L);
    private DigitalOutput led_Pir = createAndConfigDigitalOutputPI4j("LED-PIR", "LED-PIR", PIN_LED_PIR, DigitalState.LOW, DigitalState.LOW);
    private DigitalOutput ledOff = createAndConfigDigitalOutputPI4j("LED-OFF", "LED-OFF", PIN_LED_OFF, DigitalState.LOW, DigitalState.HIGH);
    private DigitalOutput led = createAndConfigDigitalOutputPI4j("LED-ON", "LED-ON", PIN_LED, DigitalState.LOW, DigitalState.LOW);


    /**
     * Declaration of the internal data structures
     */
    private final static Context pi4j = Pi4J.newAutoContext();
    private Map<String, ArrayList<?>> mapOutput = new HashMap<>();
    private Map<String, ArrayList<?>> mapInput = new HashMap<>();
    private ArrayList<String> events = new ArrayList<>();


    //TODO debounce
    //TODO stampa incorretta di button pressed e movement detected
    //TODO togliere sleep e magari ciclare per numero di eventi

    //Questo to do potrebbe dare dei problemi
    //TODO enum dei pin, così ognuno non deve guardarsi su internet che pin corrispondono, magri linko l'immagine o il sito dove vedere le properie board

    /**
     * A function that creates a new instance of a pi4j input sensor
     * @param id Identifier of the sensor.
     * @param SensorName Name of the Sensor.
     * @param PIN Pin to which the sensor is linked to.
     * @param debounceVal Specify a Long Literal (add L at end of value) as a debounce value; could be zero if not a button.
     * @return A new DigitalInput given the specified values.
     */
    private DigitalInput createAndConfigDigitalInputPI4j(String id, String SensorName, int PIN, Long debounceVal) {
        DigitalInputConfigBuilder builder = DigitalInput.newConfigBuilder(pi4j)
                .id(id)
                .name(SensorName)
                .address(PIN)
                .debounce(debounceVal)
                .provider("pigpio-digital-input");
        return pi4j.create(builder);
    }

    /**
     *
     * @param id
     * @param SensorName
     * @param PIN
     * @param shutdownState
     * @param initialState
     * @return A new DigitalOutput given the specified values.
     */
    private DigitalOutput createAndConfigDigitalOutputPI4j(String id, String SensorName, int PIN, DigitalState shutdownState, DigitalState initialState) {
        DigitalOutputConfigBuilder builder = DigitalOutput.newConfigBuilder(pi4j)
                .id(id)
                .name(SensorName)
                .address(PIN)
                .shutdown(shutdownState)
                .initial(initialState)
                .provider("pigpio-digital-output");
        return pi4j.create(builder);
    }


    /**
     * A function that permits to specify all properties, events and action Keys.
     * @return a new instance of PhysicalAssetDescription containing all the specified properties, events, actions.
     */
    public PhysicalAssetDescription createPhysicalAssetDescription(){

        //TODO Generalize behaviour, with a function that takes all the values as input and fullfills the pad, without needing to implement all the code below.
        PhysicalAssetDescription pad = new PhysicalAssetDescription();
        //TODO fix the insideInRelationShip situation.
        PhysicalAssetRelationship<String> insideInRelationship = null;

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

        insideInRelationship = new PhysicalAssetRelationship<>("insideId");
        pad.getRelationships().add(insideInRelationship);

        return pad;
    }

    /**
     * A Function that creates a new entry in the Output Devices Map given Name (Key) and [digitalOutputSensor, propertyKey, actionKey] (Value)
     * @param Name Key for the map - Name identifier of the output device and its values
     * @param digitalOutputSensor DigitalOutput device
     * @param propertyKey propertyKey linked to the output sensor
     * @param actionKey actionKey linked to the output device
     */
    private void createOutputEntrySensor(String Name, DigitalOutput digitalOutputSensor, String propertyKey, String actionKey ) {
        this.mapOutput.put(Name, new ArrayList<>() {{
            add(digitalOutputSensor);
            add(propertyKey);
            add(actionKey);
        }});
    }
    /**
     * A Function that creates a new entry in the Input Sensors Map given Name (Key) and [digitalInputSensor, eventKey] (Value)
     * @param Name Key for the map - Name identifier of the sensor and its values.
     * @param digitalInputSensor DigitalInput sensor.
     * @param eventKey EventKey linked to the input sensor.
     * @param actionKey ActionKey linked to the input sensor.
     * @param type specify the sensor type from the supported ones.
     */
    private void createInputEntrySensor(String Name, DigitalInput digitalInputSensor, String eventKey, String actionKey, sensorType type) {
        this.mapInput.put(Name, new ArrayList<>() {{
            add(digitalInputSensor);
            add(eventKey);
            add(actionKey);
            add(type);
        }});
    }

    /**
     * An enum that rapresets all the Input sensors types that are supprted.
     */
    private enum sensorType {
        BUTTON,
        PIR
    }

    /**
     *
     */
    private void fullFillMaps() {

        this.createOutputEntrySensor(led_Pir.name(), led_Pir, LED_PIR_ON_OFF_PROPERTY_KEY, LED_PIR_ON_OFF_ACTION_KEY);
        this.createOutputEntrySensor(ledOff.name(), ledOff, LED_OFF_PROPERTY_KEY, LED_OFF_ACTION_KEY);
        this.createOutputEntrySensor(led.name(), led, LED_ON_OFF_PROPERTY_KEY, LED_ON_OFF_ACTION_KEY);

        this.createInputEntrySensor(pir.name(), pir, PIR_EVENT_KEY, null, sensorType.PIR);
        this.createInputEntrySensor(button.name(), button, BUTTON_EVENT_KEY, null, sensorType.BUTTON);
        //map containing name as key; (sensor, Property, action) as value
        /*this.mapOutput.put(led_Pir.name(), new ArrayList<>() {{
            add(led_Pir);
            add(LED_PIR_ON_OFF_PROPERTY_KEY);
            add(LED_PIR_ON_OFF_ACTION_KEY);
        }});

        this.mapOutput.put(ledOff.name(), new ArrayList<>() {{
            add(ledOff);
            add(LED_OFF_PROPERTY_KEY);
            add(LED_OFF_ACTION_KEY);
        }});
        this.mapOutput.put(led.name(), new ArrayList<>() {{
            add(led);
            add(LED_ON_OFF_PROPERTY_KEY);
            add(LED_ON_OFF_ACTION_KEY);
        }});*/

        /*this.mapInput.put(pir.name(), new ArrayList<>() {{
            add(pir);
            add(PIR_EVENT_KEY);
        }});
        this.mapInput.put(button.name(), new ArrayList<>() {{
            add(button);
            add(BUTTON_EVENT_KEY);
        }});*/

        //this.mapOutput.put(led_Pir.name(), led_Pir);
        //this.mapOutput.put(ledOff.name(), ledOff);
        //this.mapOutput.put(led.name(), led);

        //this.mapInput.put(pir.name(), pir);
        //this.mapInput.put(button.name(), button);
    }

    private void addListenerButton(DigitalInput button, String event){
        button.addListener(s -> {
            try{
                if (s.state() == DigitalState.LOW) {
                    if (!this.events.contains(event)) {
                        System.out.println("\nBUTTON PRESSED\n");
                        this.events.add(event);
                    }
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
                    if (!this.events.contains(event)) {
                        System.out.println("\nMOVEMENT DETECTED\n");
                        this.events.add(event);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void startListeners() {
        this.mapInput.forEach( (k, v) -> {
            if(v.get(1) != null) {
                switch ((sensorType)v.get(3)){
                    case BUTTON:
                        this.addListenerButton((DigitalInput) v.get(0), (String) v.get(1));
                    case PIR:
                        this.addListenerPir((DigitalInput) v.get(0), (String) v.get(1));
                }
            }
        });
    }

    /*public DigitalInput getInputSensorByName(String Name){
        return (DigitalInput)mapInput.get(Name).get(0);
    }
    public DigitalOutput getOutputSensorByName(String Name){
        return (DigitalOutput)mapOutput.get(Name).get(0);
    }
    public String getSensorEvent(String Name){
        return (String)mapInput.get(Name).get(1);
    }*/


    public RaspBPhysicalAdapterConfiguration() {
        this.fullFillMaps();
    }

    public RaspBPhysicalAdapterConfiguration(DigitalInput pir, DigitalInput button, DigitalOutput led_Pir, DigitalOutput ledOff, DigitalOutput led, Map<String, ArrayList<?>> mapOutput, Map<String, ArrayList<?>> mapInput) {
        this.pir = pir;
        this.button = button;
        this.led_Pir = led_Pir;
        this.ledOff = ledOff;
        this.led = led;
        this.mapOutput = mapOutput;
        this.mapInput = mapInput;
    }

    public Context getPI4J(){
        return pi4j;
    }

    public ArrayList<String> getEvents(){
        return this.events;
    }

    public Map<String, ArrayList<?>> getMapOutput() {
        return mapOutput;
    }

    public Map<String, ArrayList<?>> getMapInput() {
        return mapInput;
    }
    public int getMaximumEvents(){
        return MAXIMUM_EVENTS;
    }
}