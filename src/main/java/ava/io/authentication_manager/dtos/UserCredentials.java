package ava.io.authentication_manager.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCredentials {
    private String username;
    private String password;
}
