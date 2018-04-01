package me.theeninja.pfflowing.gui;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.theeninja.pfflowing.flowing.FlowingRegion;

import java.lang.reflect.Type;
import java.util.List;

import static me.theeninja.pfflowing.gui.FlowGrid.FLOWING_REGIONS;

public class FlowingGridDeserializer implements JsonDeserializer<FlowGrid> {
    @Override
    public FlowGrid deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        Type listType = new TypeToken<List<FlowingRegion>>() {}.getType();


        JsonObject jsonObject = jsonElement.getAsJsonObject();

        JsonArray flowingRegionsJsonArray = jsonObject.get(FLOWING_REGIONS).getAsJsonArray();
        List<FlowingRegion> flowingRegions = context.deserialize(flowingRegionsJsonArray, listType);

        FlowGrid flowGrid = new FlowGrid();
        flowGrid.getChildren().setAll(flowingRegions);

        return flowGrid;
    }
}
