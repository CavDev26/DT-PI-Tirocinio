package io.github.wldt.demo.physical;

public class RaspBPhysicalAdapterConfiguration {

    private final static int MESSAGE_UPDATE_TIME = 10000;
    private final static int MESSAGE_UPDATE_NUMBER = 1000;

    private int messageUpdateTime = MESSAGE_UPDATE_TIME;
    private int messageUpdateNumber = MESSAGE_UPDATE_NUMBER;
    public RaspBPhysicalAdapterConfiguration(){

    }
    public RaspBPhysicalAdapterConfiguration(int messageUpdateTime, int messageUpdateNumber){
        this.messageUpdateTime = messageUpdateTime;
        this.messageUpdateNumber = messageUpdateNumber;
    }
    public int getMessageUpdateNumber() {
        return messageUpdateNumber;
    }

    public int getMessageUpdateTime() {
        return messageUpdateTime;
    }


    @Override
    public String toString() {
        return "RaspBPhysicalAdapterConfiguration{" +
                "messageUpdateTime=" + messageUpdateTime +
                ", messageUpdateNumber=" + messageUpdateNumber +
                '}';
    }
}
