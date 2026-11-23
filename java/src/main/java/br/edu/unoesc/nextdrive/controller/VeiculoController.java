package br.edu.unoesc.nextdrive.controller;

import br.edu.unoesc.nextdrive.dao.CategoriaDAO;
import br.edu.unoesc.nextdrive.dao.VeiculoDAO;
import br.edu.unoesc.nextdrive.model.Categoria;
import br.edu.unoesc.nextdrive.model.StatusVeiculo;
import br.edu.unoesc.nextdrive.model.Veiculo;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de veículos e categorias: listar, cadastrar, excluir e mandar para manutenção. */
@RestController
public class VeiculoController {

    private VeiculoDAO dao = new VeiculoDAO();
    private CategoriaDAO categoriaDAO = new CategoriaDAO();

    /**
     * Lista as categorias de veículo.
     *
     * @return lista de categorias em JSON
     */
    @GetMapping("/categorias")
    public ResponseEntity<Object> categorias() {
        return new ResponseEntity<Object>(categoriaDAO.listar(), HttpStatus.OK);
    }

    /**
     * Lista todos os veículos.
     *
     * @return lista de veículos em JSON
     */
    @GetMapping("/veiculos")
    public ResponseEntity<Object> listar() {
        return new ResponseEntity<Object>(dao.listar(), HttpStatus.OK);
    }

    /**
     * Cadastra um veículo novo.
     *
     * @param dados categoriaId, placa, modelo, ano e quilometragem
     * @return mensagem de sucesso ou de erro
     */
    @PostMapping("/veiculos")
    public ResponseEntity<Object> salvar(@RequestBody Map<String, String> dados) {
        try {
            String placa = texto(dados, "placa").toUpperCase();
            String modelo = texto(dados, "modelo");
            int ano = Integer.parseInt(texto(dados, "ano"));
            int km = Integer.parseInt(texto(dados, "quilometragem"));
            Categoria categoria = categoriaDAO.buscar(Integer.parseInt(texto(dados, "categoriaId")));
            if (placa.isEmpty() || placa.length() > 8 || modelo.isEmpty()) {
                return erro("Informe a placa (até 8 caracteres) e o modelo.");
            }
            if (ano < 1990 || ano > 2100) {
                return erro("O ano precisa estar entre 1990 e 2100.");
            }
            if (km < 0) {
                return erro("A quilometragem não pode ser negativa.");
            }
            if (categoria == null) {
                return erro("Escolha a categoria.");
            }
            if (!dao.salvar(new Veiculo(placa, modelo, ano, km, categoria))) {
                return erro("Não foi possível salvar. Confira se a placa já está cadastrada.");
            }
            return new ResponseEntity<Object>("Veículo cadastrado.", HttpStatus.CREATED);
        } catch (NumberFormatException e) {
            return erro("Ano, quilometragem e categoria precisam ser números.");
        }
    }

    /**
     * Manda o veículo para manutenção ou deixa ele disponível de novo.
     *
     * @param id    código do veículo
     * @param dados situacao (MANUTENCAO ou DISPONIVEL)
     * @return mensagem de sucesso ou de erro
     */
    @PutMapping("/veiculos/{id}/situacao")
    public ResponseEntity<Object> mudarSituacao(@PathVariable("id") int id,
                                                @RequestBody Map<String, String> dados) {
        Veiculo veiculo = dao.buscar(id);
        if (veiculo == null) {
            return erro("Veículo não encontrado.");
        }
        if (veiculo.getStatus() == StatusVeiculo.LOCADO) {
            return erro("Veículo locado: a situação muda sozinha na devolução.");
        }
        String situacao = texto(dados, "situacao");
        if (situacao.equals("MANUTENCAO")) {
            veiculo.setStatus(StatusVeiculo.MANUTENCAO);
        } else if (situacao.equals("DISPONIVEL")) {
            veiculo.setStatus(StatusVeiculo.DISPONIVEL);
        } else {
            return erro("Situação inválida.");
        }
        if (!dao.atualizar(veiculo)) {
            return erro("Não foi possível mudar a situação do veículo.");
        }
        return new ResponseEntity<Object>("Situação do veículo atualizada: " + veiculo.getStatus(),
                HttpStatus.OK);
    }

    /**
     * Exclui um veículo.
     *
     * @param id código do veículo
     * @return mensagem de sucesso ou de erro
     */
    @DeleteMapping("/veiculos/{id}")
    public ResponseEntity<Object> excluir(@PathVariable("id") int id) {
        if (!dao.excluir(id)) {
            return erro("Veículo não excluído (não existe ou já foi alugado).");
        }
        return new ResponseEntity<Object>("Veículo excluído.", HttpStatus.OK);
    }

    private String texto(Map<String, String> dados, String campo) {
        String valor = dados.get(campo);
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }

    private ResponseEntity<Object> erro(String mensagem) {
        return new ResponseEntity<Object>(mensagem, HttpStatus.BAD_REQUEST);
    }
}
