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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CardPossibilitiesTask extends Task {

    private final String text;
    private CardPossibilities cp;
    private void handle(String string) {
    }

    private StanfordCoreNLP pipeline;

    private void initializePipeline() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("ner.useSUTime", "0");
        setPipeline(new StanfordCoreNLP(props));
    }

    private boolean isPerson(CoreLabel coreLabel) {
        return coreLabel.ner().equals("PERSON");
    }

    private static final String SHORT_DATE_MATCH = "^([1-2][0-9])?[0-9][0-9]$";

    public boolean isValidShortDate(CoreLabel coreLabel) {
        Pattern pattern = Pattern.compile(SHORT_DATE_MATCH);
        Matcher matcher = pattern.matcher(coreLabel.word());
        return matcher.find();
    }

    private static int i(String string) {
        return Integer.parseInt(string);
    }

    private final int MAX_LOOKUP_LENGTH = 3;

    private final Map<List<Predicate<CoreLabel>>, Consumer<List<String>>> PREDICATE_MAPPER = ImmutableMap.of(
        List.of(this::isPerson, this::isPerson, this::isValidShortDate), strs -> {
            getCp().getAuthors().add(new Author(strs.get(0), strs.get(1)));
            getCp().getDates().add(Utils.calendarOf(strs.get(2)));
        },
        List.of(this::isPerson, this::isPerson), strs -> {
            getCp().getAuthors().add(new Author(strs.get(0), strs.get(1)));
        },
        List.of(this::isPerson, this::isValidShortDate), strs -> {
            getCp().getAuthors().add(new Author(strs.get(0)));
            getCp().getDates().add(Utils.calendarOf(strs.get(1)));
        },
        List.of(this::isPerson), strs -> {
            getCp().getAuthors().add(new Author(strs.get(0)));
        }
    );

    private void populateCardPossibilitiesd(String text) {
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        getPipeline().annotate(document);

        List<CoreMap> coreMaps =  document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence : coreMaps) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

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
                updateProgress(index, requiredEndIndex);

                /*CoreLabel currentToken = tokens.get(index);
                CoreLabel oneTokenAhead = tokens.get(index + 1);
                CoreLabel twoTokensAhead = tokens.get(index + 2);*/

                for (Map.Entry<List<Predicate<CoreLabel>>, Consumer<List<String>>> entry : PREDICATE_MAPPER.entrySet()) {
                    // The maximum portion of tokens that we are going to be testing against
                    // We must take the max as we must be ready to supply the largest collection
                    // of predicates in the PREDICATE_MAPPER with its necessary testing arguments
                    List<CoreLabel> coreLabels = IntStream.range(index, index + MAX_LOOKUP_LENGTH)
                            .mapToObj(tokens::get)
                            .collect(Collectors.toList());

                    List<Predicate<CoreLabel>> predicates = entry.getKey();

                    //
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
                        entry.getValue().accept(coreLabelStrings);

                        index += entry.getKey().size() - 1;

                        // No need to test any more lists of predicates against the arguments, as we have
                        // matched the highest priority one.
                        break;
                    }
                }

                /*if (!isPerson(currentToken))
                    continue;
                if (isPerson(oneTokenAhead)) {
                    if (isValidShortDate(twoTokensAhead)) {
                        getCp().getDates().add(Utils.calendarOf(i("20" + twoTokensAhead.word())));
                        index++;
                    }

                    getCp().getAuthors().add(new Author(currentToken.word(), oneTokenAhead.word()));
                } else if (isValidShortDate(oneTokenAhead)) {
                    getCp().add(Utils.calendarOf(i(oneTokenAhead.word())));
                    getCp().getAuthors().add(new Author(currentToken.word()));
                }
                index++;*/
            }
        }
    }

    public CardPossibilitiesTask(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CardPossibilities call() {
        initializePipeline();
        setCp(new CardPossibilities(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        ));
        populateCardPossibilitiesd("Bernstein 17 finds that 80% of the wealth created in the stock market is owned by the 1%. Only the wealthy are alrgely imapcted by the capital gains tax");
        return getCp();
    }

    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }

    public void setPipeline(StanfordCoreNLP pipeline) {
        this.pipeline = pipeline;
    }

    public CardPossibilities getCp() {
        return cp;
    }

    public void setCp(CardPossibilities cp) {
        this.cp = cp;
    }

    public String getText() {
        return text;
    }
}
