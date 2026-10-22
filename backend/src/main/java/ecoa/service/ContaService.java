package ecoa.service;

import ecoa.entity.Conta;
import ecoa.entity.Criador;
import ecoa.regras.Funcao;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaConta;
import ecoa.repository.ContaRepository;
import ecoa.repository.CriadorRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

  /** Sem 0, O, 1 e I, que se confundem quando o operador lê o código na bio. */
  private static final String ALFABETO_DO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private static final SecureRandom SORTEIO = new SecureRandom();

  private final ContaRepository contas;
  private final CriadorRepository criadores;
  private final Clock relogio;

  public ContaService(ContaRepository contas, CriadorRepository criadores, Clock relogio) {
    this.contas = contas;
    this.criadores = criadores;
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

  /**
   * No fim do login: acha a conta pelo e-mail, ou cria a de criador, pendente, com o primeiro
   * acesso como data do aceite dos termos (RN-03, RN-09, RF-02). O nome do Google completa a conta
   * cadastrada pelo operador (RF-03).
   */
  @Transactional
  public Conta entrar(String email, String nome) {
    String normalizado = RegrasDaConta.normalizarEmail(email);
    Conta conta = contas.findByEmail(normalizado).orElseGet(() -> criarCriador(normalizado));
    conta.completarNome(nome);
    return conta;
  }

  private Conta criarCriador(String email) {
    Instant agora = Instant.now(relogio);
    Conta conta = contas.save(new Conta(email, Funcao.CRIADOR, agora));
    criadores.save(new Criador(conta, codigoLivre(), agora));
    return conta;
  }

  /** Único entre os criadores (RN-12). A repetição é rara, e o banco também a recusa. */
  private String codigoLivre() {
    String codigo;
    do {
      codigo = codigoDeVerificacao();
    } while (criadores.existsByCodigoVerificacao(codigo));
    return codigo;
  }

  /** ECOA- e seis caracteres sorteados, como ECOA-7K3QMX. */
  private static String codigoDeVerificacao() {
    StringBuilder codigo = new StringBuilder("ECOA-");
    for (int i = 0; i < 6; i++) {
      codigo.append(ALFABETO_DO_CODIGO.charAt(SORTEIO.nextInt(ALFABETO_DO_CODIGO.length())));
    }
    return codigo.toString();
  }
}
