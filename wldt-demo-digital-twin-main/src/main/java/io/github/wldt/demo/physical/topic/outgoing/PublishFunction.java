package io.github.wldt.demo.physical.topic.outgoing;

import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;

import java.util.function.Function;

public interface PublishFunction extends Function<PhysicalAssetActionWldtEvent<?>, String> {
}
