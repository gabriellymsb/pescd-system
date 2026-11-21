package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mud3 — parecer enviado ao aluno quando o relatório de estágio é reprovado. */
@Getter
@Setter
@NoArgsConstructor
public class RejectReportFormDTO {

    @NotBlank(message = "O parecer é obrigatório.")
    private String parecer;
}
