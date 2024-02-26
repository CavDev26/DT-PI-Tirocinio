package io.github.wldt.demo;

import io.github.wldt.demo.digital.DemoConfDigitalAdapter;
import io.github.wldt.demo.digital.DemoDigitalAdapterConfiguration;
import io.github.wldt.demo.physical.*;
import it.wldt.core.engine.WldtEngine;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/09/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class RaspBPDigitalTwin {

    public static void main(String[] args)  {
        try{

            WldtEngine digitalTwinEngine = new WldtEngine(new RaspBPShadowingFunction("test-shadowing-function"), "test-digital-twin");

            //Physical and Digital Adapters with Configuration
            digitalTwinEngine.addPhysicalAdapter(new RaspBConfPhysicalAdapter("test-rasp-physical-adapter", new RaspBPhysicalAdapterConfiguration()));
            digitalTwinEngine.addDigitalAdapter(new DemoConfDigitalAdapter("test-digital-adapter", new DemoDigitalAdapterConfiguration()));

            digitalTwinEngine.startLifeCycle();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
