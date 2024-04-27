package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class JsonInput {
    private static final String TAG_TICKETS = "tickets";
    private static final String TAG_ORIGIN = "origin";
    private static final String TAG_ORIGIN_NAME = "origin_name";
    private static final String TAG_DESTINATION = "destination";
    private static final String TAG_DESTINATION_NAME = "destination_name";
    private static final String TAG_DEPARTURE_DATE = "departure_date";
    private static final String TAG_DEPARTURE_TIME = "departure_time";
    private static final String TAG_ARRIVAL_DATE = "arrival_date";
    private static final String TAG_ARRIVAL_TIME = "arrival_time";
    private static final String TAG_CARRIER = "carrier";
    private static final String TAG_STOPS = "stops";
    private static final String TAG_PRICE = "price";
    private static Map<String, Long> minFlightTimesMap = new HashMap<>();

    public static void parsing() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("tickets.json"));
            JSONArray jsonArray = (JSONArray) jsonObject.get(TAG_TICKETS);
            List<Ticket> ticketList = new ArrayList<>();
            parse(jsonArray, ticketList);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parse(JSONArray jsonArray, List<Ticket> ticketList) {
        parsingJsonArrayToList(jsonArray, ticketList);
        for (Ticket ticket : ticketList) {
            getMinTimeCarrier(ticket);
        }
        printMinTimeEveryoneCarrier();
        getDifferenceBetweenPriceAndMedian(ticketList);
    }

    private static void parsingJsonArrayToList(JSONArray array, List<Ticket> ticketList) {
        for (Object object : array) {
            JSONObject ticket = (JSONObject) object;
            String origin = (String) ticket.get(TAG_ORIGIN);
            String originName = (String) ticket.get(TAG_ORIGIN_NAME);
            String destination = (String) ticket.get(TAG_DESTINATION);
            String destinationName = (String) ticket.get(TAG_DESTINATION_NAME);
            String departureDate = (String) ticket.get(TAG_DEPARTURE_DATE);
            String departureTime = (String) ticket.get(TAG_DEPARTURE_TIME);
            String arrivalDate = (String) ticket.get(TAG_ARRIVAL_DATE);
            String arrivalTime = (String) ticket.get(TAG_ARRIVAL_TIME);
            String carrier = (String) ticket.get(TAG_CARRIER);
            long stops = (Long) ticket.get(TAG_STOPS);
            long price = (Long) ticket.get(TAG_PRICE);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            departureTime = getCorrectTime(departureTime);
            arrivalTime = getCorrectTime(arrivalTime);

            LocalTime departureTime2 = LocalTime.parse(departureTime, timeFormatter);
            LocalTime arrivalTime2 = LocalTime.parse(arrivalTime, timeFormatter);
            LocalDate departureDate2 = LocalDate.parse(departureDate, formatter);
            LocalDate arrivalDate2 = LocalDate.parse(arrivalDate, formatter);

            if (originName.equals("Владивосток") && destinationName.equals("Тель-Авив")) {
                ticketList.add(new Ticket(origin,
                        originName,
                        destination,
                        destinationName,
                        departureDate2,
                        departureTime2,
                        arrivalDate2,
                        arrivalTime2,
                        carrier,
                        (int) stops,
                        (int) price));
                minFlightTimesMap.put(carrier, Long.MAX_VALUE);
            }
        }
    }

    private static String getCorrectTime(String time) {
        if (time.length() < 5) {
            time = "0" + time;
        }
        return time;
    }

    private static void getMinTimeCarrier(Ticket ticket) {
        long days = DateComparator.compareDates(ticket.getDepartureDate(), ticket.getArrivalDate());
        long result = differenceBetween(ticket.getDepartureTime(), ticket.getArrivalTime(), days);

        String carrier = ticket.getCarrier();
        if (minFlightTimesMap.containsKey(carrier)) {
            long currentMinFlightTime = minFlightTimesMap.get(carrier);
            minFlightTimesMap.put(carrier, getMinFlightTime(currentMinFlightTime, result));
        } else {
            minFlightTimesMap.put(carrier, result);
        }
    }

    public static long differenceBetween(LocalTime time1, LocalTime time2, long days) {
        if (days > 0) {
            long seconds = 86400 * days;
            return seconds + time1.until(time2, ChronoUnit.SECONDS);
        }
        return time1.until(time2, ChronoUnit.SECONDS);
    }

    private static long getMinFlightTime(long minSeconds, long result) {
        if (minSeconds == 0) {
            minSeconds = result;
        } else {
            if (minSeconds > result) {
                minSeconds = result;
            }
        }
        return minSeconds;
    }

    private static void printMinTimeEveryoneCarrier() {
        for (Map.Entry<String, Long> map : minFlightTimesMap.entrySet()) {
            displaysMinTime(map.getValue(), map.getKey());
        }
    }

    private static void displaysMinTime(long seconds, String carrier) {
        System.out.println("Минимальное время для " + carrier + ": " + convertSecondsToHoursMinutes(seconds));
    }

    private static String convertSecondsToHoursMinutes(long totalSeconds) {
        int days = (int) (totalSeconds / (60 * 60 * 24));
        int remainingSeconds = (int) (totalSeconds % (60 * 60 * 24));

        int hours = remainingSeconds / 3600;
        remainingSeconds %= 3600;

        int minutes = remainingSeconds / 60;
        remainingSeconds %= 60;
        return String.format("%d дней %02d:%02d:%02d", days, hours, minutes, remainingSeconds);
    }

    private static void getDifferenceBetweenPriceAndMedian(List<Ticket> ticketList) {
        System.out.println("Разница между средней ценой и медианой: " + calculateMedian(ticketList));
    }

    private static double calculateMedian(List<Ticket> tickets) {
        double sum = 0;
        for (Ticket ticket : tickets) {
            sum += ticket.getPrice();
        }
        double mean = sum / tickets.size();

        double[] prices = tickets.stream()
                .mapToDouble(Ticket::getPrice)
                .sorted()
                .toArray();

        double median;
        if (prices.length % 2 == 0) {
            median = (prices[prices.length / 2 - 1] + prices[prices.length / 2]) / 2;
        } else {
            median = prices[prices.length / 2];
        }

        return mean - median;
    }
}
