package io.github.wldt.demo.physical.topic.outgoing;

import io.github.wldt.demo.physical.topic.Topic;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;

public class DigitalTwinOutgoingTopic extends Topic {
    private final PublishFunction publishFunction;

    public DigitalTwinOutgoingTopic(String topic, PublishFunction publishFunction) {
        super(topic);
        this.publishFunction = publishFunction;
    }

    public String applyPublishFunction(PhysicalAssetActionWldtEvent<?> actionWldtEvent){
        return this.publishFunction.apply(actionWldtEvent);
    }

    public PublishFunction getPublishFunction() {
        return publishFunction;
    }
}
