package me.theeninja.pfflowing.gui;

import com.google.gson.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

import java.lang.reflect.Type;

import static me.theeninja.pfflowing.tournament.Round.*;

public class RoundSerializer implements JsonSerializer<Round> {
    @Override
    public JsonElement serialize(Round round, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        String roundName = round.getRoundName();
        jsonObject.add(NAME, context.serialize(roundName));

        Side side = round.getSide();
        String sideName = side.name();
        jsonObject.add(SIDE, context.serialize(sideName));

        FlowGrid affirmativeFlowGrid = round.getAffirmativeController().flowGrid;
        jsonObject.add(AFF_FLOWING_GRID, context.serialize(affirmativeFlowGrid));

        FlowGrid negationFlowingGrid = round.getNegationController().flowGrid;
        jsonObject.add(NEG_FLOWING_GRID, context.serialize(negationFlowingGrid));

        return jsonObject;
    }
}
