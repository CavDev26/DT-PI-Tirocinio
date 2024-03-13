package io.github.wldt.demo.physical;

import com.pi4j.io.gpio.digital.*;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.ArrayList;


public class RaspBConfPhysicalAdapter extends ConfigurablePhysicalAdapter<RaspBPhysicalAdapterConfiguration> {

    public RaspBConfPhysicalAdapter(String id, RaspBPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if (physicalAssetActionWldtEvent != null && getConfiguration().getMapOutput().values().stream().anyMatch(obj -> obj.contains(physicalAssetActionWldtEvent.getActionKey()))) {
                getConfiguration().getMapOutput().forEach((k, v) -> {

                    if(v.contains(physicalAssetActionWldtEvent.getActionKey())) {
                        //TODO
                        notifyLedPropertyEvent(physicalAssetActionWldtEvent, (DigitalOutput) v.get(0), (String) v.get(1));
                    }
                });
            }else if (physicalAssetActionWldtEvent != null && getConfiguration().getMapInput().values().stream().anyMatch(obj -> obj.contains(physicalAssetActionWldtEvent.getActionKey()))) {
                getConfiguration().getMapInput().forEach((k, v) -> {
                    if(v.contains(physicalAssetActionWldtEvent.getActionKey())) {
                        //TODO
                    }
                });
            } else {
                System.err.println("[RaspPhysicalAdapter] -> Wrong action received!");
            }


            /*if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(LED_ON_OFF_ACTION_KEY)//Action regarding the ON/OFF property of the Button led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, getConfiguration().getLed(), LED_ON_OFF_PROPERTY_KEY);
            } else if (physicalAssetActionWldtEvent != null
                    && physicalAssetActionWldtEvent.getActionKey().equals(LED_PIR_ON_OFF_ACTION_KEY) //Action regarding the ON/OFF property of the green PIR led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, getConfiguration().getLed_Pir(), LED_PIR_ON_OFF_PROPERTY_KEY);
            } else if (physicalAssetActionWldtEvent != null
                        && physicalAssetActionWldtEvent.getActionKey().equals(LED_OFF_ACTION_KEY) //Action regarding the ON/OFF property of the red PIR led
                ) { notifyLedPropertyEvent(physicalAssetActionWldtEvent, getConfiguration().getLedOff(), LED_OFF_PROPERTY_KEY);
            } else {
                System.err.println("[RaspPhysicalAdapter] -> Wrong action received!");
            }*/

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onAdapterStart() {
        try {
            new Thread(publishPhysicalAssetDescription()).start();
            new Thread(deviceEmulation()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onAdapterStop() {

    }

    private Runnable publishPhysicalAssetDescription() {
        return () -> {
            try {
                System.out.println("[RaspPhysicalAdapter] -> Sleeping before Publishing Physical Asset Description...");
                Thread.sleep(2000); //sleep di 2 secondi
                System.out.println("[RaspPhysicalAdapter] -> Publishing Physical Asset Description...");
                this.notifyPhysicalAdapterBound(getConfiguration().createPhysicalAssetDescription());
            } catch(Exception e) {
                e.printStackTrace();
            }
        };
    }

    private Runnable deviceEmulation() {
        return () -> {
            try{
                System.out.println("[RaspPhysicalAdapter] -> Starting physical device (PI)...");
                System.out.println("[RaspPhysicalAdapter] -> Printing PI4J Registry of Sensors:");
                System.out.println(getConfiguration().getPI4J().registry().all() + "\n");

                this.getConfiguration().startListeners();
                int i = 0;
                while (i < getConfiguration().getMaximumEvents()) {
                    if(!getConfiguration().getEvents().isEmpty()) {
                        getConfiguration().getEvents().forEach( (e) -> {
                            try {
                                //TODO
                                //TODO Questa cosa funziona ma per qualche motivo mi genera eventi un pelo sbagliati,
                                //TODO mi accende e spegne il led subito (sia verde che rosso).
                                //TODO pir invece sembra funzionare.
                                //TODO da cambiare il body, sicuramente influisce sul corretto funzionamento.

                                publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(e, "Pressed"));
                                Thread.sleep(100); //sleep tra una pubblicazione di un evento e l'altro, da verificare. questo genera dei problemi
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                        i = i + getConfiguration().getEvents().size(); //da verificare quanto valga size
                        getConfiguration().getEvents().clear();
                    }
                }

                //this.addListenerButton(getConfiguration().getInputSensorByName("BUTTON"), getConfiguration().getSensorEvent("BUTTON"));
                //this.addListenerPir(getConfiguration().getInputSensorByName("PIR"), getConfiguration().getSensorEvent("PIR"));

                //pi4j.shutdown();

            }catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private void addListenerButton(DigitalInput button, String event){
        button.addListener(s -> {
            try{
                if (s.state() == DigitalState.LOW) {
                    System.out.println("BUTTON PRESSED");
                    publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(event, "Pressed"));
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addListenerPir(DigitalInput pir, String event) {
        pir.addListener(s -> {
            try{
                if (s.state() == DigitalState.LOW) {
                    System.out.println("MOVEMENT DETECTED");
                    publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(event, "Moved"));
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void notifyLedPropertyEvent(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent, DigitalOutput led, String PROPERTY_KEY){
        try {
            //TODO
            //Da aggiornare il comportamento di modotale che possa essere eseguita ogni azione. Posso mettere dei metodi nella configuration
            //che fanno le azioni specifiche e ognuna viene chiamata in base al tipo di azione (ad esempio se è un led, il led.high() viene passato dalla conf
            //posso fare come esempio il fatto di voler tracciare come action la finta pressione di un pulsante che viene inviata dal digitalAdapter
            //se quindi action == BUTTONACTION allora il comportamento pè gestito nella config e deve solo essere chiamato da questa funzione
            //
            //In seguito è necessario fare gli addlistener generici, nella config che vengono chiamati all'interno della device emulation (Da togliere in futuro)
            //Magari tramite un foreach di tutti gli input, si può aggiungere una entry nei values che indichi la presenza o meno di un listener, il cui comportamento viene
            //specificato sempre nella conf, per poi essere aggiunto al thread dentro la device emulation.
            //Probabilente uso di foreach e di stream che mi riportano le cose giuste per poter usare la funzione di publish.


            System.out.println("[RaspPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                    + "with Body: " + physicalAssetActionWldtEvent.getBody() + "\n");
            if (physicalAssetActionWldtEvent.getBody().equals(1)) {
                led.high();
                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(PROPERTY_KEY, 1);
                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
            } else {
                led.low();
                PhysicalAssetPropertyWldtEvent<Integer> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(PROPERTY_KEY, 0);
                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*private void publishPhysicalRelationshipInstance(){
        try{
            String relatoinshipTarget = "building-hq";
            Map<String, Object> relationshipMetadata = new HashMap<>();
            relationshipMetadata.put("floor", "f0");
            relationshipMetadata.put("room", "r0");
            PhysicalAssetRelationshipInstance<String> relInstance = this.insideInRelationship.createRelationshipInstance(relatoinshipTarget, relationshipMetadata);
            PhysicalAssetRelationshipInstanceCreatedWldtEvent<String> relInstanceEvent = new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(relInstance);
            publishPhysicalAssetRelationshipCreatedWldtEvent(relInstanceEvent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }*/
}
