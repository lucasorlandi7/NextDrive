package br.edu.unoesc.nextdrive.controller;

import br.edu.unoesc.nextdrive.dao.ClienteDAO;
import br.edu.unoesc.nextdrive.dao.LocacaoDAO;
import br.edu.unoesc.nextdrive.dao.PagamentoDAO;
import br.edu.unoesc.nextdrive.dao.VeiculoDAO;
import br.edu.unoesc.nextdrive.model.Cliente;
import br.edu.unoesc.nextdrive.model.Locacao;
import br.edu.unoesc.nextdrive.model.Pagamento;
import br.edu.unoesc.nextdrive.model.PagamentoCartao;
import br.edu.unoesc.nextdrive.model.PagamentoPix;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.model.StatusLocacao;
import br.edu.unoesc.nextdrive.model.Veiculo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de locações: alugar um veículo e devolver pagando. */
@RestController
public class LocacaoController {

    private LocacaoDAO dao = new LocacaoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private VeiculoDAO veiculoDAO = new VeiculoDAO();
    private PagamentoDAO pagamentoDAO = new PagamentoDAO();

    /**
     * Lista as locações, cada uma com a forma de pagamento (se já foi paga).
     *
     * @return lista em JSON com locacao e pagamento
     */
    @GetMapping("/locacoes")
    public ResponseEntity<Object> listar() {
        List<Map<String, Object>> lista = new ArrayList<Map<String, Object>>();
        for (Locacao l : dao.listar()) {
            String pagamento = "";
            for (Pagamento p : pagamentoDAO.listarPorLocacao(l.getId())) {
                pagamento = p.descrever();
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("locacao", l);
            item.put("pagamento", pagamento);
            lista.add(item);
        }
        return new ResponseEntity<Object>(lista, HttpStatus.OK);
    }

    /**
     * Aluga um veículo para um cliente. Confere as datas, a validade da CNH e se o veículo está disponível.
     *
     * @param dados clienteId, veiculoId, dataRetirada e dataDevolucao (aaaa-mm-dd)
     * @return mensagem de sucesso (com a estimativa) ou de erro
     */
    @PostMapping("/locacoes")
    public ResponseEntity<Object> alugar(@RequestBody Map<String, String> dados) {
        try {
            Cliente cliente = clienteDAO.buscar(Integer.parseInt(dados.get("clienteId")));
            Veiculo veiculo = veiculoDAO.buscar(Integer.parseInt(dados.get("veiculoId")));
            LocalDate retirada = LocalDate.parse(dados.get("dataRetirada"));
            LocalDate devolucao = LocalDate.parse(dados.get("dataDevolucao"));
            if (cliente == null) {
                return erro("Escolha o cliente.");
            }
            if (veiculo == null || !veiculo.estaDisponivel()) {
                return erro("O veículo não está disponível.");
            }
            if (retirada.isBefore(LocalDate.now())) {
                return erro("A data de retirada não pode ser no passado.");
            }
            if (!devolucao.isAfter(retirada)) {
                return erro("A devolução precisa ser depois da retirada.");
            }
            if (!cliente.cnhValidaEm(devolucao)) {
                return erro("A CNH do cliente vence antes da devolução.");
            }
            if (!dao.alugar(cliente, veiculo, retirada, devolucao)) {
                return erro("Não foi possível registrar o aluguel.");
            }
            Reserva estimativa = new Reserva(cliente, veiculo.getCategoria(), retirada, devolucao);
            return new ResponseEntity<Object>("Aluguel registrado. Estimativa: R$ " + estimativa.estimativa(),
                    HttpStatus.CREATED);
        } catch (NumberFormatException e) {
            return erro("Escolha o cliente.");
        } catch (DateTimeParseException e) {
            return erro("Informe as duas datas.");
        }
    }

    /**
     * Devolve o veículo e paga na hora: calcula o valor final (com multa de atraso),
     * encerra a locação e registra o pagamento do valor total, por cartão ou Pix.
     *
     * @param id    código da locação
     * @param dados quilometragem, dataDevolucao, forma (CARTAO ou PIX) e detalhe (bandeira ou txid)
     * @return mensagem de sucesso (com o valor final) ou de erro
     */
    @PutMapping("/locacoes/{id}/devolucao")
    public ResponseEntity<Object> devolverEPagar(@PathVariable("id") int id,
                                                 @RequestBody Map<String, String> dados) {
        try {
            Locacao locacao = dao.buscar(id);
            int km = Integer.parseInt(dados.get("quilometragem"));
            LocalDate dataDevolucao = LocalDate.parse(dados.get("dataDevolucao"));
            String forma = dados.get("forma");
            String detalhe = dados.get("detalhe") == null ? "" : dados.get("detalhe").trim();
            if (locacao == null || locacao.getStatus() != StatusLocacao.ABERTA) {
                return erro("Locação não encontrada ou já devolvida.");
            }
            if (km < locacao.getKmSaida()) {
                return erro("A quilometragem não pode ser menor que a da saída (" + locacao.getKmSaida() + ").");
            }
            if (dataDevolucao.isBefore(locacao.getDataRetirada())) {
                return erro("A devolução não pode ser antes da retirada.");
            }
            if (!"CARTAO".equals(forma) && !"PIX".equals(forma)) {
                return erro("Escolha a forma de pagamento.");
            }
            if (detalhe.isEmpty()) {
                return erro("Informe a bandeira do cartão ou o código da transação Pix.");
            }
            locacao.encerrar(km, dataDevolucao);
            if (!dao.encerrar(locacao)) {
                return erro("Não foi possível registrar a devolução.");
            }
            Pagamento pagamento;
            if ("CARTAO".equals(forma)) {
                pagamento = new PagamentoCartao(id, locacao.getValorFinal(), detalhe);
            } else {
                pagamento = new PagamentoPix(id, locacao.getValorFinal(), detalhe);
            }
            if (!pagamentoDAO.salvar(pagamento)) {
                return erro("Veículo devolvido, mas o pagamento não foi registrado.");
            }
            return new ResponseEntity<Object>("Devolução registrada. Valor final: R$ " + locacao.getValorFinal()
                    + ". Pago com: " + pagamento.descrever(), HttpStatus.OK);
        } catch (NumberFormatException e) {
            return erro("Informe a quilometragem em números.");
        } catch (DateTimeParseException e) {
            return erro("Informe a data da devolução.");
        }
    }

    private ResponseEntity<Object> erro(String mensagem) {
        return new ResponseEntity<Object>(mensagem, HttpStatus.BAD_REQUEST);
    }
}
