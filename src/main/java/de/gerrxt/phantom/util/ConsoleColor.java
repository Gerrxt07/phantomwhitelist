package de.gerrxt.phantom.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling colored console output
 */
public class ConsoleColor {
    
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Background colors
    public static final String BLACK_BACKGROUND = "\u001B[40m";
    public static final String RED_BACKGROUND = "\u001B[41m";
    public static final String GREEN_BACKGROUND = "\u001B[42m";
    public static final String YELLOW_BACKGROUND = "\u001B[43m";
    public static final String BLUE_BACKGROUND = "\u001B[44m";
    public static final String PURPLE_BACKGROUND = "\u001B[45m";
    public static final String CYAN_BACKGROUND = "\u001B[46m";
    public static final String WHITE_BACKGROUND = "\u001B[47m";
    
    // Styles
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    
    private final Logger logger;
    
    /**
     * Constructor that takes a logger
     * 
     * @param logger The logger to use
     */
    public ConsoleColor(Logger logger) {
        this.logger = logger;
    }
    
    /**
     * Log an info message with success (green) color
     * 
     * @param message The message to log
     */
    public void success(String message) {
        logger.info(GREEN + message + RESET);
    }
    
    /**
     * Log an error message with red color
     * 
     * @param message The message to log
     */
    public void error(String message) {
        logger.severe(RED + message + RESET);
    }
    
    /**
     * Log a warning message with yellow color
     * 
     * @param message The message to log
     */
    public void warning(String message) {
        logger.warning(YELLOW + message + RESET);
    }
    
    /**
     * Log an info message with blue color
     * 
     * @param message The message to log
     */
    public void info(String message) {
        logger.info(BLUE + message + RESET);
    }
    
    /**
     * Log a debug message with cyan color
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        logger.info(CYAN + "[DEBUG] " + message + RESET);
    }
    
    /**
     * Log a message with a custom color
     * 
     * @param message The message to log
     * @param color The ANSI color code to use
     */
    public void log(String message, String color) {
        logger.info(color + message + RESET);
    }
    
    /**
     * Log a highlighted message with background color
     * 
     * @param message The message to log
     * @param background The ANSI background color code to use
     */
    public void highlight(String message, String background) {
        logger.info(background + BLACK + message + RESET);
    }
    
    /**
     * Log a section header with bold purple text
     * 
     * @param message The message to log
     */
    public void section(String message) {
        String line = PURPLE + BOLD + "=".repeat(message.length() + 4) + RESET;
        logger.info(line);
        logger.info(PURPLE + BOLD + "  " + message + "  " + RESET);
        logger.info(line);
    }
    
    /**
     * Log an exception with red color
     * 
     * @param message The message to log
     * @param e The exception to log
     */
    public void exception(String message, Exception e) {
        logger.log(Level.SEVERE, RED + message + RESET, e);
    }
}
