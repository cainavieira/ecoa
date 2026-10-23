package ecoa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Um link de referência: o som, um vídeo de exemplo, as regras. Endereço http ou https para
 * qualquer lugar, e a descrição é opcional (RN-23).
 */
public record LinkReferenciaDto(
    @NotNull(message = "LINK_INVALIDO")
        @Pattern(regexp = "(?i)https?://[^\\s/?#]+\\S*", message = "LINK_INVALIDO")
        String url,
    String descricao) {}
