package me.theeninja.pfflowing.gui;

import com.google.common.collect.Iterables;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.utils.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpeechList extends SimpleListProperty<Pair<DefensiveSpeech, RefutationSpeech>> {
    private Speech selectedSpeech;

    public SpeechList(Side side) {
        super(FXCollections.observableArrayList());
        for (DefensiveSpeech defensiveSpeech : DefensiveSpeech.DEFENSIVE_SPEECH_ORDER) {
            if (defensiveSpeech.getSide() == side) {
                RefutationSpeech refutationSpeech = RefutationSpeech.getRefutationSpeech(defensiveSpeech);
                add(new Pair<>(defensiveSpeech, refutationSpeech));
            }
        }
    }

    public void setFlowingColumns(List<FlowingColumn> flowingColumns) {
        for (int i = 0; i < flowingColumns.size(); i++) {
            this.getSpeeches().get(i).setBinded(flowingColumns.get(i));
        }
    }

    public Speech getOpposite(Speech speech) {
        Pair<DefensiveSpeech, RefutationSpeech> pair = getPair(speech);
        if (speech instanceof DefensiveSpeech)
            return pair.getSecond();
        else
            return pair.getFirst();
    }

    private Pair<DefensiveSpeech, RefutationSpeech> getPair(Speech speech) {
        for (Pair<DefensiveSpeech, RefutationSpeech> pair : this) {
            if (speech instanceof DefensiveSpeech)
                if (pair.getFirst() == speech)
                    return pair;
            else
                if (pair.getSecond() == speech)
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

    public Speech getSelectedSpeech() {
        return selectedSpeech;
    }

    public void setSelectedSpeech(Speech selectedSpeech) {
        this.selectedSpeech = selectedSpeech;
    }

    public Optional<Speech> findFirstSpeech(Predicate<Speech> predicate) {
        for (Speech speech : getSpeeches())
            if (predicate.test(speech))
                return Optional.of(speech);
        return Optional.empty();
    }
}
