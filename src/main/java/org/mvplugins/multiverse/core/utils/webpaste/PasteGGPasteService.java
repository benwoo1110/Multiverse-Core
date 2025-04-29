package org.mvplugins.multiverse.core.utils.webpaste;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Pastes to {@code paste.gg}.
 */
final class PasteGGPasteService extends PasteService {
    private final boolean isPrivate;
    private static final String PASTEGG_POST_REQUEST = "https://api.paste.gg/v1/pastes";

    PasteGGPasteService(boolean isPrivate) {
        super(PASTEGG_POST_REQUEST);
        this.isPrivate = isPrivate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String encodeData(String data) {
        Map<String, String> mapData = new HashMap<>();
        mapData.put("multiverse.txt", data);
        return this.encodeData(mapData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String encodeData(Map<String, String> files) {
        JSONObject root = new JSONObject();
        root.put("name", "Multiverse-Core Debug Info");
        root.put("visibility", this.isPrivate ? "unlisted" : "public");
        JSONArray fileList = new JSONArray();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            JSONObject fileObject = new JSONObject();
            JSONObject contentObject = new JSONObject();
            fileObject.put("name", entry.getKey());
            fileObject.put("content", contentObject);
            contentObject.put("format", "text");
            contentObject.put("value", entry.getValue());
            fileList.add(fileObject);
        }

        root.put("files", fileList);
        return root.toJSONString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String postData(String data) throws PasteFailedException {
        try {
            String stringJSON = this.exec(encodeData(data), ContentType.JSON);
            return (String) ((JSONObject) ((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(stringJSON)).get("result")).get("id");
        } catch (IOException | ParseException e) {
            throw new PasteFailedException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String postData(Map<String, String> data) throws PasteFailedException {
        try {
            String stringJSON = this.exec(encodeData(data), ContentType.JSON);
            return "https://paste.gg/" + ((JSONObject) ((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(stringJSON)).get("result")).get("id");
        } catch (IOException | ParseException e) {
            throw new PasteFailedException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsMultiFile() {
        return true;
    }
}
