package com.futurecoders.smartexchange;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class BnmExchangeParser {

    private static final String BASE_URL = "https://www.bnm.md/en/official_exchange_rates";

    /**
     * Fetches the list of available ISO 4217 currency codes (e.g., "USD", "EUR") for the given date.
     * If dateStr is null, fetches for the current date.
     *
     * @param dateStr Date in "dd.mm.yyyy" format, or null for current.
     * @return List of ISO 4217 codes.
     * @throws IOException If network error occurs.
     * @throws XmlPullParserException If XML parsing fails.
     */
    public List<String> getAvailableIsoCodes(String dateStr) throws IOException, XmlPullParserException {
        Map<String, Double> rates = fetchAndParseRates(dateStr);
        return new ArrayList<>(rates.keySet());
    }

    /**
     * Fetches the exchange rate for the specified ISO 4217 code (e.g., "USD") for the given date.
     * The rate is relative to MDL (Moldovan Leu), calculated as Value / Nominal from the source.
     * If dateStr is null, fetches for the current date.
     *
     * @param isoCode ISO 4217 code like "USD".
     * @param dateStr Date in "dd.mm.yyyy" format, or null for current.
     * @return The exchange rate.
     * @throws IOException If network error occurs.
     * @throws XmlPullParserException If XML parsing fails.
     * @throws IllegalArgumentException If the code is not found.
     */
    public double getExchangeRate(String isoCode, String dateStr) throws IOException, XmlPullParserException, IllegalArgumentException {
        Map<String, Double> rates = fetchAndParseRates(dateStr);
        String upperIso = isoCode.toUpperCase();
        if (!rates.containsKey(upperIso)) {
            throw new IllegalArgumentException("Currency code " + isoCode + " not found for the date.");
        }
        return rates.get(upperIso);
    }

    /**
     * Fetches all exchange rates as a map of ISO 4217 code to rate for the given date.
     * If dateStr is null, fetches for the current date.
     *
     * @param dateStr Date in "dd.mm.yyyy" format, or null for current.
     * @return Map of ISO code to exchange rate.
     * @throws IOException If network error occurs.
     * @throws XmlPullParserException If XML parsing fails.
     */
    public Map<String, Double> getAllExchangeRates(String dateStr) throws IOException, XmlPullParserException {
        return fetchAndParseRates(dateStr);
    }

    private Map<String, Double> fetchAndParseRates(String dateStr) throws IOException, XmlPullParserException {
        String xml = fetchXml(dateStr);
        return parseXml(xml);
    }

    private String fetchXml(String dateStr) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL).newBuilder();
        urlBuilder.addQueryParameter("get_xml", "1");
        if (dateStr != null) {
            urlBuilder.addQueryParameter("date", dateStr);
        }
        Request request = new Request.Builder().url(urlBuilder.build()).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    private Map<String, Double> parseXml(String xml) throws XmlPullParserException, IOException {
        Map<String, Double> rates = new HashMap<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(xml));

        String charCode = null;
        String nominal = null;
        String value = null;
        String currentTag = null;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                currentTag = parser.getName();
            } else if (eventType == XmlPullParser.TEXT) {
                if ("CharCode".equals(currentTag)) {
                    charCode = parser.getText().trim();
                } else if ("Nominal".equals(currentTag)) {
                    nominal = parser.getText().trim();
                } else if ("Value".equals(currentTag)) {
                    value = parser.getText().trim();
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if ("Valute".equals(parser.getName()) && charCode != null && nominal != null && value != null) {
                    double rate = Double.parseDouble(value.replace(",", ".")) / Integer.parseInt(nominal);
                    rates.put(charCode.toUpperCase(), rate);
                    charCode = null;
                    nominal = null;
                    value = null;
                }
                currentTag = null;
            }
            eventType = parser.next();
        }
        return rates;
    }
}
