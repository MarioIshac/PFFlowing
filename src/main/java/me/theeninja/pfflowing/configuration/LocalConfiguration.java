package me.theeninja.pfflowing.configuration;

import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Holds constants that are specific to each project a user creates.
 */
public class LocalConfiguration {
    public Specification<Speaker> SPEAKER = new Specification<>(List.of(Speaker.values()), "Speaker", Speaker::valueOf);
    public Specification<String> TOPIC = new Specification<>("Topic", string -> string);


}
