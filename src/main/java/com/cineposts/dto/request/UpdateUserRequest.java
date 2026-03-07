package com.cineposts.dto.request;

import com.cineposts.model.enums.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String password;

    private Role role;

    private Boolean active;
}
