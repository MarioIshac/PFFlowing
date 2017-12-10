package me.theeninja.pfflowing.card;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Author {
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private static final List<String> AUTHOR_LABELS = Arrays.asList("Author", "Authors", "Author(s)");

    Author(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
    }

    Author(String fullName) {
        String[] partsOfName = fullName.split(" ");
        this.firstName = partsOfName[0];
        this.lastName = partsOfName[1];
        this.fullName = fullName;
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
