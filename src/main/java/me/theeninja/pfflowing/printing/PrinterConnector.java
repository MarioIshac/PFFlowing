package me.theeninja.pfflowing.printing;

import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;

public class PrinterConnector {
    private final Printer printer;

    PrinterConnector() {
        this(Printer.getDefaultPrinter());
    }

    public PrinterConnector(Printer printer) {
        this.printer = printer;
    }

    public Printer getPrinter() {
        return printer;
    }

    public void print(Node node) {
        //PageLayout pageLayout = new PageLayout();
    }
}
