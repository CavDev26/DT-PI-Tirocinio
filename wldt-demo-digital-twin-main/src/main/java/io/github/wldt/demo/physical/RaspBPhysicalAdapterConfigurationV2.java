package io.github.wldt.demo.physical;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.Pi4J;
import io.github.wldt.demo.physical.exception.RaspBPhysicalAdapterConfigurationException;
import io.github.wldt.demo.physical.topic.incoming.DigitalTwinIncomingTopic;
import io.github.wldt.demo.physical.topic.outgoing.DigitalTwinOutgoingTopic;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.*;

public class RaspBPhysicalAdapterConfigurationV2 {


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

    private DigitalOutput createAndConfigDigitalOutputPI4j(String id, String SensorName, int PIN, DigitalState shutdown, DigitalState initial) {
        DigitalOutputConfigBuilder builder = DigitalOutput.newConfigBuilder(pi4j)
                .id(id)
                .name(SensorName)
                .address(PIN)
                .shutdown(shutdown)
                .initial(initial)
                .provider("pigpio-digital-output");
        return pi4j.create(builder);
    }

    DigitalInput pir = createAndConfigDigitalInputPI4j("PIR", "Pir", getPin_pir());
    DigitalInput button = createAndConfigDigitalInputPI4j("BUTTON", "Button", getPin_button());
    DigitalOutput led_Pir = createAndConfigDigitalOutputPI4j("LED-PIR", "LED-PIR", getPin_led_pir(), DigitalState.LOW, DigitalState.LOW);
    DigitalOutput ledOff = createAndConfigDigitalOutputPI4j("LED-OFF", "LED-OFF", getPin_led_off(), DigitalState.LOW, DigitalState.HIGH);
    DigitalOutput led = createAndConfigDigitalOutputPI4j("LED-ON", "LED-ON", getPin_led(), DigitalState.LOW, DigitalState.LOW);

    private Map<String, DigitalOutput> mapOutput = new HashMap<>();
    private Map<String, DigitalInput> mapInput = new HashMap<>();




    private PhysicalAssetDescription physicalAssetDescription;

    //INCOMING TOPICS: Topics to which the PhysicalAdapter must subscribe
    private final List<DigitalTwinIncomingTopic> incomingTopics = new ArrayList<>();
    //OUTGOING TOPICS: Topics on which the PhysicalAdapter must publish
    private final Map<String, DigitalTwinOutgoingTopic> outgoingTopics = new HashMap<>();




    private void fullFillMaps() {
        this.mapOutput.put("PIR", led_Pir);
        this.mapOutput.put("LED-OFF", ledOff);
        this.mapOutput.put("LED-ON", led);
        this.mapInput.put("PIR", pir);
        this.mapInput.put("BUTTON", button);
    }








    public static RaspBPhysicalAdapterConfigurationBuilderV2 builder() throws RaspBPhysicalAdapterConfigurationException {
        return new RaspBPhysicalAdapterConfigurationBuilderV2();
    }
    public List<DigitalTwinIncomingTopic> getIncomingTopics() {
        return incomingTopics;
    }

    public Map<String, DigitalTwinOutgoingTopic> getOutgoingTopics() {
        return outgoingTopics;
    }

    public Optional<DigitalTwinOutgoingTopic> getOutgoingTopicByActionKey(String key){
        return outgoingTopics.containsKey(key) ? Optional.of(outgoingTopics.get(key)) : Optional.empty();
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
    }

    protected void addIncomingTopic(DigitalTwinIncomingTopic topic){
        this.incomingTopics.add(topic);
    }

    protected void addOutgoingTopic(String actionKey, DigitalTwinOutgoingTopic topic){
        this.outgoingTopics.put(actionKey,topic);
    }
    protected void setPhysicalAssetDescription(List<PhysicalAssetAction> actions,
                                               List<PhysicalAssetProperty<?>> properties,
                                               List<PhysicalAssetEvent> events){
        this.physicalAssetDescription = new PhysicalAssetDescription(actions, properties, events);
    }











    public RaspBPhysicalAdapterConfigurationV2() {
        this.fullFillMaps();
    }

    public RaspBPhysicalAdapterConfigurationV2(int pin_led, int pin_pir, int pin_led_pir, int pin_led_off, int pin_button, DigitalInput pir, DigitalInput button, DigitalOutput led_Pir, DigitalOutput ledOff, DigitalOutput led, Map<String, DigitalOutput> mapOutput, Map<String, DigitalInput> mapInput) {
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

    public Map<String, DigitalOutput> getMapOutput() {
        return mapOutput;
    }

    public void setMapOutput(Map<String, DigitalOutput> mapOutput) {
        this.mapOutput = mapOutput;
    }

    public Map<String, DigitalInput> getMapInput() {
        return mapInput;
    }

    public void setMapInput(Map<String, DigitalInput> mapInput) {
        this.mapInput = mapInput;
    }
}