package ava.io.authentication_manager.db.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends BaseEntity{
    private String name;
    private String issuerUrl;
    private String jwksUrl;
    private String resource;
    private String resourceSecret;


}
