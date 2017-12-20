package me.theeninja.pfflowing;

import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

class PFFlowingTest extends ApplicationTest {
    @BeforeEach
    public void start() throws Exception {
        ApplicationTest.launch(PFFlowing.class);
    }

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    @AfterEach
    public void postEachTest() throws TimeoutException {
        FxToolkit.hideStage();
    }
}