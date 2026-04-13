package controle;

import modelo.ArquivoCurso;
import modelo.ArquivoUsuario;
import modelo.Usuario;
import visao.VisaoUsuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Scanner;

public class ControleUsuario {

    private final ArquivoUsuario arquivoUsuario;
    private final ArquivoCurso arquivoCurso;
    private final VisaoUsuario visaoUsuario;

    public ControleUsuario(ArquivoUsuario arquivoUsuario, ArquivoCurso arquivoCurso, Scanner scanner) {
        this.arquivoUsuario = arquivoUsuario;
        this.arquivoCurso = arquivoCurso;
        this.visaoUsuario = new VisaoUsuario(scanner);
    }

    public Usuario fluxoCadastro() throws Exception {
        visaoUsuario.exibirCabecalho("Novo usuário");
        VisaoUsuario.DadosCadastroUsuario dados = visaoUsuario.leUsuario();

        if (dados.nome.isBlank() || dados.email.isBlank() || dados.senha.isBlank() ||
                dados.perguntaSecreta.isBlank() || dados.respostaSecreta.isBlank()) {
            visaoUsuario.mensagem("\nTodos os campos são obrigatórios.\n");
            return null;
        }

        Usuario usuario = new Usuario(
                dados.nome,
                dados.email,
                gerarHash(dados.senha),
                dados.perguntaSecreta,
                gerarHash(dados.respostaSecreta)
        );

        try {
            int id = arquivoUsuario.create(usuario);
            visaoUsuario.mensagem("\nUsuário cadastrado com sucesso! ID: " + id + "\n");
            return arquivoUsuario.read(id);
        } catch (Exception e) {
            visaoUsuario.mensagem("\nFalha ao cadastrar usuário: " + e.getMessage() + "\n");
            return null;
        }
    }

    public Usuario fluxoLogin() throws Exception {
        while (true) {
            visaoUsuario.exibirCabecalho("Login");

            String email = visaoUsuario.lerTexto("E-mail: ");
            String senha = visaoUsuario.lerTexto("Senha: ");

            Usuario usuario = arquivoUsuario.readPorEmail(email);
            if (usuario != null && Arrays.equals(usuario.getHashSenha(), gerarHash(senha))) {
                visaoUsuario.mensagem("\nLogin realizado com sucesso.\n");
                return usuario;
            }

            visaoUsuario.mensagem("\nE-mail ou senha inválidos.");
            String opcao = visaoUsuario.lerTexto("(T) Tentar novamente, (R) Recuperar senha, (V) Voltar: ").toUpperCase();
            System.out.println();

            if ("R".equals(opcao)) {
                fluxoRecuperacaoSenha(email);
            } else if ("V".equals(opcao)) {
                return null;
            }
        }
    }

    public void fluxoRecuperacaoSenha() throws Exception {
        fluxoRecuperacaoSenha(null);
    }

    public Usuario menuMeusDados(Usuario usuarioLogado) throws Exception {
        while (true) {
            Usuario atual = arquivoUsuario.read(usuarioLogado.getId());
            if (atual == null) {
                visaoUsuario.mensagem("\nUsuário não encontrado.\n");
                return null;
            }

            visaoUsuario.exibirCabecalho("Início > Meus dados");
            visaoUsuario.mostraUsuario(atual);
            System.out.println();
            System.out.println("(A) Alterar dados");
            System.out.println("(B) Excluir usuário");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.println();

            String opcao = visaoUsuario.lerTexto("Opção: ").toUpperCase();
            System.out.println();

            switch (opcao) {
                case "A" -> atualizarDados(atual);
                case "B" -> {
                    if (excluirUsuario(atual)) {
                        return null;
                    }
                }
                case "R" -> {
                    return atual;
                }
                default -> visaoUsuario.mensagem("Opção inválida.\n");
            }
        }
    }

    private void fluxoRecuperacaoSenha(String emailPreenchido) throws Exception {
        visaoUsuario.exibirCabecalho("Recuperação de senha");

        String email = emailPreenchido;
        if (email == null || email.isBlank()) {
            email = visaoUsuario.lerTexto("E-mail: ");
        } else {
            visaoUsuario.mensagem("E-mail: " + email);
        }

        Usuario usuario = arquivoUsuario.readPorEmail(email);
        if (usuario == null) {
            visaoUsuario.mensagem("\nUsuário não encontrado.\n");
            return;
        }

        visaoUsuario.mensagem("Pergunta secreta: " + usuario.getPerguntaSecreta());
        String resposta = visaoUsuario.lerTexto("Resposta: ");
        if (!Arrays.equals(usuario.getHashRespostaSecreta(), gerarHash(resposta))) {
            visaoUsuario.mensagem("\nResposta secreta inválida.\n");
            return;
        }

        String novaSenha = visaoUsuario.lerTexto("Nova senha: ");
        if (novaSenha.isBlank()) {
            visaoUsuario.mensagem("\nNova senha inválida.\n");
            return;
        }

        usuario.setHashSenha(gerarHash(novaSenha));
        arquivoUsuario.update(usuario);
        visaoUsuario.mensagem("\nSenha atualizada com sucesso.\n");
    }

    private void atualizarDados(Usuario atual) throws Exception {
        VisaoUsuario.DadosAtualizacaoUsuario dados = visaoUsuario.leAtualizacaoUsuario(atual);

        Usuario atualizado = new Usuario();
        atualizado.setId(atual.getId());
        atualizado.setNome(dados.nome.isBlank() ? atual.getNome() : dados.nome);
        atualizado.setEmail(dados.email.isBlank() ? atual.getEmail() : dados.email);
        atualizado.setHashSenha(dados.novaSenha.isBlank() ? atual.getHashSenha() : gerarHash(dados.novaSenha));
        atualizado.setPerguntaSecreta(dados.perguntaSecreta.isBlank() ? atual.getPerguntaSecreta() : dados.perguntaSecreta);
        atualizado.setHashRespostaSecreta(dados.respostaSecreta.isBlank()
                ? atual.getHashRespostaSecreta()
                : gerarHash(dados.respostaSecreta));

        try {
            boolean ok = arquivoUsuario.update(atualizado);
            if (ok) {
                visaoUsuario.mensagem("\nDados atualizados com sucesso.\n");
            } else {
                visaoUsuario.mensagem("\nNão foi possível atualizar os dados.\n");
            }
        } catch (Exception e) {
            visaoUsuario.mensagem("\nFalha na atualização: " + e.getMessage() + "\n");
        }
    }

    private boolean excluirUsuario(Usuario usuario) throws Exception {
        if (arquivoCurso.usuarioPossuiCursoAtivo(usuario.getId())) {
            visaoUsuario.mensagem("Exclusão negada: o usuário possui curso ativo vinculado.\n");
            return false;
        }

        arquivoCurso.excluirTodosCursosDoUsuario(usuario.getId());
        boolean ok = arquivoUsuario.delete(usuario.getId());
        if (ok) {
            visaoUsuario.mensagem("Usuário excluído com sucesso.\n");
            return true;
        }

        visaoUsuario.mensagem("Não foi possível excluir o usuário.\n");
        return false;
    }

    private static byte[] gerarHash(String texto) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(texto.getBytes(StandardCharsets.UTF_8));
    }
}
