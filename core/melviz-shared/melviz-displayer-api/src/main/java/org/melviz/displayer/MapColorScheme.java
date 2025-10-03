package org.melviz.displayer;

import java.util.stream.Stream;

public enum MapColorScheme {

    RED,
    GREEN,
    BLUE;

    public static MapColorScheme from(String value) {
        return Stream.of(values())
                     .filter(v -> v.toString().equalsIgnoreCase(value))
                     .findFirst().orElseGet(() -> GREEN);
    }

}
