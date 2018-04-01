package me.theeninja.pfflowing.flowing;

import com.google.gson.*;
import me.theeninja.pfflowing.gui.FlowGrid;

import java.lang.reflect.Type;

import static me.theeninja.pfflowing.flowing.FlowingRegion.*;

public class FlowingRegionDeserializer implements JsonDeserializer<FlowingRegion> {
    @Override
    public FlowingRegion deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String flowingRegionTypeName = jsonObject.get(TYPE_NAME).getAsString();
        FlowingRegionType flowingRegionType = FlowingRegionType.valueOf(flowingRegionTypeName);

        String fullText = jsonObject.get(TEXT_NAME).getAsString();

        int column = jsonObject.get(COLUMN_NAME).getAsInt();
        int row = jsonObject.get(ROW_NAME).getAsInt();

        JsonElement questionJsonElement = jsonObject.get(QUESTION);
        String question;

        if (questionJsonElement.isJsonNull())
            question = null;
        else
            question = questionJsonElement.getAsString();

        FlowingRegion flowingRegion = new FlowingRegion(fullText, flowingRegionType);

        FlowGrid.setColumnIndex(flowingRegion, column);
        FlowGrid.setRowIndex(flowingRegion, row);

        flowingRegion.setQuestionText(question);

        return flowingRegion;
    }
}
