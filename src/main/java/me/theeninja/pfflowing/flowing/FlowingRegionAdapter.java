package me.theeninja.pfflowing.flowing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final String QUESTION = "question";
    private static final String ASSOCIATED_CARDS = "associatedCards";

    public static FlowingRegion readJSON(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();

        Utils.expect(jsonReader, TYPE_NAME);
        String flowingRegionTypeString = jsonReader.nextString();
        FlowingRegionType flowingRegionType = FlowingRegionType.valueOf(flowingRegionTypeString);

        Utils.expect(jsonReader, TEXT_NAME);
        String text = jsonReader.nextString();

        Utils.expect(jsonReader, COLUMN_NAME);
        int column = jsonReader.nextInt();

        Utils.expect(jsonReader, ROW_NAME);
        int row = jsonReader.nextInt();

        Utils.expect(jsonReader, QUESTION);
        String question = jsonReader.nextString();

        jsonReader.endObject();

        FlowingRegion flowingRegion = new FlowingRegion(text, flowingRegionType);
        FlowGrid.setColumnIndex(flowingRegion, column);
        FlowGrid.setRowIndex(flowingRegion, row);
        flowingRegion.getProperties().put(FlowingRegion.QUESTION_KEY, question);

        return null;
    }

    public static void addJSON(JsonWriter jsonWriter, FlowingRegion flowingRegion) throws IOException {
        jsonWriter.beginObject();

        String className = flowingRegion.getClass().getSimpleName();
        jsonWriter.name(TYPE_NAME).value(className);

        jsonWriter.name(TEXT_NAME).value(flowingRegion.getFullText());
        jsonWriter.name(COLUMN_NAME).value(FlowGrid.getColumnIndex(flowingRegion));
        jsonWriter.name(ROW_NAME).value(FlowGrid.getRowIndex(flowingRegion));

        Object questionObject = flowingRegion.getProperties().get(FlowingRegion.QUESTION_KEY);
        String question = (String) questionObject;

        jsonWriter.name(QUESTION).value(question);

        jsonWriter.endObject();
    }
}
