package pl.javastart.task;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final List<DateTimeFormatter> DATE_TIME_PATTERNS = List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm:ss"));
    private static final List<DateTimeFormatter> DATE_PATTERNS = List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    private static final DateTimeFormatter DISPLAYED_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final char PLUS = '+';
    private static final char MINUS = '-';
    private Map<String, ZoneId> zones;

    public Main() {
        this.zones = createZonesMap();
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.run(new Scanner(System.in));
    }

    public void run(Scanner scanner) {
        LocalDateTime dateTime = loadTimeInput(scanner);
        if (dateTime == null) {
            System.out.println("Niepoprawna data");
        } else {
            showTime(dateTime);
        }
    }

    private LocalDateTime loadTimeInput(Scanner scanner) {
        System.out.println("Podaj datę/różnicę czasu:");
        String input = scanner.nextLine();
        if (input.startsWith("t")) {
            return calculateDateFromNow(input.substring(1));
        } else {
            return adjustDate(input);
        }
    }

    private LocalDateTime calculateDateFromNow(String timeShift) {
        LocalDateTime now = LocalDateTime.now();
        int indexOfPlus = timeShift.indexOf(PLUS);
        int indexOfMinus = timeShift.indexOf(MINUS);
        while (indexOfPlus != -1 || indexOfMinus != -1) {
            int number = 0;
            char operation = PLUS;
            int timeUnitIndex = 0;
            if (indexOfMinus == -1 || indexOfPlus != -1 && indexOfPlus < indexOfMinus) {
                number = getTimeNumberFromString(timeShift.substring(indexOfPlus+1));
                operation = PLUS;
                timeUnitIndex = indexOfPlus + Integer.toString(number).length() + 1;
            } else if (indexOfPlus == -1 || indexOfMinus != -1 && indexOfMinus < indexOfPlus) {
                number = getTimeNumberFromString(timeShift.substring(indexOfMinus+1));
                operation = MINUS;
                timeUnitIndex = indexOfMinus + Integer.toString(number).length() + 1;
            }
            now = calculateLocalDateTime(operation, number, timeShift.charAt(timeUnitIndex), now);
            timeShift = timeShift.substring(timeUnitIndex + 1);
            indexOfPlus = timeShift.indexOf(PLUS);
            indexOfMinus = timeShift.indexOf(MINUS);
        }
        return now;
    }

    private int getTimeNumberFromString(String timeShift) {
        StringBuilder builder = new StringBuilder();
        char[] timeShiftAsArray = timeShift.toCharArray();
        for (int i = 0; i < timeShiftAsArray.length; i++) {
            if (Character.isDigit(timeShiftAsArray[i])) {
                builder.append(timeShiftAsArray[i]);
            } else {
                break;
            }
        }
        return Integer.parseInt(builder.toString());
    }

    private LocalDateTime calculateLocalDateTime(char operation, int timeAmount, char timeUnit, LocalDateTime now) {
        TemporalUnit unit = getTemporalUnit(timeUnit);
        if (unit == null) {
            return null;
        }
        if (operation == PLUS) {
            return now.plus(timeAmount, unit);
        } else {
            return now.minus(timeAmount, unit);
        }
    }

    private TemporalUnit getTemporalUnit(char timeUnit) {
        return switch (timeUnit) {
            case 's' -> ChronoUnit.SECONDS;
            case 'm' -> ChronoUnit.MINUTES;
            case 'h' -> ChronoUnit.HOURS;
            case 'd' -> ChronoUnit.DAYS;
            case 'M' -> ChronoUnit.MONTHS;
            case 'y' -> ChronoUnit.YEARS;
            default -> null;
        };
    }

    private void showTime(LocalDateTime dateTime) {
        ZonedDateTime local = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
        for (String zone : zones.keySet()) {
            String zonedDate = getZonedFormattedDate(local, zones.get(zone));
            System.out.printf("%s: %s%n", zone, zonedDate);
        }
    }

    private LocalDateTime adjustDate(String date) {
        DateTimeFormatter pattern = matchPattern(date);
        if (pattern == null) {
            return null;
        } else if (DATE_PATTERNS.contains(pattern)) {
            DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
            builder.append(pattern).append(DateTimeFormatter.ofPattern(" HH:mm:ss"));
            return LocalDateTime.parse(date + " 00:00:00", builder.toFormatter());
        }
        return LocalDateTime.parse(date, pattern);
    }

    private String getZonedFormattedDate(ZonedDateTime date, ZoneId zone) {
        return date.withZoneSameInstant(zone).format(DISPLAYED_DATE_PATTERN);
    }

    private DateTimeFormatter matchPattern(String date) {
        DateTimeFormatter pattern = getPattern(date, DATE_TIME_PATTERNS);
        if (pattern == null) {
            pattern = getPattern(date, DATE_PATTERNS);
        }
        return pattern;
    }

    private DateTimeFormatter getPattern(String date, List<DateTimeFormatter> patterns) {
        for (DateTimeFormatter pattern : patterns) {
            if (isDateValid(date, pattern)) {
                return pattern;
            }
        }
        return null;
    }

    private boolean isDateValid(String date, DateTimeFormatter pattern) {
        try {
            pattern.parse(date);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private Map<String, ZoneId> createZonesMap() {
        Map<String, ZoneId> zones = new LinkedHashMap<>();
        zones.put("Czas lokalny", ZoneId.systemDefault());
        zones.put("UTC", ZoneId.of("UTC"));
        zones.put("Londyn", ZoneId.of("Europe/London"));
        zones.put("Los Angeles", ZoneId.of("America/Los_Angeles"));
        zones.put("Sydney", ZoneId.of("Australia/Sydney"));
        return zones;
    }
}
