package me.theeninja.pfflowing.gui.cardparser;

import edu.stanford.nlp.ling.CoreLabel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Testers {
    public static boolean person(CoreLabel coreLabel) {
        return coreLabel.ner().equals("PERSON");
    }

    public static boolean organization(CoreLabel coreLabel) {
        return true;
    }

    private static final String SHORT_DATE_MATCH = "^([1-2][0-9])?[0-9][0-9]$";

    public static boolean shortDate(CoreLabel coreLabel) {
        Pattern pattern = Pattern.compile(SHORT_DATE_MATCH);
        Matcher matcher = pattern.matcher(coreLabel.word());
        return matcher.find();
    }

    private static final String URL_MATCH = "/((([A-Za-z]{3,9}:(?://)?)(?:[-;:&=+$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)/";

    public static boolean url(CoreLabel coreLabel) {
        Pattern pattern = Pattern.compile(URL_MATCH);
        Matcher matcher = pattern.matcher(coreLabel.word());
        return matcher.find();
    }
}
