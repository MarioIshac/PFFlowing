package me.theeninja.pfflowing.card;

import me.theeninja.pfflowing.flowingregions.Author;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

// Class has to be used because java does not have generic support for enums
public class CardComponent<ComponentType> {

    public static final CardComponent<Author> AUTHOR = new CardComponent<>(Arrays.asList("Author", "Authors", "Author(s)"), Author::new);
    public static final CardComponent<String> SOURCE = new CardComponent<>(Arrays.asList("Source", "URL", "Website", "Web"), String::new);
    public static final CardComponent<Calendar> DATE = new CardComponent<>(Arrays.asList("Time", "Date", "Year"), string -> {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            calendar.setTime(simpleDateFormat.parse(string));
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    });
    public static final CardComponent<CardContent> CONTENT = new CardComponent<>(Arrays.asList("CardContent", "Body"), CardContent::new);
    private final List<String> acceptedLabels;
    private final Function<String, ComponentType> function;

    /**
     * @param acceptedLabels
     * @param function
     */
    private CardComponent(List<String> acceptedLabels, Function<String, ComponentType> function) {
        this.acceptedLabels = acceptedLabels;
        this.function = function;
    }

    public List<String> getAcceptedLabels() {
        return acceptedLabels;
    }

    public Function<String, ComponentType> getFunction() {
        return function;
    }
}
