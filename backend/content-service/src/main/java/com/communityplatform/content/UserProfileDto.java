package com.communityplatform.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;

    public String toFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (firstName != null) {
            return firstName;
        }
        if (lastName != null) {
            return lastName;
        }
        return null;
    }
}
