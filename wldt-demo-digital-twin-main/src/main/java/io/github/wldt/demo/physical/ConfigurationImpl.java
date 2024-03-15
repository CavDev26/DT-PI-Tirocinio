package io.github.wldt.demo.physical;

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import it.wldt.adapter.physical.PhysicalAssetDescription;

public class ConfigurationImpl implements Configuration{

    @Override
    public void fullfillMaps() {

    }

    @Override
    public PhysicalAssetDescription createPhysicalAssetDescription() {
        return null;
    }

    @Override
    public void addListenerButton(DigitalInput button, String event, String body) {

    }

    @Override
    public void addListenerPir(DigitalInput pir, String event, String body) {

    }

    @Override
    public void ledActionHandler(Object body, DigitalOutput led) {

    }
}
