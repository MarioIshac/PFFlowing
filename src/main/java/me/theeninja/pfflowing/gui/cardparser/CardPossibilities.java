package me.theeninja.pfflowing.gui.cardparser;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public final class CardPossibilities {
    private final Set<String> authors;
    private final Set<String> sources;
    private final Set<String> dates;

    public CardPossibilities(Set<String> authors, Set<String> sources, Set<String> dates) {
        this.authors = authors;
        this.sources = sources;
        this.dates = dates;
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public Set<String> getSources() {
        return sources;
    }

    public Set<String> getDates() {
        return dates;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CardPossibilities").append("[").append("\n");
        authors.forEach(author -> stringBuilder.append("Author").append(author).append("\n"));
        sources.forEach(source -> stringBuilder.append("Source").append(source).append("\n"));
        dates.forEach(date -> {
            stringBuilder.append(date).append("\n");
        });
        stringBuilder.append("\n]");
        return stringBuilder.toString();
    }
}
