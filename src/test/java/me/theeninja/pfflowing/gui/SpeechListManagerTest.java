package me.theeninja.pfflowing.gui;

import me.theeninja.pfflowing.flowing.DefensiveSpeech;
import me.theeninja.pfflowing.flowing.RefutationSpeech;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SpeechListManagerTest {
    private SpeechListManager speechListManager;

    @Before
    public void setUp() {
        speechListManager = new SpeechListManager(new FlowingGrid());
    }

    @Ignore
    @Test
    public void speechListOfAll_IsValid() {
        assertTrue(DefensiveSpeech.DEFENSIVE_SPEECH_ORDER.stream()
                .allMatch(defensiveSpeech ->
                        speechListManager.getSpeechList(defensiveSpeech) ==
                                speechListManager.getSpeechList(defensiveSpeech.getSide())));
        assertTrue(RefutationSpeech.REFUTATION_SPEECH_ORDER.stream()
                .allMatch(refutationSpeech ->
                        speechListManager.getSpeechList(refutationSpeech) !=
                                speechListManager.getSpeechList(refutationSpeech.getSide())));
    }

    private SpeechListManager getSpeechListManager() {
        return speechListManager;
    }
}