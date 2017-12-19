package me.theeninja.pfflowing.flowing;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

import static me.theeninja.pfflowing.flowing.RefutationSpeech.REFUTATION_SPEECH_ORDER;
import static me.theeninja.pfflowing.flowing.DefensiveSpeech.DEFENSIVE_SPEECH_ORDER;

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