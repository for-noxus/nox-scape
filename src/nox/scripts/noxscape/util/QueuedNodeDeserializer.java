package nox.scripts.noxscape.util;

import com.google.gson.*;
import nox.scripts.noxscape.core.QueuedNode;
import nox.scripts.noxscape.core.StopWatcher;

import java.lang.reflect.Type;

public class QueuedNodeDeserializer implements JsonDeserializer<QueuedNode> {

    @Override
    public QueuedNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();

        QueuedNode node = new QueuedNode();

        node.className = obj.get("className").getAsString();
        node.stopWatcher = ctx.deserialize(obj.get("stopWatcher"), StopWatcher.class);
        if (obj.has("configuration")) {
            try {
                node.configClassName = obj.get("configClassName").getAsString();
                Class configClass = Class.forName(node.configClassName);
                node.configuration = ctx.deserialize(obj.get("configuration"), configClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return node;
    }
}