package ecoa.service;

import ecoa.entity.Conta;
import ecoa.regras.Funcao;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaConta;
import ecoa.repository.ContaRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

  private final ContaRepository contas;
  private final Clock relogio;

  public ContaService(ContaRepository contas, Clock relogio) {
    this.contas = contas;
    this.relogio = relogio;
  }

  /**
   * O e-mail que já tem conta, de qualquer função, é recusado (RN-04). O nome fica nulo até o
   * primeiro acesso (RF-03).
   */
  @Transactional
  public void cadastrarOperador(String email) {
    String normalizado = RegrasDaConta.normalizarEmail(email);
    if (contas.existsByEmail(normalizado)) {
      throw new RegraVioladaException(
          Recusa.EMAIL_JA_CADASTRADO, "Esse e-mail já está cadastrado.");
    }
    contas.save(new Conta(normalizado, Funcao.OPERADOR, Instant.now(relogio)));
  }
}
