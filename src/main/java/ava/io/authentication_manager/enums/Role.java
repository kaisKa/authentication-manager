package ava.io.authentication_manager.enums;


import lombok.Getter;

@Getter
public enum Role {
    PATIENT("patient"),
    DOCTOR("doctor"),
    NURSE("nurse"),
    PARAMEDIC("paramedic"),
    CLINIC("clinic"),
    PROVIDER("provider"),
    SUPER_ADMIN("super_admin");


    private final String value;

    Role(String value) {

        this.value = value;
    }


}
