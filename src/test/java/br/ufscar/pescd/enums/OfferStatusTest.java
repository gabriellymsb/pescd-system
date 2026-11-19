package br.ufscar.pescd.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferStatusTest {

    // PCE: uma verificacao por valor do enum
    @Test
    @DisplayName("Cada status de oferta tem o nome de exibicao esperado")
    void cadaStatus_temNomeDeExibicao() {

        assertEquals("Em andamento", OfferStatus.EM_ANDAMENTO.getDisplayName());
        assertEquals("Aguardando encerramento do secretário",
                OfferStatus.AGUARDANDO_ENCERRAMENTO_SECRETARIO.getDisplayName());
        assertEquals("Concluída", OfferStatus.CONCLUIDA.getDisplayName());
        assertEquals("Em atraso", OfferStatus.EM_ATRASO.getDisplayName());
    }

    @Test
    @DisplayName("Só ofertas em andamento ou em atraso aceitam pedido de encerramento")
    void apenasStatusAbertos_aceitamPedidoDeEncerramento() {
        assertTrue(OfferStatus.EM_ANDAMENTO.acceptsClosureRequest());
        assertTrue(OfferStatus.EM_ATRASO.acceptsClosureRequest());
        assertFalse(OfferStatus.AGUARDANDO_ENCERRAMENTO_SECRETARIO.acceptsClosureRequest());
        assertFalse(OfferStatus.CONCLUIDA.acceptsClosureRequest());
    }
}
