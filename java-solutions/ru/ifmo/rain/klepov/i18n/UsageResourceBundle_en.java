package ru.ifmo.rain.klepov.i18n;

import java.util.ListResourceBundle;

public class UsageResourceBundle_en extends ListResourceBundle {

    private static final Object[][] CONTENTS = {
            {"date_avg", "Average date is {0, date}"},
            {"currency_avg", "Average currency is {0, number, currency}"},
            {"words_avg", "Average word length is {0}"},
            {"number_avg", "Average number is {0}"},
            {"line_avg", "Average line length is {0}"},
            {"sentence_avg", "Average sentence length is {0}"},

            {"date_max", "Maximal date has length = {0}, and equals {1, date}"},
            {"currency_max", "Maximal currency has length = {0}, and equals {1, number, currency}"},
            {"words_max", "Maximal word has length = {0}, and equals {1}"},
            {"number_max", "Maximal number has length = {0}, and equals {1}"},
            {"line_max", "Maximal line has length = {0}, and equals {1}"},
            {"sentence_max", "Maximal sentence has length = {0}, and equals {1}"},

            {"date_min", "Minimal date has length = {0}, and equals {1, date}"},
            {"currency_min", "Minimal currency has length = {0}, and equals {1, number, currency}"},
            {"words_min", "Minimal word has length = {0}, and equals {1}"},
            {"number_min", "Minimal number has length = {0}, and equals {1}"},
            {"line_min", "Minimal line has length = {0}, and equals {1}"},
            {"sentence_min", "Minimal sentence has length = {0}, and equals {1}"},

            {"date_min_undef", "Min date undefined"},
            {"currency_min_undef", "Min currency undefined"},
            {"words_min_undef", "Min word length undefined"},
            {"number_min_undef", "Min number undefined"},
            {"line_min_undef", "Min line length undefined"},
            {"sentence_min_undef", "Min sentence length undefined"},

            {"date_max_undef", "Max date undefined"},
            {"currency_max_undef", "Max currency undefined"},
            {"words_max_undef", "Max word length undefined"},
            {"number_max_undef", "Max number undefined"},
            {"line_max_undef", "Max line length undefined"},
            {"sentence_max_undef", "Max sentence length undefined"},

            {"date_avg_undef", "Average date undefined"},
            {"currency_avg_undef", "Average currency undefined"},
            {"words_avg_undef", "Average word length undefined"},
            {"number_avg_undef", "Average number undefined"},
            {"line_avg_undef", "Average line length undefined"},
            {"sentence_avg_undef", "Average sentence length undefined"},

            {"date_number", "Number of dates: {0} ({1} unique)"},
            {"currency_number", "Number of currencies: {0} ({1} unique)"},
            {"words_number", "Number of words: {0} ({1} unique)"},
            {"number_number", "Count of numbers: {0} ({1} unique)"},
            {"line_number", "Number of lines: {0} ({1} unique)"},
            {"sentence_number", "Number of sentences: {0} ({1} unique)"},

            {"sentence", "Sentence stat:"},
            {"date", "Date stat:"},
            {"currency", "Currency stat:"},
            {"words", "Words stat:"},
            {"number", "Numbers stat:"},
            {"line", "Lines stat:"},

            {"common_date", "Number of dates: {0}"},
            {"common_currency", "Number of currency: {0}"},
            {"common_number", "Count of numbers: {0}"},
            {"common_words", "Number of words: {0}"},
            {"common_line", "Number of lines: {0}"},
            {"common_sentence", "Number of sentences: {0}"},
            {"common", "Common statistic:"},

            {"header", "The file being analyzed: {0}"},
            {"fileHeader", "Text statistic"}
    };

    protected Object[][] getContents() {
        return CONTENTS;
    }
}