package ecoa.service;

import ecoa.entity.Conta;
import ecoa.entity.TokenRenovacao;
import ecoa.repository.TokenRenovacaoRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Só a api emite token: os modos do .jar sobem sem o ECOA_JWT_CHAVE (T-08). */
@Service
@ConditionalOnWebApplication
public class SessaoService {

  public static final Duration VALIDADE_DO_ACESSO = Duration.ofMinutes(15);
  public static final Duration VALIDADE_DA_RENOVACAO = Duration.ofDays(30);

  private static final SecureRandom SORTEIO = new SecureRandom();

  private final TokenRenovacaoRepository renovacoes;
  private final JwtEncoder emissor;
  private final Clock relogio;

  public SessaoService(TokenRenovacaoRepository renovacoes, JwtEncoder emissor, Clock relogio) {
    this.renovacoes = renovacoes;
    this.emissor = emissor;
    this.relogio = relogio;
  }

  /** Os dois tokens de uma sessão nova, no fim do login (ADR-0008). */
  @Transactional
  public Sessao abrir(Conta conta) {
    Instant agora = Instant.now(relogio);
    byte[] sorteado = new byte[32];
    SORTEIO.nextBytes(sorteado);
    String renovacao = Base64.getUrlEncoder().withoutPadding().encodeToString(sorteado);
    renovacoes.save(
        new TokenRenovacao(conta, hash(renovacao), agora, agora.plus(VALIDADE_DA_RENOVACAO)));
    return new Sessao(acesso(conta, agora), renovacao);
  }

  /**
   * Troca o token de renovação por um de acesso novo e empurra a validade da renovação, sem trocar
   * o token (RNF-23, ADR-0008).
   */
  @Transactional
  public Sessao renovar(String tokenDeRenovacao) {
    Instant agora = Instant.now(relogio);
    TokenRenovacao renovacao =
        renovacoes
            .findByHash(hash(tokenDeRenovacao))
            .filter(token -> token.expiraEm().isAfter(agora))
            .orElseThrow(
                () -> new BadCredentialsException("Token de renovação inválido ou vencido."));
    renovacao.prorrogarAte(agora.plus(VALIDADE_DA_RENOVACAO));
    return new Sessao(acesso(renovacao.conta(), agora), tokenDeRenovacao);
  }

  /**
   * Apaga o token de renovação. O de acesso que já existia vale até vencer, em no máximo 15 minutos
   * (RF-04, ADR-0008).
   */
  @Transactional
  public void sair(String tokenDeRenovacao) {
    renovacoes.findByHash(hash(tokenDeRenovacao)).ifPresent(renovacoes::delete);
  }

  private String acesso(Conta conta, Instant agora) {
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(String.valueOf(conta.idConta()))
            .claim("funcao", conta.funcao().name())
            .issuedAt(agora)
            .expiresAt(agora.plus(VALIDADE_DO_ACESSO))
            .build();
    return emissor
        .encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
        .getTokenValue();
  }

  /** O banco guarda só o hash: quem lê o banco não consegue usar o token (ADR-0008). */
  private static String hash(String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
