package ru.ifmo.rain.klepov.i18n;

import java.text.Collator;
import java.util.Locale;

public class LineContainer extends StatisticContainer {
    private Collator collator;

    public LineContainer(Locale out, Locale locale) {
        super(out);
        collator = Collator.getInstance(locale);
        cmp = (l, r) -> {
            if (l.length() == r.length())
                return collator.compare(l, r);
            return l.length() - r.length();
        };
    }
}
