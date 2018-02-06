package me.theeninja.pfflowing.flowingregions;

import java.util.Arrays;
import java.util.List;

public class Author {
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private static final List<String> AUTHOR_LABELS = Arrays.asList("Author", "Authors", "Author(s)");

    public Author(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
    }

    public Author(String lastName) {
        this.firstName = null;
        this.lastName = lastName;
        this.fullName = getLastName();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
