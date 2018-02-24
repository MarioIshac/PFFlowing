package me.theeninja.pfflowing.tournament;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.gui.FlowingGridAdapter;
import me.theeninja.pfflowing.speech.Side;

import java.io.IOException;

public class RoundAdapter extends TypeAdapter<Round> {
    private static final String NAME = "name";
    private static final String SIDE = "side";
    private static final String FLOWING_GRIDS = "flowing_grids";

    @Override
    public void write(JsonWriter jsonWriter, Round round) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name(NAME).value(round.getName());
        jsonWriter.name(SIDE).value(round.getSide().name());
        jsonWriter.name(FLOWING_GRIDS).beginArray();
        FlowingGridAdapter.writeJSON(jsonWriter, round.getAffController().flowGrid);
        FlowingGridAdapter.writeJSON(jsonWriter, round.getNegController().flowGrid);
        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    @Override
    public Round read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        if (!jsonReader.nextName().equals(NAME)) {

        }

        Side side;
        return null;
    }
}
