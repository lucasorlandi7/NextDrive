function verificarLogin() {
    if (sessionStorage.getItem('logado') !== 'sim') {
        window.location.href = 'index.html';
    }
}

function montarMenu(atual) {
    var paginas = [
        ['menu.html', 'Menu'],
        ['clientes.html', 'Clientes'],
        ['carros.html', 'Carros'],
        ['alugar.html', 'Alugar'],
        ['locacoes.html', 'Locações']
    ];
    var links = paginas.map(function (p) {
        var ativo = p[0] === atual ? ' active' : '';
        return '<li class="nav-item"><a class="nav-link' + ativo + '" href="' + p[0] + '">' + p[1] + '</a></li>';
    }).join('');
    $('#topo').html(
        '<nav class="navbar navbar-expand navbar-dark bg-dark mb-3"><div class="container">' +
        '<span class="navbar-brand">Next Drive</span>' +
        '<ul class="navbar-nav me-auto">' + links + '</ul>' +
        '<a class="btn btn-outline-light btn-sm" href="index.html" id="sair">Sair</a>' +
        '</div></nav>');
    $('#sair').click(function () {
        sessionStorage.removeItem('logado');
    });
}

function mostrar(texto, tipo) {
    $('#mensagem').removeClass('d-none alert-success alert-danger').addClass('alert-' + tipo).text(texto);
    window.scrollTo(0, 0);
}

function enviar(tipo, url, dados, aoTerminar) {
    $.ajax({
        url: url,
        type: tipo,
        contentType: 'application/json',
        data: dados ? JSON.stringify(dados) : null,
        success: function (resposta) {
            mostrar(resposta, 'success');
            aoTerminar();
        },
        error: function (erro) {
            mostrar(erro.responseText || 'Erro ao falar com o servidor.', 'danger');
        }
    });
}

function linha(celulas) {
    var tr = $('<tr>');
    celulas.forEach(function (c) {
        var td = $('<td>');
        if (c !== null && typeof c === 'object' && c.jquery) {
            td.append(c);
        } else {
            td.text(c === null || c === undefined ? '' : c);
        }
        tr.append(td);
    });
    return tr;
}

function botao(texto, classe, acao) {
    return $('<button class="btn btn-sm ' + classe + ' me-1">').text(texto).click(acao);
}

function dinheiro(valor) {
    return 'R$ ' + Number(valor).toFixed(2).replace('.', ',');
}

function hoje() {
    return new Date().toISOString().slice(0, 10);
}
