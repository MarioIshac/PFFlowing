package me.theeninja.pfflowing.gui.cardparser;

import com.google.common.collect.ImmutableMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import javafx.concurrent.Task;
import me.theeninja.pfflowing.utils.Utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CardPossibilitiesPopulatorTask extends Task<CardPossibilities> {

    public CardPossibilitiesPopulatorTask(StanfordCoreNLP pipeline, String text) {
        this.pipeline = pipeline;
        this.text = text;
        this.cardPossibilities = new CardPossibilities(
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new LinkedHashSet<>()
        );
    }

    private final String text;
    private final CardPossibilities cardPossibilities;
    private final StanfordCoreNLP pipeline;

    private void populateCardPossibilties(String text) {
        // create an empty Annotation just with the given text
        CoreDocument document = new CoreDocument(text);

        updateMessage("Annotating the document...");
        // run all Annotators on this text
        getPipeline().annotate(document);

        updateMessage("Splitting document into sentences...");
        List<CoreSentence> coreSentences = document.sentences();

        coreSentences.forEach(coreSentence -> {
            coreSentence.entityMentions().forEach(coreEntityMention -> {
                System.out.println(coreEntityMention.text());
                System.out.println(coreEntityMention.entityType());
                System.out.println();

                switch (coreEntityMention.entityType()) {
                    case "PERSON": {
                        getCardPossibilities().getAuthors().add(coreEntityMention.text());
                        break;
                    }
                    case "DATE": {
                        getCardPossibilities().getDates().add(coreEntityMention.text());
                        break;
                    }
                    case "ORGANIZATION": {
                        getCardPossibilities().getAuthors().add(coreEntityMention.text());
                        break;
                    }
                    case "URL": {
                        getCardPossibilities().getSources().add(coreEntityMention.text());
                        break;
                    }
                }
            });
        });

        updateMessage("Searching each sentence for relevant tokens...");

        updateMessage("Finished possibility population.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CardPossibilities call() {
        populateCardPossibilties(getText());
        return cardPossibilities;
    }

    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }

    public CardPossibilities getCardPossibilities() {
        return cardPossibilities;
    }

    public String getText() {
        return text;
    }
}
