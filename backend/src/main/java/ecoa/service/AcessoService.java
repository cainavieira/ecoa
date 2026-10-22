package ecoa.service;

import ecoa.dto.AcessoDto;
import ecoa.entity.Conta;
import ecoa.mapper.AcessoMapper;
import ecoa.regras.Funcao;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaConta;
import ecoa.repository.ContaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quem é operador e quem é da gravadora (UC-18), e o primeiro operador (UC-20). */
@Service
public class AcessoService {

  private final ContaRepository contas;
  private final Clock relogio;

  public AcessoService(ContaRepository contas, Clock relogio) {
    this.contas = contas;
    this.relogio = relogio;
  }

  @Transactional(readOnly = true)
  public List<AcessoDto> listar() {
    return contas.findByFuncaoInOrderByIdConta(List.of(Funcao.OPERADOR, Funcao.GRAVADORA)).stream()
        .map(AcessoMapper::paraDto)
        .toList();
  }

  /**
   * O e-mail que já tem conta, de qualquer função, é recusado (RN-04). O nome fica nulo até o
   * primeiro acesso (RF-03).
   */
  @Transactional
  public AcessoDto cadastrar(String email, Funcao funcao) {
    String normalizado = RegrasDaConta.normalizarEmail(email);
    if (contas.existsByEmail(normalizado)) {
      throw new RegraVioladaException(
          Recusa.EMAIL_JA_CADASTRADO, "Esse e-mail já está cadastrado.");
    }
    return AcessoMapper.paraDto(contas.save(new Conta(normalizado, funcao, Instant.now(relogio))));
  }

  /**
   * Remover o acesso apaga a conta (RN-05). As linhas de operador ficam bloqueadas da contagem até
   * a remoção: dois operadores que removem um ao outro ao mesmo tempo não deixam o sistema sem
   * nenhum (RN-06, UC-18 4d).
   */
  @Transactional
  public void remover(int idQuemRemove, int idRemovida) {
    long operadores = contas.travarPorFuncao(Funcao.OPERADOR).size();
    Conta removida =
        contas
            .findById(idRemovida)
            .filter(conta -> conta.funcao() != Funcao.CRIADOR)
            .orElseThrow(
                () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Acesso não encontrado."));
    RegrasDaConta.validarRemocaoDeAcesso(idQuemRemove, idRemovida, removida.funcao(), operadores);
    contas.delete(removida);
  }
}
