package io.github.wldt.demo.physical;

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import it.wldt.adapter.physical.PhysicalAssetDescription;

public interface Configuration {

    /**
     *
     */
    void fullfillMaps();

    /**
     * A function that permits to specify all properties, events and action Keys.
     * @return a new instance of PhysicalAssetDescription containing all the specified properties, events, actions.
     */
    public PhysicalAssetDescription createPhysicalAssetDescription();

    /**
     * A method that implements a basic construction of a listener for a button that generates an event upon detecting movement.
     * @param button the DigitalInput sensor to which a listener should be added.
     * @param event The event that is generated upon verifying a basic condition.
     */
    void addListenerButton(DigitalInput button, String event, String body);

    /**
     * A method that implements a basic construction of a listener for a PIR that generates an event upon detecting movement.
     * @param pir the DigitalInput sensor to which a listener should be added.
     * @param event The event that is generated upon verifying a basic condition.
     */
    void addListenerPir(DigitalInput pir, String event, String body);

    /**
     * A method to handle an action on a LED, given the body of the action and the led.
     * @param body Body of the action, specifying the correct behaviour
     * @param led Led on which executing the action.
     */
    void ledActionHandler(Object body, DigitalOutput led);
}
