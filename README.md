# TP1 — AEDs III — EntrePares 1.0

## Participantes

- *Paulo Gabriel de Oliveira Leite*
- *Samuel Lucas Rodrigues Vieira*
- *Carlos Eduardo de Melo Sabino*
- *Rubens Dias Bicalho*
---

## Descrição do sistema

O **EntrePares 1.0** é um sistema de console em Java para gerenciamento de cursos de estudo em pares. Cada usuário se cadastra, faz login e pode criar, editar, listar e encerrar cursos de sua autoria. O sistema foi construído sobre a infraestrutura de persistência do pacote `aed3` (CRUD genérico com índices diretos, Tabela Hash Extensível e Árvore B+) e segue o padrão MVC, separando as camadas em `modelo`, `visao` e `controle` - A recomendação e obrigatoriedade do Prof. Marcos Kutova

### Principais funcionalidades

- **Cadastro de usuário** com nome, e-mail único, senha (armazenada como hash SHA-256), pergunta secreta e resposta secreta (também em hash).
- **Login** por e-mail + senha, usando a Tabela Hash Extensível para localizar o usuário em O(1).
- **Recuperação de senha** usa uma pergunta secreta: o sistema exibe a pergunta cadastrada e, se a resposta conferir, permite definir uma nova senha.
- **Alteração dos dados do usuário**, incluindo troca de e-mail (com validação de unicidade) e troca de senha.
- **Exclusão de usuário**, bloqueada quando o usuário ainda possui cursos ativos (estado 0 ou 1). Cursos já concluídos/cancelados são removidos em cascata.
- **Criação de curso** com nome, data de início, descrição e um **código compartilhável** único gerado automaticamente (validado por hash extensível).
- **Listagem dos cursos do usuário logado**, em ordem alfabética, obtida a partir da Árvore B+ que modela o relacionamento 1:N entre usuários e cursos (Disponibilizado pelo repositório de AEDS III).
- **Atualização dos dados de um curso** (nome, data, descrição).
- **Transições de estado do curso**:
  - `0` → `1`: *Encerrar inscrições*.
  - `0`/`1` → `2`: *Concluir curso*.
  - *Cancelar curso*: como no TP1 ainda não tem as inscrições implementadas, exclui o curso de forma fisica.
- **Breadcrumbs** exibidos em todas as telas, no formato `Início > Meus Cursos > Nome do Curso`.

---

## Classes criadas

O projeto está organizado em quatro pastas principais:

### Pasta `aed3` — infraestrutura de persistência
Estes arquivos são adaptações diretas dos repositórios de código-base da disciplina (`CRUD2`, `TabelaHashExtensivel` e `ArvoreBMais` do repositório AEDsIII do Professor Kutova).

| `Registro` | Interface dos objetos armazenáveis no Arquivo (id + serialização). |
| `RegistroHashExtensivel` | Interface dos pares armazenáveis na Hash Extensível. |
| `RegistroArvoreBMais` | Interface dos pares armazenáveis na Árvore B+. |
| `Arquivo<T>` | CRUD genérico com cabeçalho, lista de registros deletados e índice direto por ID (Tabela Hash Extensível de `ParIDEndereco`). |
| `HashExtensivel<T>` | Tabela Hash Extensível com diretório dinâmico e duplicação por profundidade. |
| `ArvoreBMais<T>` | Árvore B+ genérica com folhas encadeadas, suportando consulta por prefixo (coringa). |
| `ParIDEndereco` | Par (ID, endereço em bytes) usado internamente pelo índice direto do `Arquivo`. |

### Pasta `modelo` — entidades e pares de índice

| `Usuario` | Entidade do usuário (id, nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta). |
| `Curso` | Entidade do curso (id, nome, dataInicio, descricao, codigoCompartilhavel, estado, idUsuario). |
| `ParEmailID` | Par para índice hash `email → idUsuario` (tamanho fixo de 124 bytes). |
| `ParCodigoCursoID` | Par para índice hash `codigoCompartilhavel → idCurso`. |
| `ParIntInt` | Par `(idUsuario, idCurso)` para a Árvore B+ do relacionamento 1:N. `compareTo` trata `-1` no segundo campo como coringa, permitindo `read(new ParIntInt(idUsuario, -1))` retornar todos os cursos do usuário. |
| `ParNomeIDCurso` | Par `(nomeCurso, idCurso)` para a Árvore B+ que mantém os cursos ordenados alfabeticamente. |
| `ArquivoUsuario` | Estende `Arquivo<Usuario>`. Mantém o índice `indiceEmail: HashExtensivel<ParEmailID>` para unicidade e busca por e-mail. |
| `ArquivoCurso` | Estende `Arquivo<Curso>`. Mantém três índices: `indiceCodigo` (hash), `indiceUsuarioCurso: ArvoreBMais<ParIntInt>` (relacionamento 1:N) e `indiceNomeCurso: ArvoreBMais<ParNomeIDCurso>` (ordenação por nome). |

### Pasta `visao` — telas

| `VisaoUsuario` | Formulários e menus de cadastro, login, recuperação de senha, alteração e exclusão de usuário. |
| `VisaoCurso` | Listagem de cursos, criação, edição, menu de ações do curso selecionado, exibição de breadcrumbs. |

### Pasta `controle` — regras de negócio

| `ControleUsuario` | Orquestra cadastro, login, recuperação de senha e exclusão de usuário (com as restrições de cursos ativos). |
| `ControleCurso` | Orquestra criação, atualização e transições de estado dos cursos; aplica a listagem ordenada por nome. |

### Classe raiz

| `Principal` | Ponto de entrada; instancia `ArquivoUsuario`/`ArquivoCurso`, monta a sessão e fecha os recursos ao sair. |


## Operações especiais

1. **Consulta 1:N via Árvore B+ com chave parcial.** A busca `indiceUsuarioCurso.read(new ParIntInt(idUsuario, -1))` aproveita o `compareTo` de `ParIntInt`, que trata `num2 == -1` como coringa, para devolver *todos* os pares cujo primeiro campo é `idUsuario`. É assim que a tela "Meus Cursos" obtém a lista de cursos do usuário logado sem varredura linear do arquivo.

2. **Listagem alfabética de cursos do usuário.** Para cada ID retornado pela Árvore B+ do relacionamento 1:N, o `ArquivoCurso` lê o curso correspondente e devolve a lista ordenada por nome, que a `VisaoCurso` apresenta com numeração sequencial (1, 2, 3...). O projeto ainda mantém, para extensões futuras, o índice `indiceNomeCurso: ArvoreBMais<ParNomeIDCurso>`, atualizado automaticamente em toda operação de CRUD de curso.

3. **Geração de código compartilhável único.** Ao criar um curso, o sistema sorteia um código alfanumérico e verifica a colisão em O(1) usando `indiceCodigo: HashExtensivel<ParCodigoCursoID>`; em caso de colisão, sorteia novamente.

4. **Unicidade de e-mail com Hash Extensível.** O `create` e o `update` de `ArquivoUsuario` consultam `indiceEmail` antes de gravar o registro, rejeitando duplicatas. E-mails são normalizados (trim + lowercase) antes de hashear.

5. **Hash SHA-256 de senha e resposta secreta.** Nem a senha nem a resposta da pergunta secreta são armazenadas em texto claro. A recuperação de senha compara os hashes.

6. **Exclusão em cascata controlada.** `ArquivoCurso.usuarioPossuiCursoAtivo(idUsuario)` bloqueia a exclusão do usuário quando existe curso em estado 0 ou 1. Caso contrário, `excluirTodosCursosDoUsuario(idUsuario)` remove todos os cursos (concluídos/cancelados) antes de apagar o usuário, mantendo os três índices de curso consistentes.

7. **Transições de estado do curso.** O método `alterarEstado` do `ArquivoCurso` centraliza as mudanças `0 → 1` (encerrar inscrições) e `0/1 → 2` (concluir curso), garantindo que apenas transições válidas sejam efetivadas.

8. **Sincronização automática de índices secundários.** Os sobrescritos `create`, `update` e `delete` de `ArquivoUsuario` e `ArquivoCurso` propagam as mudanças para todos os índices (hash e B+) envolvidos, de modo que o chamador não precisa se preocupar com a consistência.

---

## Capturas de tela

1. **Tela inicial (Login / Novo usuário / Sair).**
   ![Tela inicial](docs/tela-inicial.png)

2. **Login do usuário.**
   ![Novo usuário](docs/login-usuario.png)

4. **Listagem de "Meus Cursos" ordenada por nome e criação de curso com código compartilhável gerado.**
   ![Meus cursos](docs/cursos.png)

6. **Tela de um curso selecionado com opções de encerrar inscrições / concluir / cancelar.**
   ![Curso selecionado](docs/informacoes-cursos.png)

7. **Cancelamento de um curso**
   ![Meus dados](docs/cancelamento-curso.png)

---

## Vídeo de demonstração

- Link: **A PREENCHER**

## Checklist de entrega

**1. Há um CRUD de usuários (que estende a classe `ArquivoIndexado`, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente?**
**Sim.** A classe `modelo.ArquivoUsuario` estende `aed3.Arquivo<Usuario>` e acrescenta o índice indireto `indiceEmail: HashExtensivel<ParEmailID>`, usado para garantir unicidade de e-mail e para o login.

**2. Há um CRUD de cursos (que estende a classe `ArquivoIndexado`, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente?**
**Sim.** A classe `modelo.ArquivoCurso` estende `aed3.Arquivo<Curso>` e acrescenta três índices secundários: `indiceCodigo` (Hash Extensível para o código compartilhável), `indiceUsuarioCurso` (Árvore B+ de `ParIntInt` para o relacionamento 1:N) e `indiceNomeCurso` (Árvore B+ de `ParNomeIDCurso` para a ordem alfabética).

**3. Os cursos estão vinculados aos usuários usando o idUsuario como chave estrangeira?**
**Sim.** A entidade `Curso` possui o campo `idUsuario`, preenchido automaticamente a partir da sessão do usuário logado no momento da criação. O `ControleCurso` usa esse campo em todas as consultas para restringir as operações aos cursos do próprio usuário.

**4. Há uma árvore B+ que registre o relacionamento 1:N entre usuários e cursos?**
**Sim.** O índice `indiceUsuarioCurso: ArvoreBMais<ParIntInt>` mantém um par `(idUsuario, idCurso)` para cada curso. A consulta `read(new ParIntInt(idUsuario, -1))` retorna todos os cursos de um usuário, aproveitando o `compareTo` de `ParIntInt` que trata `-1` como coringa.

**5. O trabalho compila corretamente?**
**Sim.** O projeto compila com `javac` a partir da pasta `src/`, sem dependências externas além do JDK.

**6. O trabalho está completo e funcionando sem erros de execução?**
**Sim.** Os fluxos de cadastro, login, recuperação de senha, CRUD de usuário, CRUD de curso, transições de estado e listagem ordenada foram testados manualmente (Testado por: Paulo Gabriel de Oliveira Leite - 860144) e funcionam conforme o enunciado do TP1.

**7. O trabalho é original e não a cópia de um trabalho de outro grupo?**
**Sim.** O código foi escrito pelos integrantes do grupo. As únicas partes reaproveitadas são as classes do pacote `aed3` (CRUD genérico, Tabela Hash Extensível e Árvore B+), que são código-base oficial da disciplina e de uso obrigatório segundo o enunciado. P.S: foi utilizado I.A (Claude) **SOMENTE** para melhorar a compreensão e corrigir sintaticamente os textos das Strings, visando manter a normal culta da lingua portuguesa.

---

## Como executar

Na raiz do repositório:

cd src
javac Principal.java
java Principal

Os arquivos de dados são criados automaticamente nas pastas `dados/usuarios/` e `dados/cursos/` na primeira execução.
