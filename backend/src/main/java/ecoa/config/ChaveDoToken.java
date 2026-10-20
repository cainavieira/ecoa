package ecoa.config;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * A chave do token de acesso, que chega em ECOA_JWT_CHAVE: a privada, em PKCS#8, codificada em
 * base64 numa linha só (ADR-0008).
 */
final class ChaveDoToken {

  private static final String INVALIDA =
      "ECOA_JWT_CHAVE não é uma chave RSA privada em PKCS#8, codificada em base64.";

  private ChaveDoToken() {}

  /** A metade pública, que confere a assinatura, calculada a partir da privada. */
  static RSAPublicKey publica(String chaveEmBase64) {
    try {
      KeyFactory fabrica = KeyFactory.getInstance("RSA");
      PrivateKey privada =
          fabrica.generatePrivate(
              new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(chaveEmBase64)));
      if (!(privada instanceof RSAPrivateCrtKey rsa)) {
        throw new IllegalStateException(INVALIDA);
      }
      return (RSAPublicKey)
          fabrica.generatePublic(new RSAPublicKeySpec(rsa.getModulus(), rsa.getPublicExponent()));
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new IllegalStateException(INVALIDA, e);
    }
  }
}
