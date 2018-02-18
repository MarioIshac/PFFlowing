package me.theeninja.pfflowing.gui;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.tournament.Round;

import java.io.IOException;

public class RoundAdapter extends TypeAdapter<Round> {
    @Override
    public void write(JsonWriter jsonWriter, Round round) throws IOException {
        jsonWriter.beginArray();
        FlowingGridAdapter.writeJSON(jsonWriter, round.getAffController().getCorrelatingView());
        FlowingGridAdapter.writeJSON(jsonWriter, round.getNegController().getCorrelatingView());
        jsonWriter.endArray();
    }

    @Override
    public Round read(JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();
        FlowingGrid aff = FlowingGridAdapter.readJSON(jsonReader);
        FlowingGrid neg = FlowingGridAdapter.readJSON(jsonReader);
        jsonReader.endArray();
        return new Round(aff, neg);
    }
}
