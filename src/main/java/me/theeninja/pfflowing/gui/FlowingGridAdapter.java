package me.theeninja.pfflowing.gui;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionAdapter;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlowingGridAdapter extends TypeAdapter<FlowingGrid> {
    private static final String SIDE = "side";
    private static final String FLOWING_REGIONS = "flowing_regions";

    @Override
    public void write(JsonWriter jsonWriter, FlowingGrid flowingGrid) throws IOException {
        writeJSON(jsonWriter, flowingGrid);
    }

    @Override
    public FlowingGrid read(JsonReader jsonReader) throws IOException {
        return readJSON(jsonReader);
    }

    public static void writeJSON(JsonWriter jsonWriter, FlowingGrid flowingGrid) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name(SIDE).value(flowingGrid.getSide().name());
        jsonWriter.name(FLOWING_REGIONS).beginArray();
        for (FlowingRegion flowingRegion : Utils.getOfType(flowingGrid.getChildren(), FlowingRegion.class)) {
            FlowingRegionAdapter.addJSON(jsonWriter, flowingRegion);
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    public static FlowingGrid readJSON(JsonReader jsonReader) throws IOException {
        FlowingGrid flowingGrid = new FlowingGrid();

        jsonReader.beginObject();
        jsonReader.nextName(); // discards side
        String sideName = jsonReader.nextString();
        Side side = Side.valueOf(sideName);

        flowingGrid.setSide(side);

        jsonReader.nextName(); // discards flowing_regions
        jsonReader.beginArray();
        List<FlowingRegion> flowingRegions = new ArrayList<>();
        while (jsonReader.hasNext()) {
            FlowingRegion flowingRegion = FlowingRegionAdapter.readJSON(jsonReader);
            flowingRegions.add(flowingRegion);
        }
        jsonReader.endArray();
        jsonReader.endObject();

        flowingGrid.getChildren().setAll(flowingRegions);
        return flowingGrid;
    }
}
