package br.com.danielsilva.ms_email.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cliente implements Serializable {

    private Long id;
    @NotBlank
    private String nome;
    @Email
    private String email;
    @NotNull
    private BigDecimal peso;
    @NotNull
    private BigDecimal altura;
    @NotNull
    private BigDecimal imc;

    private String subject = "IMC";
}
