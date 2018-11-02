package me.theeninja.pfflowing.flowing;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.theeninja.pfflowing.flowing.FlowingRegion.*;

public class FlowingRegionSerializer implements JsonSerializer<FlowingRegion> {
    @Override
    public JsonElement serialize(FlowingRegion flowingRegion, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        FlowingRegionType flowingRegionType = flowingRegion.getFlowingRegionType();
        jsonObject.add(TYPE_NAME, context.serialize(flowingRegionType));

        String fullText = flowingRegion.getFullText();
        jsonObject.add(TEXT_NAME, context.serialize(fullText));

        int column = FlowGrid.getColumnIndex(flowingRegion);
        jsonObject.add(COLUMN_NAME, context.serialize(column));

        int row = FlowGrid.getRowIndex(flowingRegion);
        jsonObject.add(ROW_NAME, context.serialize(row));

        List<String> questions = flowingRegion.getAssociatedQuestions();
        jsonObject.add(ASSOCIATED_QUESTIONS, context.serialize(questions));

        List<Card> associatedCards = flowingRegion.getAssociatedCards();
        jsonObject.add(ASSOCIATED_CARDS, context.serialize(associatedCards));

        return jsonObject;
    }
}
