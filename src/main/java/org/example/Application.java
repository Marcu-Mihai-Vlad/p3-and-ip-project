package org.example;

public class Application {
    InputDevice input;
    OutputDevice output;

    public Application(OutputDevice output, InputDevice input) {
        this.output = output;
        this.input = input;
    }

    /**
     * Helper function that checks if a name is too long or empty. Throws custom exceptions if it fails the checks.
     * @param name String it checks
     */
    public void nameCheck(String name) throws IllegalNameException {
        if (name.length() > 24) {
            throw new IllegalNameException("Name is too long! Must be less than 25 characters.\n");
        }
        else if (name.isBlank()) {
            throw new IllegalNameException("Name must not be empty!\n");
        }
    }

    /**
     * Helper function that checks if a password is too long, too short or too weak. Throws custom exceptions if it
     * fails the checks.
     * @param password String it checks
     */
    public void passwordCheck(String password) throws IllegalPasswordException {
        if (password.length() < 8) {
            throw new IllegalPasswordException("Password is too short! Must be at least 8 characters.\n");
        }
        else if (password.length() > 32) {
            throw new IllegalPasswordException("Password is too long! Must be less than 32 characters.\n");
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigits = false;
        String message = "";

        for (int i = 0; i < password.length(); i++) {
            if (Character.isUpperCase(password.charAt(i))) {
                hasUppercase = true;
            }
            else if (Character.isLowerCase(password.charAt(i))) {
                hasLowercase = true;
            }
            else if (Character.isDigit(password.charAt(i))) {
                hasDigits = true;
            }
        }

        if (hasUppercase && hasLowercase && hasDigits) {
            return;
        }

        message += "Password is too weak! Needs ";

        if(!hasUppercase) {
            message += "at least one more uppercase character";
        }
        if(!hasLowercase) {
            if(!hasUppercase) message += " and ";
            message += "at least one more lowercase character";
        }
        if(!hasDigits) {
            if(!hasLowercase || !hasUppercase ) message += " and ";
            message += "at least one more digit";
        }

        message += ".";
        throw new IllegalPasswordException(message);
    }
}
