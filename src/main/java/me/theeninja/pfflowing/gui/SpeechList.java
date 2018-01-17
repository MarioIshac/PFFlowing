package me.theeninja.pfflowing.gui;

import com.google.common.collect.ImmutableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.utils.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpeechList extends ArrayList<SpeechPair> {
    private final Side side;
    private ObjectProperty<Speech> selectedSpeech = new SimpleObjectProperty<>();

    public static final Map<Side, String> SIDE_HEADERS = new HashMap<>();
    public static final String REF_PREFIX = "AT";
    public static final String PREFIX_HEADER_SEPERATOR = "-";
    public static final String HEADER_NUMBER_SEPERATOR = " ";

    static {
        SIDE_HEADERS.put(Side.AFFIRMATIVE, "Aff");
        SIDE_HEADERS.put(Side.NEGATION, "Neg");
    }

    /**
     * A subround refers to a specific exchange between speakers in a round excluding crossfires. There are 4:
     * <ol>
     *     <li>Constructive Speeches</li>
     *     <li>Rebuttal Speeches</li>
     *     <li>Summary Speeches</li>
     *     <li>Final Focus Speeches</li>
     * </ol>
     */
    public static final int NUMBER_OF_SUBROUNDS = 4;

    public SpeechList(Side side) {
        this.side = side;

        for (int subround = 0; subround < NUMBER_OF_SUBROUNDS; subround++) {
            String suffixWithSeperator = HEADER_NUMBER_SEPERATOR + (subround + 1);
            String defHead = SIDE_HEADERS.get(getSide()) + suffixWithSeperator;
            String refHead = REF_PREFIX + PREFIX_HEADER_SEPERATOR + SIDE_HEADERS.get(getSide()) + suffixWithSeperator;
            DefensiveSpeech defensiveSpeech =
                new DefensiveSpeech(getSide(), defHead, subround * 2) ;
            RefutationSpeech refutationSpeech =
                new RefutationSpeech(defensiveSpeech, getSide().getOpposite(), refHead, subround * 2 + 1);
            add(new SpeechPair(defensiveSpeech, refutationSpeech));
        }

        for (int speechIndex = 1; speechIndex < NUMBER_OF_SUBROUNDS * 2; speechIndex++) {
            Speech leftSpeech = getSpeeches().get(speechIndex - 1);
            Speech currentSpeech = getSpeeches().get(speechIndex);
            currentSpeech.proactiveStartProperty().bind(leftSpeech.proactiveEndProperty()
                    .add(1)); // Increment to get gridpane row below last proactive flowing region of previous speech
        }

        Speech firstSpeech = get(0).getFirst();

        setSelectedSpeech(firstSpeech);

        firstSpeech.setProactiveStart(0);
        firstSpeech.setProactiveEnd(0);
    }

    public Speech getOpposite(Speech speech) {
        System.out.println("Opposite" + speech.getLabelText());
        Pair<DefensiveSpeech, RefutationSpeech> pair = getPair(speech);
        if (speech instanceof DefensiveSpeech)
            return pair.getSecond();
        else
            return pair.getFirst();
    }

    private Pair<DefensiveSpeech, RefutationSpeech> getPair(Speech speech) {
        System.out.println(this.size());
        System.out.println("Pair" + speech.getLabelText());
        for (Pair<DefensiveSpeech, RefutationSpeech> pair : this) {
            System.out.println(pair.getFirst().getLabelText());
            System.out.println(pair.getSecond().getLabelText());
            if (pair.contains(speech))
                return pair;
        }
        return null;
    }

    public List<DefensiveSpeech> getDefensiveSpeeches() {
        return stream().map(Pair::getFirst).collect(Collectors.toList());
    }

    public List<RefutationSpeech> getRefutationSpeeches() {
        return stream().map(Pair::getSecond).collect(Collectors.toList());
    }

    public List<Speech> getSpeeches() {
        List<Speech> returnList = new ArrayList<>();
        for (Pair<DefensiveSpeech, RefutationSpeech> pair : this) {
            returnList.add(pair.getFirst());
            returnList.add(pair.getSecond());
        }
        return returnList;
    }
    public ObjectProperty<Speech> selectedSpeechProperty() {
        return selectedSpeech;
    }

    public Speech getSelectedSpeech() {
        return selectedSpeech.get();
    }

    public void setSelectedSpeech(Speech selectedSpeech) {
        this.selectedSpeech.set(selectedSpeech);
    }

    public Optional<Speech> findFirstSpeech(Predicate<Speech> predicate) {
        for (Speech speech : getSpeeches())
            if (predicate.test(speech))
                return Optional.of(speech);
        return Optional.empty();
    }

    public Speech getSpeech(FlowingRegion flowingRegion) {
        for (Speech speech : getSpeeches())
            if (speech.getChildren().containsValue(flowingRegion))
                return speech;
        return null;
    }

    public void selectSpeech(int offset) {
        setSelectedSpeech(Utils.getRelativeElement(
            getSpeeches(),
            getSelectedSpeech(),
            offset
        ));
    }

    public Side getSide() {
        return side;
    }

    public Optional<OffensiveFlowingRegion> getOffendor(FlowingRegion flowingRegion) {
        Speech baseSpeech = this.getSpeech(flowingRegion);
        Speech refSpeech  = Utils.getRelativeElement(getSpeeches(), baseSpeech, 1);

        int rowIndex = FlowingGrid.getRowIndex(flowingRegion);

        OffensiveFlowingRegion offendor = (OffensiveFlowingRegion) refSpeech.getChildren().get(rowIndex);

        if (offendor == null) // flowing region has not been refuted yet
            return Optional.empty();

        return Optional.of(offendor);
    }

    public Optional<ExtensionFlowingRegion> getExtension(FlowingRegion flowingRegion) {
        Speech baseSpeech = this.getSpeech(flowingRegion);
        Speech nextDefendingSpeech = Utils.getRelativeElement(getSpeeches(), baseSpeech, 2);

        int rowIndex = FlowingGrid.getRowIndex(flowingRegion);

        ExtensionFlowingRegion extension = (ExtensionFlowingRegion) nextDefendingSpeech.getChildren().get(rowIndex);

        if (flowingRegion == null) // flowingRegion has not been extended yet
            return Optional.empty();

        return Optional.of(extension);
    }
}
