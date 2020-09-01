package ru.ifmo.rain.klepov.i18n;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Comparator;
import java.util.Locale;

public class NumberContainer extends StatisticContainer {
    private NumberFormat isNumber;

    public NumberContainer(Locale out, NumberFormat f) {
        super(out);
        isNumber = f;
        cmp = Comparator.comparingDouble(this::parse);
    }

    protected double parse(String s) {
        ParsePosition pos = new ParsePosition(0);
        return isNumber.parse(s, pos).doubleValue();
    }

    @Override
    protected Object[] getArgsMinMax(String value) {
        try {
            Object[] messageArguments;
            Number ans = isNumber.parse(value);
            messageArguments = new Object[]{isNumber.format(ans).length(), ans};
            return messageArguments;
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    protected String add(String sum, String s) {
        return Double.toString(Double.parseDouble(sum) + parse(s));
    }
}
