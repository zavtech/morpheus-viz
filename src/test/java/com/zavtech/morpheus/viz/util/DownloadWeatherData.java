package com.zavtech.morpheus.viz.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zavtech.morpheus.util.TextStreamReader;
import com.zavtech.morpheus.util.http.HttpClient;

public class DownloadWeatherData {

    private static final String DOUBLE_REGEX = "([-+]?[0-9]*\\.?[0-9]+).*";
    private static final Matcher decimalMatcher = Pattern.compile(DOUBLE_REGEX).matcher("");


    public static void main(String[] args) {
        final String[] urls = new String[] {
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/aberporthdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/ballypatrickdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/bradforddata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/cambridgedata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/cardiffdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/chivenordata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/durhamdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/dunstaffnagedata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/cwmystwythdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/eastbournedata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/eskdalemuirdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/braemardata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/hurndata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/leucharsdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/heathrowdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/lowestoftdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/armaghdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/manstondata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/oxforddata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/ringwaydata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/sheffielddata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/nairndata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/cambornedata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/stornowaydata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/valleydata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/whitbydata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/newtonriggdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/waddingtondata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/wickairportdata.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/stationdata/yeoviltondata.txt"
        };

        Arrays.asList(urls).forEach(url -> {
            System.out.println("Downloading " + url);
            HttpClient.getDefault().doGet(httpRequest -> {
                httpRequest.setUrl(url);
                httpRequest.setResponseHandler((status, stream) -> {
                    try {
                        BufferedWriter writer = null;
                        TextStreamReader reader = null;
                        try {
                            reader = new TextStreamReader(stream);
                            if (reader.hasNext()) {
                                boolean header = false;
                                final String location = reader.nextLine().trim().split("\\s+")[0];
                                final File file = new File("morpheus-viz/src/test/resources/weather", location + ".csv");
                                writer = new BufferedWriter(new FileWriter(file));
                                while (reader.hasNext()) {
                                    final String line = reader.nextLine();
                                    if (!header && line.trim().startsWith("yyyy")) {
                                        header = true;
                                        final List<String> tokens = Arrays.asList(line.trim().split("\\s+"));
                                        final String output = String.join(",", tokens);
                                        writer.write(output);
                                        writer.write("\n");
                                        reader.hasNext();
                                        reader.nextLine();
                                    } else if (header) {
                                        if (line.trim().startsWith("2015")) {
                                            break;
                                        } else {
                                            final String[] tokens = line.trim().split("\\s+");
                                            final String output = String.join(",", process(tokens));
                                            writer.write(output);
                                            writer.write("\n");
                                        }
                                    }
                                }
                            }
                        } finally {
                            try {
                                if (writer != null) writer.close();
                                if (reader != null) reader.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        return Optional.empty();
                    } catch (IOException ex) {
                        throw new RuntimeException("Failed to load data from URL: " + url, ex);
                    }
                });
            });
        });
    }

    /**
     * Returns the process list of tokens for the line specified
     * @param tokens    the tokens
     * @return          the processed list of tokens
     */
    private static List<String> process(String[] tokens) {
        final List<String> result = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            token = token != null ? token.trim() : null;
            if (token == null || token.equals("---")) {
                result.add("NaN");
            } else if (decimalMatcher.reset(token).matches()) {
                final String value = decimalMatcher.group(1);
                result.add(value);
            }
        }
        return result.size() > 7 ? result.subList(0, 7) : result;
    }
}
