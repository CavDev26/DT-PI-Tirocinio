package io.github.wldt.demo.physical.topic.incoming;

import io.github.wldt.demo.physical.topic.Topic;
import it.wldt.core.event.WldtEvent;

import java.util.List;

public class DigitalTwinIncomingTopic extends Topic {

    private final SubscribeFunction mqttSubscribeFunction;

    public DigitalTwinIncomingTopic(String topic, SubscribeFunction mqttSubscribeFunction) {
        super(topic);
        this.mqttSubscribeFunction = mqttSubscribeFunction;
    }

    public List<WldtEvent<?>> applySubscribeFunction(String topicMessagePayload){
        return mqttSubscribeFunction.apply(topicMessagePayload);
    }

    public SubscribeFunction getSubscribeFunction() {
        return mqttSubscribeFunction;
    }
}
