package ava.io.authentication_manager.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {
    public String key;
    public String operation;
    public Object value;
}