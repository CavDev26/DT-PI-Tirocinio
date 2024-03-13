package io.github.wldt.demo.physical;

import com.pi4j.io.gpio.digital.*;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.ArrayList;
import java.util.Map;


public class RaspBConfPhysicalAdapter extends ConfigurablePhysicalAdapter<RaspBPhysicalAdapterConfiguration> {

    public RaspBConfPhysicalAdapter(String id, RaspBPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
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

    /**
     *
     * @return
     */
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

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if (physicalAssetActionWldtEvent != null && getConfiguration().getMapOutput().values().stream().anyMatch(obj -> obj.contains(physicalAssetActionWldtEvent.getActionKey()))) {
                getConfiguration().getMapOutput().forEach((k, v) -> {

                    if(v.contains(physicalAssetActionWldtEvent.getActionKey())) {
                        System.out.println("[RaspPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                                + "with Body: " + physicalAssetActionWldtEvent.getBody() + "\n");
                        try {
                            PhysicalAssetPropertyWldtEvent<?> newPhysicalPropertyEvent = getConfiguration().actionHandlerOutput(physicalAssetActionWldtEvent.getBody(), k);
                            if (newPhysicalPropertyEvent != null){
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                            } else {
                                //Caso in cui non c'è nessun update delle property e si necessita la sola esecuzione dell'action.
                            }
                        } catch (EventBusException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });


            }else if (physicalAssetActionWldtEvent != null && getConfiguration().getMapInput().values().stream().anyMatch(obj -> obj.contains(physicalAssetActionWldtEvent.getActionKey()))) {
                getConfiguration().getMapInput().forEach((k, v) -> {
                    if(v.contains(physicalAssetActionWldtEvent.getActionKey())) {
                        System.out.println("[RaspPhysicalAdapter] -> Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                                + "with Body: " + physicalAssetActionWldtEvent.getBody() + "\n");
                        try {
                            PhysicalAssetPropertyWldtEvent<?> newPhysicalPropertyEvent = getConfiguration().actionHandlerInput(physicalAssetActionWldtEvent.getBody(), k);
                            if (newPhysicalPropertyEvent != null){
                                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                            } else {
                                //Caso in cui non c'è nessun update delle property e si necessita la sola esecuzione dell'action.
                            }
                        } catch (EventBusException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } else {
                System.err.println("[RaspPhysicalAdapter] -> Wrong action received!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    private Runnable deviceEmulation() {
        return () -> {
            try{
                System.out.println("[RaspPhysicalAdapter] -> Starting physical device (PI)...");
                System.out.println("[RaspPhysicalAdapter] -> Printing PI4J Registry of Sensors:");
                System.out.println(getConfiguration().getPI4J().registry().all() + "\n");
                Thread.sleep(1000);
                this.getConfiguration().startListeners();
                int i = 0;
                while (i < getConfiguration().getMaximumEvents()) {
                    if(!getConfiguration().getEventsMap().isEmpty()) {
                        getConfiguration().getEventsMap().forEach( (e, b) -> {
                            try {
                                publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(e, b));
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                        //TODO da verificare quanto valga size
                        i = i + getConfiguration().getEventsMap().size();
                        getConfiguration().getEventsMap().clear();
                    }
                }
                System.out.println("\n Maximum number of events - closing PI4J \n");
                getConfiguration().getPI4J().shutdown();
            }catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    //TODO Fix the relationShipInstance Situation.
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
