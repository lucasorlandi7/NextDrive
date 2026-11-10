package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.StatusVeiculo;
import br.edu.unoesc.nextdrive.model.Veiculo;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Acesso à frota de veículos e à consulta de disponibilidade. */
public class VeiculoDAO {
    private final Connection con;

    public VeiculoDAO(Connection con) { this.con = con; }

    /**
     * Veículos livres de locação e de manutenção no período (função {@code fn_veiculos_disponiveis}).
     *
     * @param categoriaId   categoria desejada
     * @param lojaId        loja onde o veículo está
     * @param inicio        início do período
     * @param fim           fim do período
     * @param somenteNaLoja se true, só devolve veículos fisicamente disponíveis agora (retirada)
     * @return veículos com id, placa, marca, modelo e quilometragem
     * @throws SQLException em erro de banco
     */
    public List<Veiculo> disponiveis(int categoriaId, int lojaId, LocalDateTime inicio, LocalDateTime fim,
                                     boolean somenteNaLoja) throws SQLException {
        String sql = "SELECT f.veiculo_id, f.placa, f.marca, f.modelo, v.quilometragem "
                   + "FROM fn_veiculos_disponiveis(?, ?, ?, ?) f JOIN veiculo v ON v.id = f.veiculo_id "
                   + (somenteNaLoja ? "WHERE v.status = 'DISPONIVEL' " : "")
                   + "ORDER BY f.placa";
        List<Veiculo> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            ps.setInt(2, lojaId);
            ps.setTimestamp(3, Timestamp.valueOf(inicio));
            ps.setTimestamp(4, Timestamp.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Veiculo v = new Veiculo();
                    v.setId(rs.getInt(1));
                    v.setPlaca(rs.getString(2));
                    v.setMarca(rs.getString(3));
                    v.setModelo(rs.getString(4));
                    v.setQuilometragem(rs.getInt(5));
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    /**
     * Cadastra um veículo (somente gerente). A placa é única.
     *
     * @param v dados do veículo
     * @throws SQLException se a placa for repetida ou algum dado violar as regras
     */
    public void inserir(Veiculo v) throws SQLException {
        String sql = "INSERT INTO veiculo (placa, marca, modelo, ano, cor, categoria_id, loja_atual_id, "
                   + "quilometragem, capacidade_tanque_litros) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAno());
            ps.setString(5, v.getCor());
            ps.setInt(6, v.getCategoriaId());
            ps.setInt(7, v.getLojaAtualId());
            ps.setInt(8, v.getQuilometragem());
            ps.setInt(9, v.getCapacidadeTanqueLitros());
            ps.executeUpdate();
        }
    }

    /**
     * Frota com a utilização de cada veículo.
     *
     * @return linhas da view {@code vw_utilizacao_frota}
     * @throws SQLException em erro de banco
     */
    public Tabela frota() throws SQLException {
        String sql = "SELECT veiculo_id AS id, placa, veiculo, categoria, loja_atual AS loja, status, "
                   + "qtd_locacoes AS locacoes, km_rodados AS km, receita_total AS receita "
                   + "FROM vw_utilizacao_frota ORDER BY placa";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }

    /**
     * Veículos que podem ir para manutenção (não estão locados nem em manutenção).
     *
     * @return veículos com id, placa, marca e modelo
     * @throws SQLException em erro de banco
     */
    public List<Veiculo> paraManutencao() throws SQLException {
        List<Veiculo> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id, placa, marca, modelo FROM veiculo WHERE status = 'DISPONIVEL' ORDER BY placa");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Veiculo v = new Veiculo();
                v.setId(rs.getInt(1));
                v.setPlaca(rs.getString(2));
                v.setMarca(rs.getString(3));
                v.setModelo(rs.getString(4));
                lista.add(v);
            }
        }
        return lista;
    }

    /**
     * Desativa ou reativa um veículo. Só vale para veículos que não estão locados nem em manutenção.
     *
     * @param id     código do veículo
     * @param status DISPONIVEL ou INATIVO
     * @return true se a mudança foi aplicada
     * @throws SQLException em erro de banco
     */
    public boolean alterarStatus(int id, StatusVeiculo status) throws SQLException {
        String sql = "UPDATE veiculo SET status = ? WHERE id = ? AND status IN ('DISPONIVEL', 'INATIVO')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Situação da frota por loja e categoria (RF06).
     *
     * @return linhas da view {@code vw_disponibilidade_frota}
     * @throws SQLException em erro de banco
     */
    public Tabela situacaoDaFrota() throws SQLException {
        String sql = "SELECT loja, categoria, valor_diaria AS diaria, total_veiculos AS total, disponiveis, "
                   + "locados, em_manutencao AS manutencao FROM vw_disponibilidade_frota ORDER BY loja, categoria";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }
}
