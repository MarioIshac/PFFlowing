package me.theeninja.pfflowing.gui;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

import java.io.IOException;

public class RoundAdapter extends TypeAdapter<Round> {
    @Override
    public void write(JsonWriter jsonWriter, Round round) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("side").value(round.getSide().name());
        jsonWriter.beginArray();
        FlowingGridAdapter.writeJSON(jsonWriter, round.getAffController().getCorrelatingView());
        FlowingGridAdapter.writeJSON(jsonWriter, round.getNegController().getCorrelatingView());
        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    @Override
    public Round read(JsonReader jsonReader) throws IOException {
        Side side;

        jsonReader.beginObject();

        if (!jsonReader.nextName().equals("side")) {
            throw new JsonParseException("Expected name side");
        }

        side = Side.valueOf(jsonReader.nextString());

        jsonReader.beginArray();
        FlowingGrid aff = FlowingGridAdapter.readJSON(jsonReader);
        FlowingGrid neg = FlowingGridAdapter.readJSON(jsonReader);

        jsonReader.endArray();
        Round round = new Round(aff, neg, Side.AFFIRMATIVE);
        round.setName("a");
        return round;
    }
}
