package me.theeninja.pfflowing.gui;

import com.google.gson.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

import java.lang.reflect.Type;

import static me.theeninja.pfflowing.tournament.Round.*;

public class RoundDeserializer implements JsonDeserializer<Round> {
    @Override
    public Round deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String roundName = jsonObject.get(NAME).getAsString();

        String sideName = jsonObject.get(SIDE).getAsString();
        Side side = Side.valueOf(sideName);

        JsonObject affFlowGridJsonElement = jsonObject.get(AFF_FLOWING_GRID).getAsJsonObject();
        FlowGrid affFlowingGrid = context.deserialize(affFlowGridJsonElement, FlowGrid.class);

        JsonObject negFlowGridJsonElement = jsonObject.get(NEG_FLOWING_GRID).getAsJsonObject();
        FlowGrid negFlowingGrid = context.deserialize(negFlowGridJsonElement, FlowGrid.class);

        return new Round(roundName, side, affFlowingGrid, negFlowingGrid);
    }
}
