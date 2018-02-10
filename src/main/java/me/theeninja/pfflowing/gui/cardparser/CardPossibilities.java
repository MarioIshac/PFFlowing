package me.theeninja.pfflowing.gui.cardparser;

import me.theeninja.pfflowing.flowingregions.Author;

import java.util.Calendar;
import java.util.List;

public final class CardPossibilities {
    private final List<Author> authors;
    private final List<String> sources;
    private final List<Calendar> dates;

    public CardPossibilities(List<Author> authors, List<String> sources, List<Calendar> dates) {
        this.authors = authors;
        this.sources = sources;
        this.dates = dates;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public List<String> getSources() {
        return sources;
    }

    public List<Calendar> getDates() {
        return dates;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CardPossibilities").append("[").append("\n");
        authors.forEach(author -> stringBuilder.append("Author").append(author.getFullName()).append("\n"));
        sources.forEach(source -> stringBuilder.append("Source").append(source).append("\n"));
        dates.forEach(date -> {
            stringBuilder.append(date.get(Calendar.YEAR)).append("\n");
        });
        stringBuilder.append("\n]");
        return stringBuilder.toString();
    }
}
