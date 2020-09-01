package ru.ifmo.rain.klepov;

import java.util.ListResourceBundle;

public class Bundle_en extends ListResourceBundle {

    private static final Object[][] CONTENTS = {
            {"usage", "Enter coordinates in following order: white rook x, white rook y, white king x, white king y, black king x, black king y. 1-indexation."},
            {"null_args", "Args shouldn't be null"},
            {"int_args", "Args should be integers"},
            {"read_coords", "Please provide new coordinates for your king, separated by whitespace. \n First vertical, then horizontal. 1-indexation."},
            {"invalid_turn", "Invalid turn, repeat your input."},
            {"invalid_coords", "Please check your coordinates - no less than 1, no more than {0} }."},
            {"print", "Y - your black king\nR - opponent white rook\nK - opponent white king"},
            {"gg", "Good game!"}
    };

    protected Object[][] getContents() {
        return CONTENTS;
    }
}