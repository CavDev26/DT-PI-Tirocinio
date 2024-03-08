package io.github.wldt.demo.physical.topic.incoming;


import it.wldt.core.event.WldtEvent;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
/**
 * Represents a function that accepts a message published on topic (of type String) and when applied, produces a WldtEvent.
 * */
public interface SubscribeFunction extends Function<String, List<WldtEvent<?>>> {
}