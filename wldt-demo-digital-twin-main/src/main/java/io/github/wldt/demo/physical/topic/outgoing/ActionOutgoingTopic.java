package io.github.wldt.demo.physical.topic.outgoing;

import java.util.function.Function;

public class ActionOutgoingTopic<T> extends DigitalTwinOutgoingTopic {
    public ActionOutgoingTopic(String topic, Function<T, String> actionBodyConsumer) {
        super(topic, actionWldtEvent -> actionBodyConsumer.apply((T) actionWldtEvent.getBody()));
    }
}