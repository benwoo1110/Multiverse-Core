package org.mvplugins.multiverse.core.utils.webpaste;

import java.io.IOException;
import java.util.Map;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * A {@link URLShortener} using {@code bit.ly}. Requires an access token.
 */
final class BitlyURLShortener extends URLShortener {
    private static final String ACCESS_TOKEN = "Bearer @bitly-access-token@";
    private static final String BITLY_POST_REQUEST = "https://api-ssl.bitly.com/v4/shorten";

    BitlyURLShortener() {
        super(BITLY_POST_REQUEST, ACCESS_TOKEN);
        if (ACCESS_TOKEN.endsWith("access-token")) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String encodeData(String data) {
        JSONObject json = new JSONObject();
        json.put("long_url", data);
        return json.toJSONString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String encodeData(Map<String, String> data) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String shorten(String longUrl) {
        try {
            String stringJSON = this.exec(encodeData(longUrl), ContentType.JSON);
            return (String) ((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(stringJSON)).get("link");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return longUrl;
        }
    }
}
