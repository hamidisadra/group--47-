package ir.ac.pvz.model.travel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class QuestTypeAdapter implements JsonSerializer<Quest>, JsonDeserializer<Quest> {
    private static final String TYPE_FIELD = "questType";

    @Override
    public JsonElement serialize(Quest source, Type type, JsonSerializationContext context) {
        JsonObject object = context.serialize(source, source.getClass()).getAsJsonObject();
        object.addProperty(TYPE_FIELD, source.getClass().getSimpleName());
        return object;
    }

    @Override
    public Quest deserialize(JsonElement element, Type type, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        if (!object.has(TYPE_FIELD)) {
            return context.deserialize(element, EpicQuest.class);
        }

        String questType = object.get(TYPE_FIELD).getAsString();

        switch (questType) {
            case "StoryQuest":
                return context.deserialize(element, StoryQuest.class);

            case "DailyQuest":
                return context.deserialize(element, DailyQuest.class);

            case "RepeatableQuest":
                return context.deserialize(element, RepeatableQuest.class);

            case "EpicQuest":
            default:
                return context.deserialize(element, EpicQuest.class);
        }
    }
}
