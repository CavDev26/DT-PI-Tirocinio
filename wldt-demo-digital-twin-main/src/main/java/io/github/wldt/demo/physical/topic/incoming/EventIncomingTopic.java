package io.github.wldt.demo.physical.topic.incoming;

import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.function.Function;

public class EventIncomingTopic<T> extends DigitalTwinIncomingTopic {
    public EventIncomingTopic(String topic, String eventKey, Function<String, T>eventBodyProducer) {
        super(topic, topicMsgPayload -> {
            try {
                return Collections.singletonList(new PhysicalAssetEventWldtEvent<>(eventKey, eventBodyProducer.apply(topicMsgPayload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return null;
        } );
    }
}
