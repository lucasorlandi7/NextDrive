package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Cliente;
import br.edu.unoesc.nextdrive.model.Locacao;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.model.StatusLocacao;
import br.edu.unoesc.nextdrive.model.Veiculo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela locacao. A retirada e a devolução mexem em mais de uma tabela, numa transação só. */
public class LocacaoDAO {

    private Connection connection = SingleConnection.getConnection();
    private ReservaDAO reservaDAO = new ReservaDAO();
    private VeiculoDAO veiculoDAO = new VeiculoDAO();

    /**
     * Registra o aluguel de um veículo. Numa única transação: cria a reserva (já efetivada),
     * cria a locação e marca o veículo como LOCADO.
     *
     * @param cliente           quem está alugando
     * @param veiculo           veículo escolhido
     * @param dataRetirada      dia da retirada
     * @param devolucaoPrevista dia previsto para a devolução
     * @return true se deu certo; se algo falhar, nada é gravado
     */
    public boolean alugar(Cliente cliente, Veiculo veiculo, LocalDate dataRetirada, LocalDate devolucaoPrevista) {
        String sqlReserva = "INSERT INTO reserva (cliente_id, categoria_id, data_retirada_prevista, "
                + "data_devolucao_prevista, status) VALUES (?, ?, ?, ?, 'EFETIVADA')";
        String sqlLocacao = "INSERT INTO locacao (reserva_id, veiculo_id, data_retirada, km_saida) "
                + "VALUES (currval('reserva_id_seq'), ?, ?, ?)";
        String sqlVeiculo = "UPDATE veiculo SET status = 'LOCADO' WHERE id = ? AND status = 'DISPONIVEL'";
        try {
            PreparedStatement ps = connection.prepareStatement(sqlReserva);
            ps.setInt(1, cliente.getId());
            ps.setInt(2, veiculo.getCategoria().getId());
            ps.setDate(3, Date.valueOf(dataRetirada));
            ps.setDate(4, Date.valueOf(devolucaoPrevista));
            ps.execute();
            ps.close();

            ps = connection.prepareStatement(sqlLocacao);
            ps.setInt(1, veiculo.getId());
            ps.setDate(2, Date.valueOf(dataRetirada));
            ps.setInt(3, veiculo.getQuilometragem());
            ps.execute();
            ps.close();

            ps = connection.prepareStatement(sqlVeiculo);
            ps.setInt(1, veiculo.getId());
            int linhas = ps.executeUpdate();
            ps.close();
            if (linhas == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            SingleConnection.erro("registrar o aluguel", e);
            return false;
        }
    }

    /**
     * Registra a devolução. Numa única transação: grava os dados da devolução na locação
     * e deixa o veículo DISPONIVEL com a quilometragem nova.
     *
     * @param l locação já encerrada com {@link Locacao#encerrar(int, LocalDate)}
     * @return true se deu certo; se algo falhar, nada é gravado
     */
    public boolean encerrar(Locacao l) {
        String sqlLocacao = "UPDATE locacao SET data_devolucao_real = ?, km_chegada = ?, "
                + "valor_final = ?, status = 'ENCERRADA' WHERE id = ?";
        String sqlVeiculo = "UPDATE veiculo SET quilometragem = ?, status = 'DISPONIVEL' WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sqlLocacao);
            ps.setDate(1, Date.valueOf(l.getDataDevolucaoReal()));
            ps.setInt(2, l.getKmChegada());
            ps.setBigDecimal(3, l.getValorFinal());
            ps.setInt(4, l.getId());
            ps.execute();
            ps.close();

            ps = connection.prepareStatement(sqlVeiculo);
            ps.setInt(1, l.getKmChegada());
            ps.setInt(2, l.getVeiculo().getId());
            ps.execute();
            ps.close();

            connection.commit();
            return true;
        } catch (SQLException e) {
            SingleConnection.erro("registrar a devolução", e);
            return false;
        }
    }

    /**
     * Lista todas as locações.
     *
     * @return lista de locações ordenada pelo código
     */
    public List<Locacao> listar() {
        List<Integer> ids = new ArrayList<Integer>();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT id FROM locacao ORDER BY id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("listar as locações", e);
        }
        List<Locacao> lista = new ArrayList<Locacao>();
        for (int id : ids) {
            lista.add(buscar(id));
        }
        return lista;
    }

    /**
     * Busca uma locação pelo código.
     *
     * @param id código da locação
     * @return a locação, ou null se não existir
     */
    public Locacao buscar(int id) {
        int reservaId = 0;
        int veiculoId = 0;
        LocalDate dataRetirada = null;
        LocalDate dataDevolucao = null;
        int kmSaida = 0;
        Integer kmChegada = null;
        BigDecimal valorFinal = null;
        StatusLocacao status = null;
        boolean achou = false;
        String sql = "SELECT reserva_id, veiculo_id, data_retirada, data_devolucao_real, km_saida, "
                + "km_chegada, valor_final, status FROM locacao WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                achou = true;
                reservaId = rs.getInt("reserva_id");
                veiculoId = rs.getInt("veiculo_id");
                dataRetirada = rs.getDate("data_retirada").toLocalDate();
                if (rs.getDate("data_devolucao_real") != null) {
                    dataDevolucao = rs.getDate("data_devolucao_real").toLocalDate();
                }
                kmSaida = rs.getInt("km_saida");
                int km = rs.getInt("km_chegada");
                if (!rs.wasNull()) {
                    kmChegada = km;
                }
                valorFinal = rs.getBigDecimal("valor_final");
                status = StatusLocacao.valueOf(rs.getString("status"));
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("buscar a locação", e);
        }
        if (!achou) {
            return null;
        }
        Reserva reserva = reservaDAO.buscar(reservaId);
        Veiculo veiculo = veiculoDAO.buscar(veiculoId);
        Locacao l = new Locacao(reserva, veiculo, dataRetirada);
        l.setId(id);
        l.setKmSaida(kmSaida);
        l.setDataDevolucaoReal(dataDevolucao);
        l.setKmChegada(kmChegada);
        l.setValorFinal(valorFinal);
        l.setStatus(status);
        return l;
    }
}
