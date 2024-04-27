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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static long minSecondsS7 = 0;
    private static long minSecondsSU = 0;
    private static long minSecondsTK = 0;
    private static long minSecondsBA = 0;


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
            }
        }
    }

    private static String getCorrectTime(String arrivalTime) {
        if (arrivalTime.length() < 5) {
            arrivalTime = "0" + arrivalTime;
        }
        return arrivalTime;
    }

    private static void getMinTimeCarrier(Ticket ticket) {
        switch (ticket.getCarrier()) {
            case "S7": {
                long result = differenceBetween(ticket.getDepartureTime(), ticket.getArrivalTime());
                minSecondsS7 = getMinFlightTime(minSecondsS7, result);
                break;
            }
            case "SU": {
                long result = differenceBetween(ticket.getDepartureTime(), ticket.getArrivalTime());
                minSecondsSU = getMinFlightTime(minSecondsSU, result);
                break;
            }
            case "TK": {
                long result = differenceBetween(ticket.getDepartureTime(), ticket.getArrivalTime());
                minSecondsTK = getMinFlightTime(minSecondsTK, result);
                break;
            }
            case "BA": {
                long result = differenceBetween(ticket.getDepartureTime(), ticket.getArrivalTime());
                minSecondsBA = getMinFlightTime(minSecondsBA, result);
                break;
            }
            default:
                System.out.println("Непредвиденная ошибка");

        }
    }

    public static long differenceBetween(LocalTime time1, LocalTime time2) {
        return time1.until(time2, ChronoUnit.SECONDS);
    }

    private static long getMinFlightTime(long minSecondsBA, long result) {
        if (minSecondsBA == 0) {
            minSecondsBA = result;
        } else {
            if (minSecondsBA > result) {
                minSecondsBA = result;
            }
        }
        return minSecondsBA;
    }

    private static void printMinTimeEveryoneCarrier() {
        getMinFlightTime(minSecondsS7, "S7");
        getMinFlightTime(minSecondsSU, "SU");
        getMinFlightTime(minSecondsTK, "TK");
        getMinFlightTime(minSecondsBA, "BA");
    }

    private static void getMinFlightTime(long seconds, String carrier) {
        System.out.println("Минимальное время для " + carrier + ": " + convertSecondsToHoursMinutes(seconds));
    }

    private static String convertSecondsToHoursMinutes(long totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static void getDifferenceBetweenPriceAndMedian(List<Ticket> ticketList) {
        System.out.println("Разница между средней ценой и медианой: " + calculateMedian(ticketList));
    }

    private static double calculateMedian(List<Ticket> tickets) {
        double sum = 0;
        for(Ticket ticket : tickets) {
            sum += ticket.getPrice();
        }
        double mean = sum / tickets.size();

        double[] prices = tickets.stream()
                .mapToDouble(Ticket::getPrice)
                .sorted()
                .toArray();

        double median;
        if(prices.length % 2 == 0) {
            median = (prices[prices.length/2 - 1] + prices[prices.length/2]) / 2;
        } else {
            median = prices[prices.length / 2];
        }

        return mean - median;
    }
}
