package io.github.wldt.demo.physical;

import io.github.wldt.demo.physical.exception.RaspBPhysicalAdapterConfigurationException;
import io.github.wldt.demo.physical.topic.incoming.DigitalTwinIncomingTopic;
import io.github.wldt.demo.physical.topic.incoming.EventIncomingTopic;
import io.github.wldt.demo.physical.topic.incoming.PropertyIncomingTopic;
import io.github.wldt.demo.physical.topic.outgoing.ActionOutgoingTopic;
import io.github.wldt.demo.physical.topic.outgoing.DigitalTwinOutgoingTopic;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import io.github.wldt.demo.physical.topic.Topic;
import it.wldt.adapter.physical.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RaspBPhysicalAdapterConfigurationBuilderV2 {
    private final RaspBPhysicalAdapterConfigurationV2 configuration = new RaspBPhysicalAdapterConfigurationV2();
    private final List<PhysicalAssetProperty<?>> properties = new ArrayList<>();
    private final List<PhysicalAssetEvent> events = new ArrayList<>();
    private final List<PhysicalAssetAction> actions = new ArrayList<>();

    public <T> RaspBPhysicalAdapterConfigurationBuilderV2 addPhysicalAssetPropertyAndTopic(String propertyKey, T initialValue, String topic, Function<String, T> topicFunction) throws RaspBPhysicalAdapterConfigurationException {
        checkTopicAndFunction(topic, topicFunction, this.configuration.getIncomingTopics().stream().map(Topic::getTopic).collect(Collectors.toList()));
        configuration.addIncomingTopic(new PropertyIncomingTopic<>(topic, propertyKey, topicFunction));
        return addPhysicalAssetProperty(propertyKey, initialValue);
    }

    public <T> RaspBPhysicalAdapterConfigurationBuilderV2 addPhysicalAssetActionAndTopic(String actionKey, String type, String contentType,
                                                                                      String topic, Function<T, String> topicFunction) throws RaspBPhysicalAdapterConfigurationException {
        checkTopicAndFunction(topic, topicFunction, this.configuration.getOutgoingTopics().values().stream().map(Topic::getTopic).collect(Collectors.toList()));
        configuration.addOutgoingTopic(actionKey, new ActionOutgoingTopic<>(topic, topicFunction));
        return addPhysicalAssetAction(actionKey, type, contentType);
    }

    public <T> RaspBPhysicalAdapterConfigurationBuilderV2 addPhysicalAssetEventAndTopic(String eventKey, String type, String topic, Function<String, T> topicFunction) throws RaspBPhysicalAdapterConfigurationException {
        checkTopicAndFunction(topic, topicFunction, this.configuration.getIncomingTopics().stream().map(Topic::getTopic).collect(Collectors.toList()));
        configuration.addIncomingTopic(new EventIncomingTopic<>(topic, eventKey, topicFunction));
        return addPhysicalAssetEvent(eventKey, type);

    }

    public RaspBPhysicalAdapterConfigurationBuilderV2 addIncomingTopic(DigitalTwinIncomingTopic topic, List<PhysicalAssetProperty<?>> properties, List<PhysicalAssetEvent> events) throws RaspBPhysicalAdapterConfigurationException {
        if(topic == null) throw new RaspBPhysicalAdapterConfigurationException("DigitalTwinIncomingTopic cannot be null");
        if(!isValid(properties) && !isValid(events)) throw new RaspBPhysicalAdapterConfigurationException("Property and event list cannot be null or empty. For each DigitalTwinIncomingTopic, related properties and events must be specified");
        checkTopicAndFunction(topic.getTopic(), topic.getSubscribeFunction(), this.configuration.getIncomingTopics().stream().map(Topic::getTopic).collect(Collectors.toList()));
        this.properties.addAll(properties);
        this.events.addAll(events);
        configuration.addIncomingTopic(topic);
        return this;
    }

    public RaspBPhysicalAdapterConfigurationBuilderV2 addOutgoingTopic(String actionKey,  String type, String contentType, DigitalTwinOutgoingTopic topic) throws RaspBPhysicalAdapterConfigurationException {
        if(topic == null || isValid(actionKey)) throw new RaspBPhysicalAdapterConfigurationException("DigitalTwinOutgoingTopic cannot be null | Action key cannot be empty string or null");
        checkTopicAndFunction(topic.getTopic(), topic.getPublishFunction(), this.configuration.getOutgoingTopics().values().stream().map(Topic::getTopic).collect(Collectors.toList()));
        configuration.addOutgoingTopic(actionKey, topic);
        return addPhysicalAssetAction(actionKey, type, contentType);
    }

    private <T> RaspBPhysicalAdapterConfigurationBuilderV2 addPhysicalAssetProperty(String key, T initValue){
        this.properties.add(new PhysicalAssetProperty<>(key, initValue));
        return this;
    }

    private RaspBPhysicalAdapterConfigurationBuilderV2 addPhysicalAssetAction(String key, String type, String contentType){
        this.actions.add(new PhysicalAssetAction(key, type, contentType));
        return this;
    }

    private RaspBPhysicalAdapterConfigurationBuilderV2 addPhysicalAssetEvent(String key, String type){
        this.events.add(new PhysicalAssetEvent(key, type));
        return this;
    }

    public RaspBPhysicalAdapterConfigurationV2 build() throws RaspBPhysicalAdapterConfigurationException {
        if(properties.isEmpty() && actions.isEmpty() && events.isEmpty())
            throw new RaspBPhysicalAdapterConfigurationException("Physical Adapter must have at least one property or event or action");
        if(this.configuration.getIncomingTopics().isEmpty() && this.configuration.getOutgoingTopics().isEmpty())
            throw new RaspBPhysicalAdapterConfigurationException("MQTT Physical Adapter must define at least one DigitalTwinIncomingTopic or DigitalTwinOutgoingTopic");
        this.configuration.setPhysicalAssetDescription(actions, properties, events);
        return this.configuration;
    }

    private <I, O> void checkTopicAndFunction(String topic, Function<I, O> topicFunction, List<String> topicList) throws RaspBPhysicalAdapterConfigurationException {
        if(!isValid(topic) || topicFunction == null)
            throw new RaspBPhysicalAdapterConfigurationException("topic cannot be empty or null | topic function cannot be null");
        if(topicList.contains(topic))
            throw new RaspBPhysicalAdapterConfigurationException("topic already defined");
    }

    private <T> boolean isValid(List<T> list){
        return list != null && !list.isEmpty();
    }

    private boolean isValid(String param){
        return param != null && !param.isEmpty();
    }

    private boolean isValid(int param){
        return param > 0;
    }

}
