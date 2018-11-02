package me.theeninja.pfflowing.flowing;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.collections.ObservableList;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.FlowGrid;

import java.lang.reflect.Type;
import java.util.List;

import static me.theeninja.pfflowing.flowing.FlowingRegion.*;

public class FlowingRegionDeserializer implements JsonDeserializer<FlowingRegion> {
    private static final Type CARDS_LIST_TYPE = new TypeToken<ObservableList<Card>>() {}.getType();
    private static final Type QUESTIONS_LIST_TYPE = new TypeToken<ObservableList<String>>() {}.getType();

    @Override
    public FlowingRegion deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String flowingRegionTypeName = jsonObject.get(TYPE_NAME).getAsString();
        FlowingRegionType flowingRegionType = FlowingRegionType.valueOf(flowingRegionTypeName);

        String fullText = jsonObject.get(TEXT_NAME).getAsString();

        int column = jsonObject.get(COLUMN_NAME).getAsInt();
        int row = jsonObject.get(ROW_NAME).getAsInt();

        JsonArray associatedCardsArray = jsonObject.get(ASSOCIATED_CARDS).getAsJsonArray();
        JsonArray associatedQuestionsArray = jsonObject.get(ASSOCIATED_CARDS).getAsJsonArray();

        ObservableList<Card> cards = EFlow.getInstance().getGSON().fromJson(associatedCardsArray, CARDS_LIST_TYPE);
        ObservableList<String> questions = EFlow.getInstance().getGSON().fromJson(associatedQuestionsArray, QUESTIONS_LIST_TYPE);

        FlowingRegion flowingRegion = new FlowingRegion(fullText, flowingRegionType);

        FlowGrid.setColumnIndex(flowingRegion, column);
        FlowGrid.setRowIndex(flowingRegion, row);

        return flowingRegion;
    }
}
