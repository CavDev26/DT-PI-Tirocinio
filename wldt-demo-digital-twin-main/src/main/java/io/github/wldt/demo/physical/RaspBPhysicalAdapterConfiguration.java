package io.github.wldt.demo.physical;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.Pi4J;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RaspBPhysicalAdapterConfiguration {


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



    private int pin_led = PIN_LED;
    private int pin_pir = PIN_PIR;
    private int pin_led_pir = PIN_LED_PIR;
    private int pin_led_off = PIN_LED_OFF;
    private int pin_button = PIN_BUTTON;

    private final static Context pi4j = Pi4J.newAutoContext();

    private DigitalInput createAndConfigDigitalInputPI4j(String id, String SensorName, int PIN) {
        DigitalInputConfigBuilder builder = DigitalInput.newConfigBuilder(pi4j)
                .id(id)
                .name(SensorName)
                .address(PIN)
                .provider("pigpio-digital-input");
        return pi4j.create(builder);
    }

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

    DigitalInput pir = createAndConfigDigitalInputPI4j("PIR", "PIR", getPin_pir());
    DigitalInput button = createAndConfigDigitalInputPI4j("BUTTON", "BUTTON", getPin_button());
    DigitalOutput led_Pir = createAndConfigDigitalOutputPI4j("LED-PIR", "LED-PIR", getPin_led_pir(), DigitalState.LOW, DigitalState.LOW);
    DigitalOutput ledOff = createAndConfigDigitalOutputPI4j("LED-OFF", "LED-OFF", getPin_led_off(), DigitalState.LOW, DigitalState.HIGH);
    DigitalOutput led = createAndConfigDigitalOutputPI4j("LED-ON", "LED-ON", getPin_led(), DigitalState.LOW, DigitalState.LOW);

    private void createPhysicalAssetDescription(){
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
    }

    //private Map<String, DigitalOutput> mapOutput = new HashMap<>();
    //private Map<String, DigitalInput> mapInput = new HashMap<>();


    private Map<String, ArrayList<?>> mapOutput = new HashMap<>();

    private Map<String, ArrayList<?>> mapInput = new HashMap<>();


    private void fullFillMaps() {
        //map containing name as key; (sensor, Property, action) as value
        this.mapOutput.put(led_Pir.name(), new ArrayList<>() {{
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
        }});

        this.mapInput.put(pir.name(), new ArrayList<>() {{
            add(pir);
            add(PIR_EVENT_KEY);
        }});
        this.mapInput.put(button.name(), new ArrayList<>() {{
            add(button);
            add(BUTTON_EVENT_KEY);
        }});

        //this.mapOutput.put(led_Pir.name(), led_Pir);
        //this.mapOutput.put(ledOff.name(), ledOff);
        //this.mapOutput.put(led.name(), led);

        //this.mapInput.put(pir.name(), pir);
        //this.mapInput.put(button.name(), button);
    }

    public DigitalInput getInputSensorByName(String Name){
        return (DigitalInput)mapInput.get(Name).get(0);
    }
    public DigitalOutput getOutputSensorByName(String Name){
        return (DigitalOutput)mapOutput.get(Name).get(0);
    }
    public String getSensorEvent(String Name){
        return (String)mapInput.get(Name).get(1);
    }


    public RaspBPhysicalAdapterConfiguration() {
        this.fullFillMaps();
    }

    public RaspBPhysicalAdapterConfiguration(int pin_led, int pin_pir, int pin_led_pir, int pin_led_off, int pin_button, DigitalInput pir, DigitalInput button, DigitalOutput led_Pir, DigitalOutput ledOff, DigitalOutput led, Map<String, ArrayList<?>> mapOutput, Map<String, ArrayList<?>> mapInput) {
        this.pin_led = pin_led;
        this.pin_pir = pin_pir;
        this.pin_led_pir = pin_led_pir;
        this.pin_led_off = pin_led_off;
        this.pin_button = pin_button;
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

    public int getPin_led() {
        return pin_led;
    }

    public void setPin_led(int pin_led) {
        this.pin_led = pin_led;
    }

    public int getPin_pir() {
        return pin_pir;
    }

    public void setPin_pir(int pin_pir) {
        this.pin_pir = pin_pir;
    }

    public int getPin_led_pir() {
        return pin_led_pir;
    }

    public void setPin_led_pir(int pin_led_pir) {
        this.pin_led_pir = pin_led_pir;
    }

    public int getPin_led_off() {
        return pin_led_off;
    }

    public void setPin_led_off(int pin_led_off) {
        this.pin_led_off = pin_led_off;
    }

    public int getPin_button() {
        return pin_button;
    }

    public void setPin_button(int pin_button) {
        this.pin_button = pin_button;
    }

    public DigitalInput getPir() {
        return pir;
    }

    public void setPir(DigitalInput pir) {
        this.pir = pir;
    }

    public DigitalInput getButton() {
        return button;
    }

    public void setButton(DigitalInput button) {
        this.button = button;
    }

    public DigitalOutput getLed_Pir() {
        return led_Pir;
    }

    public void setLed_Pir(DigitalOutput led_Pir) {
        this.led_Pir = led_Pir;
    }

    public DigitalOutput getLedOff() {
        return ledOff;
    }

    public void setLedOff(DigitalOutput ledOff) {
        this.ledOff = ledOff;
    }

    public DigitalOutput getLed() {
        return led;
    }

    public void setLed(DigitalOutput led) {
        this.led = led;
    }

    public Map<String, ArrayList<?>> getMapOutput() {
        return mapOutput;
    }

    public void setMapOutput(Map<String, ArrayList<?>> mapOutput) {
        this.mapOutput = mapOutput;
    }

    public Map<String, ArrayList<?>> getMapInput() {
        return mapInput;
    }

    public void setMapInput(Map<String, ArrayList<?>> mapInput) {
        this.mapInput = mapInput;
    }
}