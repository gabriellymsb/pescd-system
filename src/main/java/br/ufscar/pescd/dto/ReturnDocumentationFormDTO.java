package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mud4 — parecer enviado ao aluno quando a documentação é devolvida. */
@Getter
@Setter
@NoArgsConstructor
public class ReturnDocumentationFormDTO {

    @NotBlank(message = "O parecer é obrigatório.")
    private String parecer;
}
