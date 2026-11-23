package br.edu.unoesc.nextdrive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.edu.unoesc.nextdrive.model.Categoria;
import br.edu.unoesc.nextdrive.model.Cliente;
import br.edu.unoesc.nextdrive.model.Locacao;
import br.edu.unoesc.nextdrive.model.Pagamento;
import br.edu.unoesc.nextdrive.model.PagamentoCartao;
import br.edu.unoesc.nextdrive.model.PagamentoPix;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.model.StatusLocacao;
import br.edu.unoesc.nextdrive.model.StatusReserva;
import br.edu.unoesc.nextdrive.model.Veiculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;

/** Testes das regras do sistema que não dependem do banco de dados. */
public class NextDriveTest {

    private static final BigDecimal DIARIA = new BigDecimal("100.00");

    private Cliente novoCliente() {
        return new Cliente("Ana", "11122233344", "ana@x.com", "49999990001", "12345678901",
                LocalDate.of(2030, 1, 1));
    }

    private Locacao novaLocacao(LocalDate retirada, LocalDate devolucaoPrevista) {
        Categoria categoria = new Categoria(1, "Econômico", DIARIA);
        Reserva reserva = new Reserva(novoCliente(), categoria, retirada, devolucaoPrevista);
        Veiculo veiculo = new Veiculo("ABC1D23", "Fiat Mobi", 2023, 1000, categoria);
        return new Locacao(reserva, veiculo, retirada);
    }

    @Test
    public void valorDeTresDiasSemAtraso() {
        Locacao locacao = novaLocacao(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        BigDecimal valor = locacao.calcularValorFinal(LocalDate.of(2026, 12, 4));
        assertEquals(new BigDecimal("300.00"), valor);
    }

    @Test
    public void devolucaoNoMesmoDiaCobraUmaDiaria() {
        Locacao locacao = novaLocacao(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 2));
        BigDecimal valor = locacao.calcularValorFinal(LocalDate.of(2026, 12, 1));
        assertEquals(new BigDecimal("100.00"), valor);
    }

    @Test
    public void atrasoAcrescentaTrintaPorCentoDaDiariaPorDia() {
        Locacao locacao = novaLocacao(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        BigDecimal valor = locacao.calcularValorFinal(LocalDate.of(2026, 12, 6));
        assertEquals(new BigDecimal("560.00"), valor);
    }

    @Test
    public void encerrarLocacaoGuardaOsDadosDaDevolucao() {
        Locacao locacao = novaLocacao(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        locacao.encerrar(1500, LocalDate.of(2026, 12, 4));
        assertEquals(StatusLocacao.ENCERRADA, locacao.getStatus());
        assertEquals(new BigDecimal("300.00"), locacao.getValorFinal());
        assertEquals(Integer.valueOf(1500), locacao.getKmChegada());
    }

    @Test
    public void saldoDevedorDescontaOQueJaFoiPago() {
        Locacao locacao = novaLocacao(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        locacao.encerrar(1500, LocalDate.of(2026, 12, 4));
        assertEquals(new BigDecimal("200.00"), locacao.saldoDevedor(new BigDecimal("100.00")));
    }

    @Test
    public void cnhVencidaNaoEhValida() {
        Cliente cliente = new Cliente("Ana", "11122233344", "ana@x.com", "1", "12345678901",
                LocalDate.of(2026, 1, 1));
        assertTrue(cliente.cnhValidaEm(LocalDate.of(2025, 12, 31)));
        assertFalse(cliente.cnhValidaEm(LocalDate.of(2026, 1, 2)));
    }

    @Test
    public void pagamentosDescrevemSeDeFormaDiferente() {
        Pagamento cartao = new PagamentoCartao(1, DIARIA, "Visa");
        Pagamento pix = new PagamentoPix(1, DIARIA, "TX123");
        assertEquals("Cartão Visa", cartao.descrever());
        assertEquals("Pix (txid TX123)", pix.descrever());
        assertEquals("CARTAO", cartao.getTipo());
        assertEquals("PIX", pix.getTipo());
    }

    @Test
    public void soReservaAtivaPodeSerCancelada() {
        Reserva reserva = new Reserva(novoCliente(), new Categoria(1, "Econômico", DIARIA),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3));
        assertTrue(reserva.cancelar());
        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        assertFalse(reserva.cancelar());
    }

    @Test
    public void estimativaDaReservaUsaADiariaDaCategoria() {
        Reserva reserva = new Reserva(novoCliente(), new Categoria(1, "Econômico", DIARIA),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        assertEquals(new BigDecimal("300.00"), reserva.estimativa());
    }
}
