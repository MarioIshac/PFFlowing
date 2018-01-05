package me.theeninja.pfflowing.gui;

import javafx.scene.layout.BorderPane;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.utils.Utils;
import org.apache.commons.collections4.ListUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlowingPane extends BorderPane {
    private static final Function<ExtensionFlowingRegion, FlowingRegion> extensionBaseFunction;
    private static final Function<OffensiveFlowingRegion, FlowingRegion> offensiveBaseFunction;

    static {
        extensionBaseFunction = ExtensionFlowingRegion::getBase;
        offensiveBaseFunction = OffensiveFlowingRegion::getTargetRegion;
    }

    // TODO: 1/4/18 - rewrite using generics
    public void organizeArrows() {
        SpeechList speechList = FlowingGridController.getFXMLInstance().getSpeechListManager().getSelectedSpeechList();
        for (Speech speech : speechList.getSpeeches()) {

            // No need to sort the first flowing column, as it is completely proactive
            if (speechList.getSpeeches().indexOf(speech) == 0)
                continue;

            ContentContainer contentContainer = speech.getBinded().getContentContainer();

            contentContainer.getChildren().setAll(this.variableSort(contentContainer.getContent(), ListUtils.union(contentContainer.getBaseContent(), contentContainer.getRefContent()),
                    (first, second) -> {
                        FlowingRegion firstBase = extensionBaseFunction.apply((ExtensionFlowingRegion) first);
                        FlowingRegion secondBase = extensionBaseFunction.apply((ExtensionFlowingRegion) second);
                        int firstTargetIndex = Utils.getChildIndex(firstBase.getContentContainer(), firstBase);
                        int secondTargetIndex = Utils.getChildIndex(secondBase.getContentContainer(), secondBase);
                        return Integer.compare(firstTargetIndex, secondTargetIndex);
                    }));

        }

        for (Speech speech : speechList.getSpeeches()) {

            // No need to sort the first flowing column, as it is completely proactive
            if (speechList.getSpeeches().indexOf(speech) == 0)
                continue;

            ContentContainer contentContainer = speech.getBinded().getContentContainer();

            contentContainer.getChildren().setAll(this.variableSort(contentContainer.getContent(), ListUtils.union(contentContainer.getBaseContent(), contentContainer.getExtensionContent()),
                    (first, second) -> {
                        FlowingRegion firstBase = offensiveBaseFunction.apply((OffensiveFlowingRegion) first);
                        FlowingRegion secondBase = offensiveBaseFunction.apply((OffensiveFlowingRegion) second);
                        int firstTargetIndex = Utils.getChildIndex(firstBase.getContentContainer(), firstBase);
                        int secondTargetIndex = Utils.getChildIndex(secondBase.getContentContainer(), secondBase);
                        return Integer.compare(firstTargetIndex, secondTargetIndex);
                    }));

        }

        Utils.getOfType(getChildren(), FlowingLink.class).forEach(FlowingLink::rebindProperties);

    }

    /*
    Given an array such as E3 E2 E1 _ E4 E6 _ E5 (in this example, of size 8)
    the following algorithm sorts the elements exclusive of the static ones (in this example,
    the _). Therefore, the output for this example would be:

    E1 E2 E3 _ E4 E5 _ E6, not E1 E2 E3 E4 E5 E6 _ _ or _ _ E1 E2 E3 E4 E5 E6

    */
    private List<FlowingRegion> variableSort(List<FlowingRegion> oldWhole, List<? extends FlowingRegion> statics, Comparator<? super FlowingRegion> comparator) {
        List<Integer> staticPosList = statics.stream().map(oldWhole::indexOf).collect(Collectors.toList());
        Predicate<FlowingRegion> variableElementFilter = element -> !staticPosList.contains(oldWhole.indexOf(element));
        List<FlowingRegion> variableElements = oldWhole.stream().filter(variableElementFilter).sorted(comparator).collect(Collectors.toList());
        List<FlowingRegion> newWhole = new ArrayList<>();

        int wholeIndex = 0, variableIndex = 0;
        while (wholeIndex < oldWhole.size()) {
            if (staticPosList.contains(wholeIndex))
                newWhole.add(statics.get(wholeIndex - variableIndex));

            else {
                newWhole.add(variableElements.get(variableIndex));
                variableIndex++;
            }

            wholeIndex++;
        }

        return newWhole;
    }

    public void addLink(FlowingLink flowingLink) {
        getChildren().add(flowingLink);
        organizeArrows();
    }
}
