package ava.io.authentication_manager.dtos;

import ava.io.authentication_manager.enums.Gender;
import ava.io.authentication_manager.utils.ErrorCode;
import ava.io.authentication_manager.utils.ValidationMSG;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class ProviderDTO {

    private String userName;


//    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$", message = ValidationMSG.EMAIL_PATTERN)
    private String emailId;


    private String password;
    private String firstname;
    private String lastName;
    private String careGiver;
    private String speciality;
    private Gender gender;


    @NotBlank(message = ValidationMSG.EMTY_PHONE)//"Invalid Phone: must not be empty")
    @NotNull(message = ValidationMSG.NULL_PHONE)
    @Size(min = 9, max = 10, message = ValidationMSG.PHONE_SIZE)
    @Pattern(regexp = "^0?\\d{1,9}$", message = ValidationMSG.PHONE_PATTERN)
    private String phone;


    @NotNull(message = ValidationMSG.NULL_COUNTRY_CODE)
    @NotBlank(message = ValidationMSG.EMTY_COUNTRY_CODE)
    @Pattern(regexp = "^\\+\\d{3}$", message = ValidationMSG.COUNTRY_CODE_PATTERN)
    private String countryCode;

//    @NotBlank(message = ValidationMSG.EMTY_LICENSE_NUM)
//    @NotNull(message = ValidationMSG.NULL_LICENSE_NUM)
//    private String licenseNumber;

    //    private Date createdDate;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
//    private Date licenseIssueDate;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
//    private Date licenseExpireDate;

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