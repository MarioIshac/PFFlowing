package me.theeninja.pfflowing.gui;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionAdapter;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlowingGridAdapter extends TypeAdapter<FlowGrid> {
    private static final String FLOWING_REGIONS = "flowing_regions";

    @Override
    public void write(JsonWriter jsonWriter, FlowGrid flowGrid) throws IOException {
        writeJSON(jsonWriter, flowGrid);
    }

    @Override
    public FlowGrid read(JsonReader jsonReader) throws IOException {
        return readJSON(jsonReader);
    }

    public static void writeJSON(JsonWriter jsonWriter, FlowGrid flowGrid) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name(FLOWING_REGIONS).beginArray();

        for (FlowingRegion flowingRegion : Utils.getOfType(flowGrid.getChildren(), FlowingRegion.class)) {
            FlowingRegionAdapter.addJSON(jsonWriter, flowingRegion);
        }

        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    public static FlowGrid readJSON(JsonReader jsonReader) throws IOException {
        FlowGrid flowGrid = new FlowGrid();

        jsonReader.beginObject();

        Utils.expect(jsonReader, FLOWING_REGIONS);

        jsonReader.beginArray();
        List<FlowingRegion> flowingRegions = new ArrayList<>();
        while (jsonReader.hasNext()) {
            FlowingRegion flowingRegion = FlowingRegionAdapter.readJSON(jsonReader);
            flowingRegions.add(flowingRegion);
        }
        jsonReader.endArray();
        jsonReader.endObject();

        flowGrid.getChildren().setAll(flowingRegions);
        return flowGrid;
    }
}
