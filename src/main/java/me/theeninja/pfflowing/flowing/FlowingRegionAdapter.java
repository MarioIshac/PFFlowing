package me.theeninja.pfflowing.flowing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;

public class FlowingRegionAdapter extends TypeAdapter<FlowingRegion> {
    @Override
    public void write(JsonWriter jsonWriter, FlowingRegion flowingRegion) throws IOException {
        addJSON(jsonWriter, flowingRegion);
    }

    @Override
    public FlowingRegion read(JsonReader jsonReader) throws IOException {
        return readJSON(jsonReader);
    }

    private static final String TEXT_NAME = "text";
    private static final String COLUMN_NAME = "column";
    private static final String ROW_NAME = "row";
    private static final String TYPE_NAME = "type";

    public static FlowingRegion readJSON(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();

        Utils.expect(jsonReader, TYPE_NAME);


        Utils.expect(jsonReader, TEXT_NAME);
        FlowingRegion flowingRegion = new FlowingRegion(jsonReader.nextString());

        Utils.expect(jsonReader, COLUMN_NAME);
        FlowGrid.setColumnIndex(flowingRegion, jsonReader.nextInt());

        Utils.expect(jsonReader, ROW_NAME);
        FlowGrid.setRowIndex(flowingRegion, jsonReader.nextInt());


        jsonReader.endObject();
        return flowingRegion;
    }

    public static void addJSON(JsonWriter jsonWriter, FlowingRegion flowingRegion) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name(TEXT_NAME).value(flowingRegion.getFullText());
        jsonWriter.name(COLUMN_NAME).value(FlowGrid.getColumnIndex(flowingRegion));
        jsonWriter.name(ROW_NAME).value(FlowGrid.getRowIndex(flowingRegion));
        jsonWriter.endObject();
    }
}
