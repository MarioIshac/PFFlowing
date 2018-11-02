package me.theeninja.pfflowing.printing;

import javafx.concurrent.Task;
import javafx.print.PageLayout;
import javafx.print.PrintSides;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import me.theeninja.pfflowing.gui.FlowDisplay;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowingPane;
import me.theeninja.pfflowing.tournament.Round;

public class RoundPrinter {
    public static void print(Round round) {
        Runnable printRunnable = new PrintTask(round);
        Thread printThread = new Thread(printRunnable);

        printThread.start();
    }

    private static class PrintTask extends Task<Void> {
        private final Round round;

        PrintTask(Round round) {
            this.round = round;
        }

        @Override
        protected Void call() {
            PrinterJob printerJob = PrinterJob.createPrinterJob();

            Stage allocatedStage = new Stage();

            boolean hasFinishedSetup = printerJob.showPageSetupDialog(allocatedStage);
            printerJob.getJobSettings().setPrintSides(PrintSides.DUPLEX);

            // Should always be finished, as showPageSetupDialog waits until page setup has been configured
            // (and thus user has finished configuration) before allowing the rest of the code to occur
            if (!hasFinishedSetup)
                // User has decided not to print
                return null;

            for (FlowDisplayController flowDisplayController : getRound().getSideControllers()) {
                FlowDisplay flowDisplay = flowDisplayController.getCorrelatingView();

                boolean hadSuccessfulRendering = printerJob.printPage(flowDisplay);

                if (hadSuccessfulRendering)
                    printerJob.endJob();
            }

            return null;
        }

        public Round getRound() {
            return round;
        }
    }
}