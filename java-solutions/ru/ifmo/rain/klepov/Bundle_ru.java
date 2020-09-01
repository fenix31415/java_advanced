package ru.ifmo.rain.klepov;

import java.util.ListResourceBundle;

// :NOTE: * Необоснованное использование ListResourceBundle
public class Bundle_ru extends ListResourceBundle {

    private static final Object[][] CONTENTS = {
            {"usage", "Введите координаты: белая ладья x, белая ладья y, белый король x, белый король y, черный король x, черный король y. 1-индексация."},
            {"null_args", "Аргументы null"},
            {"int_args", "Аргументы должны быть целыми числами"},
            {"read_coords", "Напишите координаты короля. \n Вертикально, горизонтально. 1-индексация."},
            {"invalid_turn", "Неверный ход, повторите."},
            {"invalid_coords", "Проверьте координаты. Размер доски -- {0} }."},
            {"print", "Y - выш король\nR - моя ладья\nK - мой король"},
            {"gg", "Все."}
    };

    protected Object[][] getContents() {
        return CONTENTS;
    }
}