package me.theeninja.pfflowing.gui.cardparser;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.EFlow;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class OfflineFileFetcher extends FileFetcher<Path> {
    @Override
    protected String getHTMLOfFile(Path path) throws IOException {
        ContentHandler handler = new ToXMLContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();

        try (InputStream stream = Files.newInputStream(path)) {
            parser.parse(stream, handler, metadata);
            return handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void feedFetchedFile(Consumer<Path> fileConsumerCallback) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selected File to Parse into Block");

        Stage allocatedStage = new Stage();
        File file = fileChooser.showOpenDialog(allocatedStage);
        Path path = file.toPath();

        fileConsumerCallback.accept(path);
    }
}
