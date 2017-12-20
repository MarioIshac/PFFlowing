package me.theeninja.pfflowing.flowing;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

import static me.theeninja.pfflowing.flowing.DefensiveSpeech.DEFENSIVE_SPEECH_ORDER;
import static me.theeninja.pfflowing.flowing.RefutationSpeech.REFUTATION_SPEECH_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RefutationSpeechTest {
    @Ignore @Test
    public void numberOfRefutationSpeeches_Equals8() {
        assertEquals(REFUTATION_SPEECH_ORDER.size(), 8);
    }

    @Ignore @Test
    public void oneToOneCorrespondenceWithDefensiveSpeeches_True() {
        for (int i = 0; i < REFUTATION_SPEECH_ORDER.size(); i++) {
            assertEquals(REFUTATION_SPEECH_ORDER.get(i).getTargetSpeech(), DEFENSIVE_SPEECH_ORDER.get(i));
        }
    }
}