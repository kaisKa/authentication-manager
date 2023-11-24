package ava.io.authentication_manager.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public enum Tenant {


    PATIENT("patient", "patient"),
    CLINIC("super_admin", "clinic"),
    LAB("lab","lab"),
    PROVIDER("provider","provider");


    private final String type;
    private final String tenant;

    Tenant(String type, String tenant) {

        this.type = type;
        this.tenant = tenant;
    }
}
