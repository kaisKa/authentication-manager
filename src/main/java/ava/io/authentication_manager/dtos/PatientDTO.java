package ava.io.authentication_manager.dtos;

import ava.io.authentication_manager.enums.Gender;
import ava.io.authentication_manager.utils.ValidationMSG;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO {
//    @JsonIgnoreProperties(value={ "userName" }, allowGetters=true)

    private String userName;


//    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = ValidationMSG.EMAIL_PATTERN)
    private String emailId;



    private String password;
    private String firstname;
    private String lastName;

    private Gender gender;

    @NotBlank(message = ValidationMSG.EMTY_PHONE)
    @NotNull(message = ValidationMSG.NULL_PHONE)
    @Size(min = 9, max = 10, message = ValidationMSG.PHONE_SIZE)
    @Pattern(regexp = "^0?\\d{1,9}$", message = ValidationMSG.PHONE_PATTERN)
    private String phone;


    @NotNull(message = ValidationMSG.NULL_COUNTRY_CODE)
    @NotBlank(message = ValidationMSG.EMTY_COUNTRY_CODE)
    @Pattern(regexp = "^\\+\\d{3}$", message = ValidationMSG.COUNTRY_CODE_PATTERN)
    private String countryCode;

    private String callBackUrl;

    private JsonNode callBackBody;

    public String getUserName() {
        return userName;
    }

    @JsonIgnore()
    public void setUserName(String userName) {
        this.userName = userName;
    }
}