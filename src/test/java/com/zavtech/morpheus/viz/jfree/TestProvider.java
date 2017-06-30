package com.zavtech.morpheus.viz.jfree;

import java.io.IOException;
import java.time.LocalDate;

import com.zavtech.morpheus.frame.DataFrame;


public class TestProvider {

    /**
     * Returns a DataFrame for the ticker specified
     * @param ticker        the ticker reference
     * @return              the DataFrame result
     * @throws java.io.IOException  if there is an IO exception
     */
    public static DataFrame<LocalDate,String> getQuotes(String ticker) throws IOException {
        return DataFrame.read().csv(options -> {
            options.setResource("/" + ticker + ".csv");
            options.setRowKeyParser(LocalDate.class, v -> LocalDate.parse(v[0]));
        });
    }

}
