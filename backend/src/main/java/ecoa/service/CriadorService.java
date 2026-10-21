package ecoa.service;

import ecoa.regras.StatusCriador;
import ecoa.repository.CriadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriadorService {

  private final CriadorRepository criadores;

  public CriadorService(CriadorRepository criadores) {
    this.criadores = criadores;
  }

  /** Lido do banco a cada requisição: o status não vai no token, porque muda (ADR-0008). */
  @Transactional(readOnly = true)
  public boolean estaBloqueado(int idConta) {
    return criadores.existsByIdCriadorAndStatus(idConta, StatusCriador.BLOQUEADA);
  }
}
