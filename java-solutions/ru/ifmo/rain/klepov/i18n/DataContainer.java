package ru.ifmo.rain.klepov.i18n;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataContainer extends StatisticContainer {
    private final List<DateFormat> dateFormat;
    Locale localOut;

    public DataContainer(Locale locale, Locale out) {
        super(out);
        dateFormat = IntStream.range(0, 4)
                .mapToObj(i -> DateFormat.getDateInstance(i, locale))
                .collect(Collectors.toList());
        sumLength = "0";
        localOut = out;
        cmp = Comparator.comparing(this::parse);
    }

    private Date parse(String s) {
        try {
            return dateFormat.stream().filter(i -> {
                try {
                    i.parse(s);
                    return true;
                } catch (ParseException e) {
                    return false;
                }
            }).findFirst().get().parse(s);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    protected Object[] getArgsAvg(String sumLength, int elements) {
        return new Object[]{new Date(Long.parseLong(sumLength) * 1000 / elements)};
    }

    @Override
    protected Object[] getArgsMinMax(String value) {
        Date ans = parse(value);
        return new Object[]{DateFormat.getDateInstance(DateFormat.DEFAULT, localOut).format(ans).length(), ans};
    }

    @Override
    protected String add(String sum, String s) {
        return Long.toString(Long.parseLong(sum) + parse(s).getTime()/1000);
    }
}
