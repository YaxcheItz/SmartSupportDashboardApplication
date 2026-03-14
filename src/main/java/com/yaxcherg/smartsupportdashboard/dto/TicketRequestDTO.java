package com.yaxcherg.smartsupportdashboard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRequestDTO {

    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 10, message = "La descripción debe tener al menos 10 caracteres")
    private String description;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ser un formato de correo electrónico válido")
    private String customerEmail;
}