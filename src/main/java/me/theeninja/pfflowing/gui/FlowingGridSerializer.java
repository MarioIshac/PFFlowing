package me.theeninja.pfflowing.gui;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionSerializer;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.theeninja.pfflowing.gui.FlowGrid.FLOWING_REGIONS;

public class FlowingGridSerializer implements JsonSerializer<FlowGrid> {
    @Override
    public JsonElement serialize(FlowGrid flowGrid, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        List<FlowingRegion> flowingRegions = flowGrid.getChildren().stream()
                .filter(FlowingRegion.class::isInstance)
                .map(FlowingRegion.class::cast)
                .collect(Collectors.toList());

        jsonObject.add(FLOWING_REGIONS, context.serialize(flowingRegions));

        return jsonObject;
    }
}
