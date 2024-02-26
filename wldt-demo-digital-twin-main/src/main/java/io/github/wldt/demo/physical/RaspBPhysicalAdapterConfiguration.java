package io.github.wldt.demo.physical;

public class RaspBPhysicalAdapterConfiguration {

    private final static int MESSAGE_UPDATE_TIME = 1000;
    private final static int MESSAGE_UPDATE_NUMBER = 10;


    private static final int PIN_LED = 27; //PIN 13 = BCM 27
    private static final int PIN_PIR = 4; //PIN 7 = BCM 4
    private static final int PIN_LED_PIR = 17; //PIN 11 = BCM 17
    private static final int PIN_LED_OFF = 23; //PIN 16 = BCM 23
    private static final int PIN_BUTTON = 22; //PIN 15 = BCM 22

    private int messageUpdateTime = MESSAGE_UPDATE_TIME;
    private int messageUpdateNumber = MESSAGE_UPDATE_NUMBER;
    private int pin_led = PIN_LED;
    private int pin_pir = PIN_PIR;
    private int pin_led_pir = PIN_LED_PIR;
    private int pin_led_off = PIN_LED_OFF;
    private int pin_button = PIN_BUTTON;


    public RaspBPhysicalAdapterConfiguration() {
    }

    public RaspBPhysicalAdapterConfiguration(int messageUpdateTime, int messageUpdateNumber, int pin_led, int pin_pir, int pin_led_pir, int pin_led_off, int pin_button){
        this.messageUpdateTime = messageUpdateTime;
        this.messageUpdateNumber = messageUpdateNumber;
        this.pin_led = pin_led;
        this.pin_pir = pin_pir;
        this.pin_led_pir = pin_led_pir;
        this.pin_led_off = pin_led_off;
        this.pin_button = pin_button;
    }

    public int getMessageUpdateTime() {
        return messageUpdateTime;
    }

    public void setMessageUpdateTime(int messageUpdateTime) {
        this.messageUpdateTime = messageUpdateTime;
    }

    public int getMessageUpdateNumber() {
        return messageUpdateNumber;
    }

    public void setMessageUpdateNumber(int messageUpdateNumber) {
        this.messageUpdateNumber = messageUpdateNumber;
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



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DemoPhysicalAdapterConfiguration{");
        sb.append("messageUpdateTime=").append(messageUpdateTime);
        sb.append(", messageUpdateNumber=").append(messageUpdateNumber);
        sb.append('}');
        return sb.toString();
    }
}
