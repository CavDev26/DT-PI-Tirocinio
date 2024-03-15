package io.github.wldt.demo.physical;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.Pi4J;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RaspBPhysicalAdapterConfiguration {

    private final static Context pi4j = Pi4J.newAutoContext();
    private Map<String, ArrayList<?>> mapOutput = new HashMap<>();
    private Map<String, ArrayList<?>> mapInput = new HashMap<>();
    private Map<String, String> eventsMap = new HashMap<>();
    private static final int MAXIMUM_EVENTS = 10;

    /**
     * For a complete PINOUT documentation, see
     * <a href="
     *      https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#schematics-and-mechanical-drawings
     * ">...</a>
     */
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

    private final DigitalInput pir = createAndConfigDigitalInputPI4j("PIR", "PIR", PIN_PIR, 0L);
    private final DigitalInput button = createAndConfigDigitalInputPI4j("BUTTON", "BUTTON", PIN_BUTTON, 3000L);
    private final DigitalOutput led_Pir = createAndConfigDigitalOutputPI4j("LED-PIR", "LED-PIR", PIN_LED_PIR, DigitalState.LOW, DigitalState.LOW);
    private final DigitalOutput ledOff = createAndConfigDigitalOutputPI4j("LED-OFF", "LED-OFF", PIN_LED_OFF, DigitalState.LOW, DigitalState.HIGH);
    private final DigitalOutput led = createAndConfigDigitalOutputPI4j("LED-ON", "LED-ON", PIN_LED, DigitalState.LOW, DigitalState.LOW);


    //TODO METODO DA IMPLEMENTARE DA UTENTE
    /**
     * A method where to fill the given data structures using the provided utility methods (createOutputEntrySensor, createInputEntrySensor)
     */
    private void fullfillMaps(){
        this.createOutputEntrySensor(led_Pir, LED_PIR_ON_OFF_PROPERTY_KEY, LED_PIR_ON_OFF_ACTION_KEY, outputType.LED);
        this.createOutputEntrySensor(ledOff, LED_OFF_PROPERTY_KEY, LED_OFF_ACTION_KEY, outputType.LED);
        this.createOutputEntrySensor(led, LED_ON_OFF_PROPERTY_KEY, LED_ON_OFF_ACTION_KEY, outputType.LED);

        this.createInputEntrySensor(pir, PIR_EVENT_KEY, null, sensorType.PIR, "Movement");
        this.createInputEntrySensor(button, BUTTON_EVENT_KEY, null, sensorType.BUTTON, "Pressing");
    }

    //TODO METODO DA IMPLEMENTARE DA UTENTE
    /**
     * A function that permits to specify all properties, events and action Keys.
     * @return a new instance of PhysicalAssetDescription containing all the specified properties, events, actions.
     */
    public PhysicalAssetDescription createPhysicalAssetDescription(){

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

    //TODO METODO DA IMPLEMENTARE DA UTENTE
    /**
     * A method that implements a basic construction of a listener for a button that generates an event upon detecting movement.
     * @param button the DigitalInput sensor to which a listener should be added.
     * @param event The event that is generated upon verifying a basic condition.
     */
    private void addListenerButton(DigitalInput button, String event, String body){
        button.addListener(s -> {
            if (s.state() == DigitalState.LOW) {
                if (!this.eventsMap.containsKey(event)) {
                    System.out.println("\nBUTTON PRESSED\n");
                    this.eventsMap.put(event, body);
                }
            }
        });
    }

    //TODO METODO DA IMPLEMENTARE DA UTENTE
    /**
     * A method that implements a basic construction of a listener for a PIR that generates an event upon detecting movement.
     * @param pir the DigitalInput sensor to which a listener should be added.
     * @param event The event that is generated upon verifying a basic condition.
     */
    private void addListenerPir(DigitalInput pir, String event, String body) {
        pir.addListener(s -> {
            if (s.state() == DigitalState.LOW) {
                if (!this.eventsMap.containsKey(event)) {
                    System.out.println("\nMOVEMENT DETECTED\n");
                    this.eventsMap.put(event, body);
                }
            }
        });
    }

    //TODO METODO DA IMPLEMENTARE DA UTENTE
    /**
     * A method to handle an action on a LED, given the body of the action and the led.
     * @param body Body of the action, specifying the correct behaviour
     * @param led Led on which executing the action.
     */
    private void ledActionHandler(Object body, DigitalOutput led){
        if (body.equals(1)) {
            led.high();
        } else if (body.equals(0)) {
            led.low();
        } else {
            System.out.println("Error - wrong action body received.");
        }
    }




    /**
     * Constructor
     */
    public RaspBPhysicalAdapterConfiguration() {
        this.fullfillMaps();
    }

    /**
     * A method that handles the behaviour upon receiving an action given the body of the action and the input key of the map of all input Sensors.
     * @param body body of the action received.
     * @param k key of the Input map on which the action is done.
     * @return a new PhysicalAssetPropertyWldtEvent if the specified input Type requires it, null otherwise.
     */
    public PhysicalAssetPropertyWldtEvent<?> actionHandlerInput(Object body, String k){
        //TODO

        switch ((sensorType) this.mapInput.get(k).get(3)) {
            case BUTTON:
                return null;
            case PIR:
                return null;
            default:
                return null;
        }
    }

    /**
     * A method that handles the behaviour upon receiving an action given the body of the action and the outPut key of the map of all output devices.
     * @param body body of the action received.
     * @param k key of the OutputSensors map on which the action is done.
     * @return a new PhysicalAssetPropertyWldtEvent if the specified output Type requires it, null otherwise.
     */
    public PhysicalAssetPropertyWldtEvent<?> actionHandlerOutput(Object body, String k){
        switch ((outputType) this.mapOutput.get(k).get(3)) {
            case LED:
                this.ledActionHandler(body, (DigitalOutput) this.mapOutput.get(k).get(0));
                try {
                    return new PhysicalAssetPropertyWldtEvent<>((String) this.mapOutput.get(k).get(1), (Integer) body);
                } catch (EventBusException e) {
                    throw new RuntimeException(e);
                }
            default:
                return null;
            case GENERICTEST:
                return null;
        }
    }

    /**
     * A method that creates a new instance of a pi4j input sensor.
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
     *A method that creates a new instance of a pi4j output device.
     * @param id identifier of the device.
     * @param SensorName Name of the device.
     * @param PIN Pin to which the device is linked to.
     * @param shutdownState Default state (HIGH or LOW) to which the device should be on shutdown.
     * @param initialState Default state (HIGH or LOW) to which the device should be on startup.
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
     * A Function that creates a new entry in the Output Devices Map given Name (Key) and [digitalOutputSensor, propertyKey, actionKey] (Value)
     * @param digitalOutputSensor DigitalOutput device
     * @param propertyKey propertyKey linked to the output sensor
     * @param actionKey actionKey linked to the output device
     * @param type the Output device type, chosen within the supported ones.
     */
    private void createOutputEntrySensor(DigitalOutput digitalOutputSensor, String propertyKey, String actionKey, outputType type) {
        this.mapOutput.put(digitalOutputSensor.name(), new ArrayList<>() {{
            add(digitalOutputSensor);
            add(propertyKey);
            add(actionKey);
            add(type);
        }});
    }
    /**
     * A Function that creates a new entry in the Input Sensors Map given Name (Key) and [digitalInputSensor, eventKey] (Value)
     * @param digitalInputSensor DigitalInput sensor.
     * @param eventKey EventKey linked to the input sensor.
     * @param actionKey ActionKey linked to the input sensor.
     * @param type specify the sensor type from the supported ones.
     * @param eventBody specify the body of the event linked to the sensor.
     */
    private void createInputEntrySensor(DigitalInput digitalInputSensor, String eventKey, String actionKey, sensorType type, String eventBody) {
        this.mapInput.put(digitalInputSensor.name(), new ArrayList<>() {{
            add(digitalInputSensor);
            add(eventKey);
            add(actionKey);
            add(type);
            add(eventBody);
        }});
    }
    /**
     * An enum that represents all the Input sensors types that are currently supported..
     */
    private enum sensorType {
        BUTTON,
        PIR
    }

    /**
     * An enum that represents all the output devices' types that are currently supported.
     */
    private enum outputType {
        GENERICTEST,
        LED
    }
    /**
     * A method that adds listeners to the input sensors only if they have an event associated to them.
     */
    public void startListeners() {
        System.out.println("[RraspberryPI -> Adding listeners...]");
        this.mapInput.forEach( (k, v) -> {
            if(v.get(1) != null) {
                switch ((sensorType)v.get(3)){
                    case BUTTON:
                        this.addListenerButton((DigitalInput) v.get(0), (String) v.get(1), (String) v.get(4));
                        break;
                    case PIR:
                        this.addListenerPir((DigitalInput) v.get(0), (String) v.get(1), (String) v.get(4));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * A method that returns PI4J context.
     * @return PI4J context.
     */
    public Context getPI4J(){
        return pi4j;
    }

    /**
     * A method that returns the Events map.
     * @return EventsMap.
     */
    public Map<String, String> getEventsMap(){
        return this.eventsMap;
    }

    /**
     * A method that returns the output sensors Map.
     * @return mapOutput.
     */
    public Map<String, ArrayList<?>> getMapOutput() {
        return mapOutput;
    }

    /**
     * A method that returns the input sensor Map.
     * @return mapInput.
     */
    public Map<String, ArrayList<?>> getMapInput() {
        return mapInput;
    }

    /**
     * A method that retuns the maximum number of events for the system.
     * @return MAXIMUM_EVENTS.
     */
    public int getMaximumEvents(){
        return MAXIMUM_EVENTS;
    }
}