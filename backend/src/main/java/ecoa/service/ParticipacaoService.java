package ecoa.service;

import ecoa.entity.Campanha;
import ecoa.entity.Criador;
import ecoa.entity.Participacao;
import ecoa.mapper.ParticipacaoMapper;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaCampanha;
import ecoa.repository.CampanhaRepository;
import ecoa.repository.CriadorRepository;
import ecoa.repository.ParticipacaoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipacaoService {

  private final CampanhaRepository campanhas;
  private final CriadorRepository criadores;
  private final ParticipacaoRepository participacoes;
  private final Clock relogio;

  public ParticipacaoService(
      CampanhaRepository campanhas,
      CriadorRepository criadores,
      ParticipacaoRepository participacoes,
      Clock relogio) {
    this.campanhas = campanhas;
    this.criadores = criadores;
    this.participacoes = participacoes;
    this.relogio = relogio;
  }

  /**
   * O criador aprovado entra na campanha agendada ou aberta, com a data do aceite (RN-16, RN-31,
   * RN-32). Quem já participa recebe a participação que existe, sem recusa, inclusive no duplo
   * clique, em que a segunda entrada esbarra na chave única (endpoints.md).
   */
  @Transactional
  public Entrada entrar(int idCampanha, int idConta) {
    Campanha campanha =
        campanhas
            .findById(idCampanha)
            .orElseThrow(
                () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
    Optional<Participacao> existente =
        participacoes.findByCampanhaIdCampanhaAndCriadorIdCriador(idCampanha, idConta);
    if (existente.isPresent()) {
      return entrada(existente.get(), idCampanha, false);
    }
    // A conta apagada tem o token valendo por até 15 minutos (ADR-0008): pede para entrar de novo.
    Criador criador =
        criadores
            .findById(idConta)
            .orElseThrow(() -> new BadCredentialsException("A conta do token não existe mais."));
    RegrasDaCampanha.validarEntrada(criador.status(), campanha.status(LocalDate.now(relogio)));
    boolean nova = participacoes.entrar(idCampanha, idConta, Instant.now(relogio)) == 1;
    return entrada(
        participacoes
            .findByCampanhaIdCampanhaAndCriadorIdCriador(idCampanha, idConta)
            .orElseThrow(),
        idCampanha,
        nova);
  }

  private Entrada entrada(Participacao participacao, int idCampanha, boolean nova) {
    LocalDate aceiteEm = LocalDate.ofInstant(participacao.aceiteEm(), relogio.getZone());
    return new Entrada(ParticipacaoMapper.paraDto(participacao, idCampanha, aceiteEm), nova);
  }
}
