package org.example;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateComparator {

    public static long compareDates(LocalDate departureDate, LocalDate arrivalDate) {
        return ChronoUnit.DAYS.between(departureDate, arrivalDate);
    }
}
