package ru.ifmo.rain.klepov;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class GameConfiguration {
    Locale outLocal = Locale.US;
    ResourceBundle bundle = ResourceBundle.getBundle("ru.ifmo.rain.klepov.Bundle_en", outLocal);

    private static class Pos {
        int x, y;
        Pos (int posx, int posy) {
            x = posx;
            y = posy;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    Pos ladPos, qPos, oppQPos;
    int moveNumber;

    GameConfiguration(Pos lPos, Pos q, Pos oQPos, ResourceBundle b) {
        ladPos = lPos;
        qPos = q;
        oppQPos = oQPos;
        moveNumber = 0;
        bundle = b;
    }

    GameConfiguration(int lposx, int lposy, int qx, int qy, int oppqx, int oppqy, ResourceBundle b) {
        this(new Pos(lposx, lposy), new Pos(qx, qy), new Pos(oppqx, oppqy), b);
    }

    void addMoveNumber() {
        moveNumber++;
    }

    void moveOppUnchecked(Pos pos) {
        addMoveNumber();
        oppQPos = pos;
    }

    void moveUncheckedQ(int x, int y) {
        addMoveNumber();
        qPos.x = x;
        qPos.y = y;
    }

    void moveUncheckedL(int x, int y) {
        addMoveNumber();
        ladPos.x = x;
        ladPos.y = y;
    }

    void move() {
        if (oppQPos.y == 0) {
            if (ladPos.y != 1) { //we should block
                if (Math.abs(oppQPos.x - ladPos.x) > 1) {// we can block
                    moveUncheckedL(ladPos.x, 1);
                } else {//choose direction
                    if (oppQPos.x > 4) {
                        moveUncheckedL(7, ladPos.y);
                    } else {
                        moveUncheckedL(0, ladPos.y);
                    }
                }
            } else {//move q
                if (qPos.y > 2) {//go down
                    moveUncheckedQ(qPos.x, qPos.y - 1);
                } else {//do to opp q
                    if (qPos.x != oppQPos.x) {
                        int dx = (oppQPos.x > qPos.x ? -1 : 1);
                        moveUncheckedQ(qPos.x + dx, qPos.y);
                    } else {//checkmate
                        moveUncheckedL(qPos.x, 0);
                    }
                }
            }
        } else {
            moveUncheckedL(ladPos.x, Math.min(oppQPos.y + 1, 7));
        }
    }

    void oppMove(String s) {

    }

    private String getQConf() {
        //return "My q position: " + qPos.toString();
        return readFromBundle("myq_cfg", qPos.toString());
    }

    private String getLConf() {
        //return "My l position: " + ladPos.toString();
        return readFromBundle("myl_cfg", ladPos.toString());
    }

    private String getOppQConf() {
        //return "Your q position: " + oppQPos.toString();
        return readFromBundle("opp_cfg", oppQPos.toString());
    }

    @Override
    public String toString() {
        //return "Config: \n" + getOppQConf() + "\n" + getQConf() + "\n" + getLConf() + "\n";
        return readFromBundle("all_cfg", getOppQConf(), getQConf(), getLConf());
    }

    private String readFromBundle(final String request, final Object ... v) {
        return new MessageFormat(bundle.getString(request), outLocal).format(v);
    }
}
