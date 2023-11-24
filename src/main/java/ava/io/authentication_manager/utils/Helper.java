package ava.io.authentication_manager.utils;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class Helper {

    // ******************* constant ************* //
    public static final String GSM = "gsm";
    public static final String ACCOUNT_ID = "account_id";
    public static final String VERIFICATION_CODE = "verification_code";
    public static final String IS_VERIFIED = "is_verified";
    public static final String TENANT_ID = "tenant_id";

    public static final String ADMIN_REALM = "admin";

    public static final String PROVIDER = "provider";

    public static final int VERIFICATION_CODE_LENGTH = 4;
    public static final String EMAIL = "email";


    public static String generateCode() {
        String numbers = "0123456789";
        Random rndm_method = new Random();
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            stb.append(numbers.charAt(rndm_method.nextInt(numbers.length())));
        }
        return stb.toString();
    }

    public static String generateSecret(){
        int length = 32; // Length of the secret in bytes

        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[length];
        secureRandom.nextBytes(secretBytes);

        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        System.out.println("Generated Secret: " + secret);
        return secret;
    }

    public static String removeLeadingZeros(String number) {
        var res = Integer.parseInt(number);
        return String.valueOf(res);
    }

    public static String getGSM(String countryCode, String phone) {
        return countryCode + removeLeadingZeros(phone);
    }


    public static void main(String[] args) {
        String k = "00432400";
        var t = Integer.parseInt(k);
        System.out.println(
                t

        );
    }

    public static String generatePassayPass() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ErrorCode.FAILED.getMessage();
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        return gen.generatePassword(10, splCharRule, lowerCaseRule, upperCaseRule, digitRule);
    }
}
