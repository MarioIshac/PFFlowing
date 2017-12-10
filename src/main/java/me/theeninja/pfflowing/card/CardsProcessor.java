package me.theeninja.pfflowing.card;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CardsProcessor {
    private final List<String> fileNames;
    private final String directory;

    public CardsProcessor(String directory) throws IOException {
        this.directory = directory;
        this.fileNames = Files.walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public String getDirectory() {
        return directory;
    }

    public List<Card> findCards() {
        if (this.fileNames.isEmpty()) {
            System.out.println("Warning: No cards to be found.");
        }
        return this.fileNames.stream()
                /* Now replace each file path with a card processor that will process the card represented
                by that file path */
                .map(CardProcessor::new)
                /* Process the cards and map each CardProcessor to the Card */
                .map(CardProcessor::generateCard)
                .collect(Collectors.toList());
    }
}
