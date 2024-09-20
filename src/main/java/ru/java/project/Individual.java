package ru.java.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Individual {
    private String firstName;
    private String lastName;
    private Boolean hasChildren;
    private Integer age;

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
