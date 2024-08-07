package ru.practicum.ewm.mainservice.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @Size(min = 2, max = 250)
    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Size(min = 6, max = 254)
    private String email;
}
