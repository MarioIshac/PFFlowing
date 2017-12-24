package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.flowing.DefensiveSpeech;
import me.theeninja.pfflowing.flowing.RefutationSpeech;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpeechList extends SimpleListProperty<Pair<DefensiveSpeech, RefutationSpeech>> {
    private final Side side;
    private ObjectProperty<Speech> selectedSpeech;

    public SpeechList(Side side) {
        super(FXCollections.observableArrayList());
        this.side = side;
        for (DefensiveSpeech defensiveSpeech : DefensiveSpeech.DEFENSIVE_SPEECH_ORDER) {
            if (defensiveSpeech.getSide() == side) {
                RefutationSpeech refutationSpeech = RefutationSpeech.getRefutationSpeech(defensiveSpeech);
                add(new SpeechPair(defensiveSpeech, refutationSpeech));
            }
        }

        setFlowingColumns(FlowingColumn.of(this));

        selectedSpeech = new SimpleObjectProperty<>();
        selectedSpeechProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue != null)
                oldValue.getBinded().setBackground(Background.EMPTY);
            System.out.println(newValue);
            System.out.println(newValue.getBinded());
            newValue.getBinded().setBackground(Utils.generateBackgroundOfColor(Color.LIGHTGRAY));
        }));
        setSelectedSpeech(getSpeeches().get(0));
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
}
