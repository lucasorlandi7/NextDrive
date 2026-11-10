# Next Drive — Sistema em Java

Aplicação web de locação de veículos (Programação II). Java 17, JDBC puro e PostgreSQL, sem frameworks:
o servidor HTTP é o `com.sun.net.httpserver`, que já vem no JDK.

## Requisitos

- JDK 17 ou superior
- Maven 3.8 ou superior
- Banco `nextdrive` criado com os scripts `sql/02` a `sql/09` (veja o README da pasta `sql`)

## Como rodar

```bash
cd java
mvn compile exec:java
```

Abra <http://localhost:8080>.

Para gerar um JAR executável: `mvn package` e depois `java -jar target/nextdrive-jar-with-dependencies.jar`.

Para gerar o JavaDoc em HTML: `mvn javadoc:javadoc` (saída em `target/site/apidocs`).

## Configuração (variáveis de ambiente)

| Variável | Padrão | Uso |
|---|---|---|
| `NEXTDRIVE_DB_URL` | `jdbc:postgresql://localhost:5432/nextdrive` | URL do banco |
| `NEXTDRIVE_PORT` | `8080` | Porta do servidor web |
| `NEXTDRIVE_DB_USER_SITE` / `_ATENDENTE` / `_GERENTE` | `usr_site` / `usr_atendente` / `usr_gerente` | Usuários de banco de cada papel |
| `NEXTDRIVE_DB_SENHA_SITE` / `_ATENDENTE` / `_GERENTE` | `Trocar@123` | Senhas desses usuários |

No Windows (CMD): `set NEXTDRIVE_DB_URL=jdbc:postgresql://localhost:5432/nextdrive` antes do `mvn`.

## Usuários de exemplo (script `05_dados_exemplo.sql`)

| Perfil | E-mail | Senha |
|---|---|---|
| Gerente | `marcos.almeida@nextdrive.example` | `Senha@123` |
| Atendente | `juliana.prado@nextdrive.example` | `Senha@123` |
| Cliente | qualquer cliente do script (ex.: `ana.lima@example.com`, `carlos.pereira@example.com`) ou um novo em **Cadastre-se** | `Senha@123` (os do script) |

## Estrutura

```
src/main/java/br/edu/unoesc/nextdrive/
├── App.java            ponto de entrada
├── model/              domínio (herança Pessoa → Cliente → PF/PJ e Funcionario; Pagamento → Cartão/PIX; enums)
├── dao/                acesso ao banco com JDBC (PreparedStatement e CallableStatement)
├── service/            regras de aplicação; traduz erros do banco em mensagens
├── util/               conexão (um usuário de banco por papel) e resultado tabular
└── web/                servidor HTTP, sessão, rotas e telas de cada perfil
```

## Como o Java se liga ao banco

- Cada perfil usa um usuário de banco diferente (`usr_site`, `usr_atendente`, `usr_gerente`), então o próprio PostgreSQL limita o que cada um pode fazer.
- As regras críticas ficam no banco: retirada, devolução (cálculo do valor), pagamento e cancelamento chamam as procedures `sp_abrir_locacao`, `sp_registrar_devolucao`, `sp_registrar_pagamento` e `sp_cancelar_reserva`. Os triggers validam conflito de locação, idade e CNH do condutor, imutabilidade e auditoria.
- A senha nunca é lida pelo Java: o login chama `fn_autenticar`, que compara com bcrypt dentro do banco.
- Todas as consultas usam parâmetros (`?`), sem concatenar texto do usuário, e toda saída HTML é escapada.
- A reserva confere disponibilidade e grava na mesma transação, com bloqueio por categoria e loja, para duas reservas simultâneas não ficarem com o último veículo.

## Telas

| Perfil | Telas |
|---|---|
| Visitante | Login, cadastro de cliente, disponibilidade da frota |
| Cliente | Nova reserva (consulta, estimativa e confirmação), minhas reservas e locações |
| Atendente | Painel; cancelamento com justificativa; retirada; devolução com cálculo; pagamento (crédito, débito, PIX); multas; clientes PF e PJ e histórico |
| Gerente | Tudo do atendente, mais: relatórios com gráfico de faturamento e exportação CSV (extra), frota, categorias e preços, manutenção, funcionários e auditoria |

## Segurança

- Controle de acesso por perfil em cada rota (cliente não abre telas de funcionário; só gerente abre telas de gerente).
- Proteção contra CSRF: todo formulário leva um token da sessão.
- Cookie de sessão `HttpOnly` e `SameSite=Lax`.

## Limitações conhecidas

- O servidor usa HTTP simples. Em produção, coloque-o atrás de um proxy com HTTPS.
- Não há limite de tentativas de login.
- As sessões ficam na memória e se perdem ao reiniciar o servidor.
