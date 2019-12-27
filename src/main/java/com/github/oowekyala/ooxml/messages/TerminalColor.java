package com.github.oowekyala.ooxml.messages;

/**
 * ANSI escape sequences for colors, to style text in terminal
 * environments.
 */
public enum TerminalColor {
    COL_BLACK(30),
    COL_RED(31),
    COL_GREEN(32),
    COL_YELLOW(33),
    COL_BLUE(34),
    COL_MAGENTA(35),
    COL_CYAN(36),
    COL_WHITE(37),
    ;

    /** Reset all attributes. */
    public static final String ANSI_RESET = "\\e[0m";
    private static final String ESCAPE = "\\e[";

    private final int fgCode;


    TerminalColor(int fgCode) {
        this.fgCode = fgCode;
    }

    private String getEscape(boolean bright, boolean background, boolean bold) {
        String escape = ESCAPE + getCode(bright, background);
        return bold ? escape + ";1m" : escape + "m";
    }

    private int getCode(boolean bright, boolean background) {
        int code = fgCode;
        if (bright) {
            code += 60;
        }
        if (background) {
            code += 10;
        }
        return code;
    }

    /**
     * Style the given string with this color.
     * All text attributes are {@linkplain #ANSI_RESET reset} at the end.
     *
     * @param text       Text to color
     * @param bright     Whether to use a bright version of this color
     * @param background Whether to set the color on the text background rather than on the color
     * @param bold       Whether to set the text as bold
     */
    public String apply(String text, boolean bright, boolean background, boolean bold) {
        return getEscape(bright, background, bold) + text + ANSI_RESET;
    }

    /**
     * Style both foreground and background.
     * Returns a new string containing the parameter, which uses this
     * ANSI code at the beginning, and {@linkplain #ANSI_RESET resets}
     * all text attributes at the end.
     *
     * @param text       Text to color
     * @param foreground Text color
     * @param background Background color
     * @param brightFg   Whether text color is bright
     * @param brightBg   Whether background color is bright
     * @param boldText   Whether to set the text as bold
     */
    public static String style(String text,
                               TerminalColor foreground,
                               TerminalColor background,
                               boolean brightFg,
                               boolean brightBg,
                               boolean boldText) {

        String noBold = ESCAPE
            + foreground.getCode(brightFg, false)
            + ";"
            + background.getCode(brightBg, true);

        String prefix = boldText ? noBold + ";1m" : noBold + "m";

        return prefix + text + ANSI_RESET;
    }
}
