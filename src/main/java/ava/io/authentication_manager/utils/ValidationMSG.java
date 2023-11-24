package ava.io.authentication_manager.utils;


import lombok.Getter;


@Getter
public class ValidationMSG {

   // ***** EMPTY FIELD ***** //
    public static final String EMTY_PHONE = "Invalid Phone: must not be empty";
 public static final String EMTY_COUNTRY_CODE= "Invalid country Code: must not be empty";



    public static final String EMTY_LICENSE_NUM = "Invalid license number: must not be empty";


    // ***** NULL FIELD ***** //

    public static final String NULL_PHONE = "Invalid Phone: must not be NULL";
    public static final String NULL_COUNTRY_CODE = "Invalid country Code: must not be NULL";
    public static final String NULL_LICENSE_NUM = "Invalid license number: must not be NULL";



    // ***** FIELD PATTERN ***** //
    public static final String PHONE_PATTERN = "Invalid phone number: phone should consist of 9 - 10 digit";
    public static final String COUNTRY_CODE_PATTERN = "Invalid country code: should be three digit starting with '+' ";
    public static final String EMAIL_PATTERN = "Invalid Email: not a valid email format";



    public static final String PHONE_SIZE = "Invalid Phone: Must be of 9 - 10 digit; including 0";

    public static final String NOT_VALID_MAIL = "Not A valid mail";


    ;






    public static   String message = null;


    ValidationMSG( String message) {

        this.message = message;
    }






}
