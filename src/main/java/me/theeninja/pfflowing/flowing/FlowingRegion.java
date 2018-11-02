package me.theeninja.pfflowing.flowing;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.Duplicable;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.configuration.InternalConfiguration;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.*;
import me.theeninja.pfflowing.utils.Utils;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FlowingRegion extends VBox implements Duplicable<FlowingRegion> {

    public static final String TEXT_NAME = "text";
    public static final String COLUMN_NAME = "column";
    public static final String ROW_NAME = "row";
    public static final String TYPE_NAME = "type";
    public static final String ASSOCIATED_CARDS = "associatedCards";
    public static final String ASSOCIATED_QUESTIONS = "associatedQuestions";

    private final FlowingRegionType flowingRegionType;

    private final Label reasoningLabel = new Label();
    private final VBox cardsBox = new VBox();
    private final VBox questionsBox = new VBox();

    private StringProperty fullText = new SimpleStringProperty();

    /**
     * The shortened version of full text. How short this is relative
     * to {@code fullText} is dependent on the limit specified in the
     * configuration. Since it is a dependent variable, no need to serialize.
     */
    private StringProperty shortenedText = new SimpleStringProperty();

    /**
     * Represents whether the actions region is expanded on the flow grid.
     * Since this is set to a default value (false) when a actions region is loaded on the
     * flow grid, no need to serialize.
     */
    private BooleanProperty expanded = new SimpleBooleanProperty();

    private final ObservableList<Card> associatedCards;
    private final ObservableList<String> associatedQuestions;

    public FlowingRegion(String text, FlowingRegionType flowingRegionType) {
        this(
            text,
            flowingRegionType,
            FXCollections.observableArrayList(),
            FXCollections.observableArrayList()
        );
    }

    private <T> Consumer<T> newOnAdded(final VBox container) {
        return addedValue -> {
            SupplementalBox<T> valueBox = new SupplementalBox<>(addedValue);

            container.getChildren().add(valueBox);

            // Setting 1-indexed order to pre-size + 1 is same as setting to post-size
            valueBox.setRank(container.getChildren().size());
        };
    }

    private <T> Consumer<T> newOnRemoved(final VBox container) {
        return removedValue -> {
            final List<Node> valuesBox = container.getChildren();

            for (int i = 0; i < valuesBox.size(); i++) {
                @SuppressWarnings("unchecked")
                final SupplementalBox<T> cardBox = (SupplementalBox<T>) valuesBox.get(i);

                cardBox.setRank(i + 1);

                if (cardBox.getValue() == removedValue) {
                    valuesBox.remove(cardBox);
                    i--;
                }
            }
        };
    }

    private static class SupplementalBox<T> extends HBox {
        private T value;

        private IntegerProperty rank = new SimpleIntegerProperty();

        SupplementalBox(T value) {
            this.value = value;

            setSpacing(10);

            Label rankLabel = new Label();
            rankLabel.setStyle("-fx-color: green");
            rankLabel.textProperty().bind(rankProperty().asString());

            Label valueLabel = new Label(value.toString());
            valueLabel.fontProperty().bind(EFlow.getInstance().getConfiguration().getCardLabelsFont().valueProperty());

            // Makes it so delete-button is at rightmost part of HBox
            Region separatorRegion = new Region();
            HBox.setHgrow(separatorRegion, Priority.ALWAYS);

            getChildren().addAll(rankLabel, valueLabel, separatorRegion);

        }

        public int getRank() {
            return rankProperty().get();
        }

        private IntegerProperty rankProperty() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank.set(rank);
        }

        public T getValue() {
            return value;
        }
    }

    public FlowingRegion(String text, FlowingRegionType flowingRegionType, ObservableList<Card> associatedCards, ObservableList<String> associatedQuestions) {
        this.flowingRegionType = flowingRegionType;

        getReasoningLabel().setWrapText(true);

        // Listener must be added first, before setting full text
        addFullTextListener();
        setFullText(text);

        getReasoningLabel().fontProperty().bind(EFlow.getInstance().getConfiguration().getReasoningFont().valueProperty());

        setExpanded(false);

        this.associatedCards = associatedCards;
        this.associatedQuestions = associatedQuestions;

        getAssociatedCards().addListener(Utils.generateListChangeListener(
            newOnAdded(cardsBox),
            newOnRemoved(cardsBox)
        ));

        getAssociatedQuestions().addListener(Utils.generateListChangeListener(
            newOnAdded(questionsBox),
            newOnRemoved(questionsBox)
        ));

        addDetailerSupport();

        getChildren().addAll(getReasoningLabel(), getCardsBox(), questionsBox);
    }

    public ObservableList<String> getAssociatedQuestions() {
        return this.associatedQuestions;
    }

    private void addDetailerSupport() {
        CardsDetailerController cardsDetailerController = getCardsDetailerController();

        VBox popOverVBox = new VBox();
        PopOver popOver = new PopOver(popOverVBox);

        expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && cardsDetailerController.hasDetail()) {
                cardsDetailerController.setCurrentCard(getAssociatedCards().get(0));
                popOverVBox.getChildren().add(cardsDetailerController.getCorrelatingView());
                popOver.show(this);
            }
            else {
                popOver.hide();
                popOverVBox.getChildren().clear();
                cardsDetailerController.setCurrentCard(null);
            }
        });
    }

    public void addFullTextListener() {
        fullTextProperty().addListener((observable, oldValue, newValue) -> {
            String[] seperatedStrings = getFullText().split(InternalConfiguration.LENGTH_LIMIT_TYPE.getSplit());

            List<String> limitedSeperatedStrings = Arrays.stream(seperatedStrings).limit(InternalConfiguration.LENGTH_LIMIT).collect(Collectors.toList());

            String shortenedString = String.join(InternalConfiguration.LENGTH_LIMIT_TYPE.getSplit(), limitedSeperatedStrings);

            if (seperatedStrings.length > InternalConfiguration.LENGTH_LIMIT) {
                shortenedString += "...";
            }

            setShortenedText(shortenedString);
        });
    }

    @Override
    public FlowingRegion duplicate() {
        FlowingRegion duplicate = new FlowingRegion(
            getFullText(),
            getFlowingRegionType(),
            getAssociatedCards(),
            getAssociatedQuestions()
        );
        duplicate.setStyle(this.getStyle());
        FlowGrid.setColumnIndex(duplicate, FlowGrid.getColumnIndex(this));
        FlowGrid.setRowIndex(duplicate, FlowGrid.getRowIndex(this));
        return duplicate;
    }

    private LengthLimitType lengthLimitType;
    private int limit;

    public LengthLimitType getLengthLimitType() {
        return lengthLimitType;
    }

    public void setLengthLimitType(LengthLimitType lengthLimitType) {
        this.lengthLimitType = lengthLimitType;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getShortenedText() {
        return shortenedText.get();
    }

    public StringProperty shortenedTextProperty() {
        return shortenedText;
    }

    public void setShortenedText(String shortenedText) {
        this.shortenedText.set(shortenedText);
    }

    public String getFullText() {
        return fullText.get();
    }

    public StringProperty fullTextProperty() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText.set(fullText);
    }

    public boolean getExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (expanded)
            getReasoningLabel().textProperty().bind(fullTextProperty());
        else
            getReasoningLabel().textProperty().bind(shortenedTextProperty());
        this.expanded.set(expanded);
    }

    public ObservableList<Card> getAssociatedCards() {
        return associatedCards;
    }

    private CardsDetailerController getCardsDetailerController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/card_display/card_details.fxml"));
        CardsDetailerController flowingRegionDetailController = new CardsDetailerController(this);
        fxmlLoader.setController(flowingRegionDetailController);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flowingRegionDetailController;
    }

    public FlowingRegionType getFlowingRegionType() {
        return flowingRegionType;
    }

    public boolean isProactive() {
        return getFlowingRegionType() == FlowingRegionType.PROACTIVE;
    }

    public boolean isOffensive() {
        return getFlowingRegionType() == FlowingRegionType.REFUTATION;
    }

    public boolean isExtension() {
        return getFlowingRegionType() == FlowingRegionType.EXTENSION;
    }

    public Label getReasoningLabel() {
        return reasoningLabel;
    }

    public VBox getCardsBox() {
        return cardsBox;
    }
}
