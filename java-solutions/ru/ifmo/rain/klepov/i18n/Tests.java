package ru.ifmo.rain.klepov.i18n;

import org.junit.*;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Description;
import org.junit.rules.TestWatcher;
import org.junit.rules.TestRule;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Tests {
    private static final long start = System.currentTimeMillis();
    private static final double EPS = 0.00001;

    @Rule
    public TestRule watcher = watcher(description -> System.err.println("=== Running " + description.getMethodName()));

    protected static TestWatcher watcher(final Consumer<Description> watcher) {
        return new TestWatcher() {
            @Override
            protected void starting(final Description description) {
                watcher.accept(description);
            }
        };
    }

    private String read(String name) {
        String text;
        try (InputStream in = new FileInputStream(name)) {
            text = new String(in.readAllBytes());
        } catch (IOException e) {
            System.err.println("I/O Exception with input file.");
            return "";
        } catch (InvalidPathException e) {
            System.err.println("Invalid path of input file.");
            return "";
        }
        return text;
    }

    private static Locale findLocale(final String name) {
        return Arrays.stream(Locale.getAvailableLocales()).filter(l -> l.toString().equals(name)).findFirst().orElse(null);
    }

    private void check(StatisticContainer stat, int el, int uniq, int max, int min) {
        check(stat, el, uniq);
        assertEquals(max, stat.max.length());
        assertEquals(min, stat.min.length());
    }

    private void check(StatisticContainer stat, int el, int uniq, String max, String min) {
        check(stat, el, uniq);
        assertEquals(max, stat.max);
        assertEquals(min, stat.min);
    }

    private void check(StatisticContainer stat, int el, int uniq) {
        assertEquals(el, stat.elements);
        assertEquals(uniq, stat.getDistinct());
    }

    private void check(StatisticContainer stat, int el, int uniq, int max, int min, int avg) {
        check(stat, el, uniq, max, min);
        assertEquals(avg, Integer.parseInt(stat.sumLength));
    }

    private void check(StatisticContainer stat, int el, int uniq, String max, String min, double avg) {
        check(stat, el, uniq, max, min);
        assertTrue(avg - Double.parseDouble(stat.sumLength) < EPS);
    }

    private TextStatistics getStat(String in) {
        return getStat(in, "en_US");
    }

    private TextStatistics getStat(String in, String name) {
        Locale locale = findLocale(name);
        return new TextStatistics(locale, read(in), locale);
    }

    /*@Test
    public void test00_test() {
        final TextStatistics stat = getStat("out.txt");
        StatisticContainer statisticContainer = stat.getStatWords();
        Date date = new Date(1055000000000L);
        System.out.println(DateFormat.getDateInstance(DateFormat.FULL, findLocale("ru_RU")).format(date));
        System.out.println(DateFormat.getDateInstance(DateFormat.LONG, findLocale("ru_RU")).format(date));
        System.out.println(DateFormat.getDateInstance(DateFormat.MEDIUM, findLocale("ru_RU")).format(date));
        System.out.println(DateFormat.getDateInstance(DateFormat.SHORT, findLocale("ru_RU")).format(date));
        System.out.println(NumberFormat.getCurrencyInstance(findLocale("ru_RU")).format(10));
    }*/

    @Test
    public void test01_sentences() {
        final TextStatistics stat = getStat("sent_en.txt");
        check(stat.getStatSentences(), 13, 11, 56, 10, 419);
        check(stat.getStatWords(), 79, 53, "mightier", "a", 326);
        check(stat.getLineStat(), 5, 5, 152, 0, 425);
    }

    @Test
    public void test02_words() {
        final TextStatistics stat = getStat("words_en.txt");
        check(stat.getStatWords(), 15, 12, "Zz_se_Ven_zZ", "el", 62);
    }

    @Test
    public void test03_lines() {
        TextStatistics stat = getStat("line_en.txt");
        check(stat.getLineStat(), 18, 9, 740, 0, 3401);
    }

    @Test
    public void test04_numbers() {
        TextStatistics stat = getStat("numbers_en.txt");
        check(stat.getStatNumbers(), 9, 5, "1,000,000", "1", 2031482.28);
    }

    @Test
    public void test05_currency() {
        TextStatistics stat = getStat("currency_en.txt");
        check(stat.getStatCurrency(), 8, 4, "$1,010.00", "$0.00", 2025.2);
    }

    @Test
    public void test05_dates() {
        TextStatistics stat = getStat("date_en.txt");
        check(stat.getStatDates(), 4, 4, "Wednesday, May 18, 2033", "May 18, 2031");
    }

    @Test
    public void test06_ru() {
        TextStatistics stat = getStat("ru.txt", "ru_RU");
        check(stat.getLineStat(), 12, 12, 273, 42, 1649);
        check(stat.getStatSentences(), 27, 27, 304, 3, 1636);
        check(stat.getStatWords(), 281, 175, "летоисчисление", "0", 1240);
        check(stat.getStatCurrency(), 4, 3, "10,10 ₽", "-0,10 ₽", 30);
        check(stat.getStatDates(), 4, 3, "12.04.2020", "суббота, 7 июня 2003 г.");
        check(stat.getStatNumbers(), 23, 16, "2 019", "-0,141", 8367);
    }

    public static void main(final String[] args) {
        final Result result = new JUnitCore().run(Tests.class);
        if (!result.wasSuccessful()) {
            for (final Failure failure : result.getFailures()) {
                System.err.printf("Test %s failed: %s%n", failure.getDescription().getMethodName(), failure.getMessage());
                if (failure.getException() != null) {
                    failure.getException().printStackTrace();
                }
            }
            System.exit(1);
        } else {
            final long time = System.currentTimeMillis() - start;
            System.out.printf("SUCCESS in %dms %n", time);
            System.exit(0);
        }
    }
}