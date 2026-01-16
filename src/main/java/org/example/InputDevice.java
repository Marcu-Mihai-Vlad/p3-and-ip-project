package org.example;

import java.io.InputStream;
import java.util.Scanner;

public class InputDevice {
    InputStream is;

    public InputDevice(InputStream is) {
        this.is = is;
    }

    public String readLine() {
        Scanner input = new Scanner(is);
        return input.nextLine();
    }

    public int readInt() {
        Scanner input = new Scanner(is);


        while(true) {
            String inputLine = input.nextLine();

            try {
                return Integer.parseInt(inputLine);
            }
            catch (NumberFormatException e) {
                if(is != System.in) {
                    throw e;
                }
                System.out.println("Not an integer, please try again: ");
            }
        }

    }

    public double readDouble() {
        Scanner input = new Scanner(is);


        while(true) {
            String inputLine = input.nextLine();

            try {
                return Double.parseDouble(inputLine);
            }
            catch (NumberFormatException e) {
                if(is != System.in) {
                    throw e;
                }
                System.out.println("Not a number, please try again: ");
            }
        }

    }
}
