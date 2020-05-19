package edu.ucsb.cs48.s20.demo.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidEmailService {
    public static boolean validEmail(String email) {
        if (email == null) {
            return false;
        }
        String regex = "^(.+)@(.+)$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}