package io.github.wldt.demo.physical.topic.incoming;

import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.function.Function;

public class PropertyIncomingTopic<T> extends DigitalTwinIncomingTopic {
    public PropertyIncomingTopic(String topic, String propertyKey, Function<String, T> propertyValueProducer) {
        super(topic, topicMsgPayload -> {
            try {
                return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyValueProducer.apply(topicMsgPayload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
