package xyz.genscode.type.utils;

import java.util.Random;

public class AvatarColors {
    public static final String RED = "#dc6969";
    public static final String YELLOW = "#dcc269";
    public static final String ORANGE = "#dca569";
    public static final String GREEN = "#6cdc69";
    public static final String LIGHT_BLUE = "#69d0dc";
    public static final String BLUE = "#6992dc";
    public static final String PINK = "#af69dc";

    public static String getRandomAvatarColor(){
        int rand = new Random().nextInt(7) + 1;
        switch (rand){
            case 2:
                return YELLOW;
            case 3:
                return ORANGE;
            case 4:
                return GREEN;
            case 5:
                return LIGHT_BLUE;
            case 6:
                return BLUE;
            case 7:
                return PINK;
            default:
                return RED;
        }
    }
}
