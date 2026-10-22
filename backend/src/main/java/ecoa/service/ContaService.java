package ecoa.service;

import ecoa.dto.EuDto;
import ecoa.entity.Conta;
import ecoa.entity.Criador;
import ecoa.mapper.EuMapper;
import ecoa.regras.Funcao;
import ecoa.regras.RegrasDaConta;
import ecoa.repository.ContaRepository;
import ecoa.repository.CriadorRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.authentication.BadCredentialsException;
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

  /** A própria conta, com o bloco do criador quando ela é de criador (RF-05, RF-13, RF-17). */
  @Transactional(readOnly = true)
  public EuDto eu(int idConta) {
    Conta conta = contaDoToken(idConta);
    return criadores
        .findById(idConta)
        .map(criador -> EuMapper.deCriador(conta, criador))
        .orElseGet(() -> EuMapper.deConta(conta));
  }

  /**
   * O criador informa ou corrige o @ da conta pendente ou recusada, e ela fica pendente (RN-11,
   * RN-14, RN-15). O @ de outra conta é aceito: quem prova a posse é o código na bio (UC-02 1c).
   */
  @Transactional
  public EuDto informarUsuarioTiktok(int idConta, String usuarioTiktok) {
    Conta conta = contaDoToken(idConta);
    Criador criador = criadores.findById(idConta).orElseThrow(ContaService::contaApagada);
    criador.corrigirUsuarioTiktok(
        RegrasDaConta.normalizarUsuarioTiktok(usuarioTiktok),
        RegrasDaConta.statusDepoisDaCorrecaoPeloCriador(criador.status()));
    return EuMapper.deCriador(conta, criador);
  }

  private Conta contaDoToken(int idConta) {
    return contas.findById(idConta).orElseThrow(ContaService::contaApagada);
  }

  /**
   * A conta foi apagada, e o token de acesso dela ainda vale por até 15 minutos (ADR-0008): a
   * resposta pede para entrar de novo, e não diz que algo não existe.
   */
  private static BadCredentialsException contaApagada() {
    return new BadCredentialsException("A conta do token não existe mais.");
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
