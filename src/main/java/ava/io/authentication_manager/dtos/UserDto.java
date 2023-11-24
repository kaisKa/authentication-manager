package ava.io.authentication_manager.dtos;

import ava.io.authentication_manager.enums.Gender;
import ava.io.authentication_manager.utils.ValidationMSG;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(value = {"keycloakId, isVerified"}, allowGetters = true)
public class UserDto {

    private String userName;
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|in)$" ,message = ValidationMSG.NOT_VALID_MAIL)
    private String emailId;
    private String password;
    @JsonProperty("firstname")
    private String firstName;
    private String lastName;
    private Gender gender;
    @JsonIgnore
    private String country;

    @NotBlank(message = ValidationMSG.EMTY_PHONE)
    @NotNull(message = ValidationMSG.NULL_PHONE)
    @Size(min = 9, max = 10, message = ValidationMSG.PHONE_SIZE)
    @Pattern(regexp = "^0?\\d{1,9}$", message = ValidationMSG.PHONE_PATTERN)
    private String phone;


    @NotNull(message = ValidationMSG.NULL_COUNTRY_CODE)
    @NotBlank(message = ValidationMSG.EMTY_COUNTRY_CODE)
    @Pattern(regexp = "^\\+\\d{1,3}$", message = ValidationMSG.COUNTRY_CODE_PATTERN)
    private String countryCode;

    private String userId;

    private List<String> roles;

    private String reference;

    @JsonIgnore
    private String callBackUrl;

    @JsonIgnore
    private JsonNode callBackBody;

    private String accountId;

    private Boolean isVerified = false;

    private Boolean emailVerified = false;


    @JsonIgnore()
    public void setUserName(String userName) {
        this.userName = userName;
    }





}