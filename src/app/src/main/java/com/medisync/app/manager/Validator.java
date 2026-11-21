package com.medisync.app.manager;

import com.medisync.app.util.DateUtils;

import java.util.regex.Pattern;

/** Input validation shared by the screens. Each method returns null when the value is valid, or a message to show. */
public final class Validator {
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$");
    public static final int MAX_QUANTITY = 100000;

    private Validator() { }

    public static String emailError(String email) {
        if (email == null || email.trim().isEmpty()) return "Enter your email address.";
        String e = email.trim();
        if (e.length() > 120 || !EMAIL.matcher(e).matches()) return "Enter a valid email address, such as name@example.com.";
        return null;
    }

    public static String nameError(String name) {
        if (name == null || name.trim().isEmpty()) return "Enter a name.";
        if (name.trim().length() > 60) return "Name must be 60 characters or fewer.";
        return null;
    }

    /** 8+ characters, upper, lower, digit, no spaces. */
    public static String passwordError(String p) {
        if (p == null || p.isEmpty()) return "Enter a password.";
        boolean up = false, lo = false, di = false, space = false;
        for (char ch : p.toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') up = true;
            else if (ch >= 'a' && ch <= 'z') lo = true;
            else if (ch >= '0' && ch <= '9') di = true;
            if (Character.isWhitespace(ch)) space = true;
        }
        if (space) return "Password must not contain spaces.";
        if (p.length() < 8) return "Password must be at least 8 characters.";
        if (!up) return "Password needs at least one uppercase letter (A-Z).";
        if (!lo) return "Password needs at least one lowercase letter (a-z).";
        if (!di) return "Password needs at least one number (0-9).";
        return null;
    }

    public static String dosageError(String dosage) {
        if (dosage == null || dosage.trim().isEmpty()) return "Enter the dosage, such as 500mg.";
        if (dosage.trim().length() > 40) return "Dosage must be 40 characters or fewer.";
        return null;
    }

    public static String quantityError(String quantity) {
        try {
            parseQuantity(quantity);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    /** Parses a whole-number quantity in 0..MAX_QUANTITY. Throws IllegalArgumentException with a user message. */
    public static int parseQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) throw new IllegalArgumentException("Enter a quantity.");
        int value;
        try {
            value = Integer.parseInt(quantity.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a whole number for the quantity.");
        }
        if (value < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        if (value > MAX_QUANTITY) throw new IllegalArgumentException("Quantity is too large.");
        return value;
    }

    /** Date of birth must be a real date that is not in the future. */
    public static String dateOfBirthError(String dob) {
        Long millis = DateUtils.parseToMillis(dob);
        if (millis == null) return "Choose a valid date of birth.";
        if (millis > System.currentTimeMillis()) return "Date of birth cannot be in the future.";
        return null;
    }

    public static String timeError(String hhmm) {
        return DateUtils.minutesOf24h(hhmm) < 0 ? "Choose a valid time." : null;
    }

    public static String dateError(String date) {
        return DateUtils.parseToMillis(date) == null ? "Choose a valid date." : null;
    }
}