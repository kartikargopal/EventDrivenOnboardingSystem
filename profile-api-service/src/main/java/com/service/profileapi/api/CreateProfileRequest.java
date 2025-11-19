package com.service.profileapi.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor    // needed for JSON deserialization
@AllArgsConstructor   // needed for your tests & service layer
public class CreateProfileRequest {

    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
