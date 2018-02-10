package me.theeninja.pfflowing.gui.cardparser;

import com.google.common.collect.ImmutableMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import javafx.concurrent.Task;
import me.theeninja.pfflowing.flowingregions.Author;
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
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

    private final String text;
    private final CardPossibilities cardPossibilities;
    private final StanfordCoreNLP pipeline;

    private static final int MAX_LOOKUP_LENGTH = 3;

    private static final Map<List<Predicate<CoreLabel>>, BiConsumer<List<String>, CardPossibilities>> PREDICATE_MAPPER = ImmutableMap.of(
            List.of(Testers::person,
                    Testers::person,
                    Testers::shortDate),

            (strs, cp) -> {
                cp.getAuthors().add(new Author(strs.get(0), strs.get(1)));
                cp.getDates().add(Utils.calendarOf(strs.get(2)));
            },

            List.of(Testers::person,
                    Testers::person),

            (strs, cp) ->
                cp.getAuthors().add(new Author(strs.get(0), strs.get(1))),

            List.of(Testers::person, Testers::shortDate),

            (strs, cp) -> {
                cp.getAuthors().add(new Author(strs.get(0)));
                cp.getDates().add(Utils.calendarOf(strs.get(1)));
            },

            List.of(Testers::url), (str, cp) -> {
                cp.getSources().add(str.get(0));
            },

            List.of(Testers::person),

            (strs, cp) -> cp.getAuthors().add(new Author(strs.get(0)))
    );

    private void populateCardPossibilties(String text) {
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        updateMessage("Annotating the document...");
        // run all Annotators on this text
        getPipeline().annotate(document);

        updateMessage("Splitting document into sentences...");
        List<CoreMap> coreMaps = document.get(CoreAnnotations.SentencesAnnotation.class);

        int numberOfSentences = coreMaps.size();
        updateMessage("Searching each sentence for relevant tokens...");
        for (int sentenceIndex = 0; sentenceIndex < coreMaps.size(); sentenceIndex++) {
            CoreMap sentence = coreMaps.get(sentenceIndex);
            updateProgress(sentenceIndex + 1, numberOfSentences);
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

            System.out.println();
            tokens.forEach(coreLabel -> {
                System.out.println("`" + coreLabel.word() + "`" + coreLabel.ner());
            });
            System.out.println();

            /*
            1) <person> /token/ /token/
            2a) <person> <person> /token/ 2b) <person> <shortened date> /token/
            3) <person> <person> <shortened date>

            with the following examples:

            1) Lockie
            2a) Mark Lockie 2b) Lockie 17
            3) Mark Lockie 17

            These are four token structures that require interest, where the ones at the bottom
            have higher priority than the ones at the top. (2a and 2b's occurences are mutually exclusive,
            hence we can consider them at the same priority).

            Through priority, if we are given a sentence such as "/token/ Lockie 17 /token/", the below
            algorithm will match Lockie 17 (2b) rather than Lockie 17 (1).
             */
            int requiredEndIndex = tokens.size() - MAX_LOOKUP_LENGTH + 1;

            for (int index = 0; index < requiredEndIndex; index++) {
                double portionOfSentenceCompleted = index / requiredEndIndex;

                updateProgress(sentenceIndex + portionOfSentenceCompleted, numberOfSentences);

                for (Map.Entry<List<Predicate<CoreLabel>>, BiConsumer<List<String>, CardPossibilities>> entry : PREDICATE_MAPPER.entrySet()) {
                    // The maximum portion of tokens that we are going to be testing against
                    // We must take the max as we must be ready to supply the largest collection
                    // of predicates in the PREDICATE_MAPPER with its necessary testing arguments
                    List<CoreLabel> coreLabels = IntStream.range(index, index + MAX_LOOKUP_LENGTH)
                            .mapToObj(tokens::get)
                            .collect(Collectors.toList());

                    List<Predicate<CoreLabel>> predicates = entry.getKey();

                    boolean passesTest = true;

                    // This for-loop stops iterating once there are no more predicates despite a possibility
                    // that there may be untested tokens remaining. This is because lower down
                    // the PREDICATE_MAPPER, not all lists of predicates will test for a list of arguments
                    // with length of MAX_LOOKUP_LENGTH. If we do not test a token, it is implicitly true
                    // i.e it is irrelevant to whether the whole test passes as a whole or not.

                    for (int i = 0; i < predicates.size(); i++) {
                        CoreLabel targetToken = coreLabels.get(i);

                        Predicate<CoreLabel> targetPredicate = predicates.get(i);
                        if (!targetPredicate.test(targetToken))
                            passesTest = false;
                    }

                    if (passesTest) {
                        List<String> coreLabelStrings = coreLabels.stream()
                                .map(CoreLabel::word)
                                .collect(Collectors.toList());
                        entry.getValue().accept(coreLabelStrings, getCardPossibilities());

                        index += entry.getKey().size() - 1;

                        // No need to test any more lists of predicates against the arguments, as we have
                        // matched the highest priority one.
                        break;
                    }
                }
            }
        }

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
