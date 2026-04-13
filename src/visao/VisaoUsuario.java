package visao;

import modelo.Usuario;

import java.util.Scanner;

public class VisaoUsuario {

    public static class DadosCadastroUsuario {
        public final String nome;
        public final String email;
        public final String senha;
        public final String perguntaSecreta;
        public final String respostaSecreta;

        public DadosCadastroUsuario(String nome, String email, String senha, String perguntaSecreta, String respostaSecreta) {
            this.nome = nome;
            this.email = email;
            this.senha = senha;
            this.perguntaSecreta = perguntaSecreta;
            this.respostaSecreta = respostaSecreta;
        }
    }

    public static class DadosAtualizacaoUsuario {
        public final String nome;
        public final String email;
        public final String novaSenha;
        public final String perguntaSecreta;
        public final String respostaSecreta;

        public DadosAtualizacaoUsuario(String nome, String email, String novaSenha, String perguntaSecreta, String respostaSecreta) {
            this.nome = nome;
            this.email = email;
            this.novaSenha = novaSenha;
            this.perguntaSecreta = perguntaSecreta;
            this.respostaSecreta = respostaSecreta;
        }
    }

    private final Scanner scanner;

    public VisaoUsuario(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibirCabecalho(String breadcrumb) {
        System.out.println("EntrePares 1.0");
        System.out.println("--------------");
        System.out.println("> " + breadcrumb);
        System.out.println();
    }

    public DadosCadastroUsuario leUsuario() {
        String nome = lerTexto("Nome: ");
        String email = lerTexto("E-mail: ");
        String senha = lerTexto("Senha: ");
        String pergunta = lerTexto("Pergunta secreta: ");
        String resposta = lerTexto("Resposta secreta: ");
        return new DadosCadastroUsuario(nome, email, senha, pergunta, resposta);
    }

    public DadosAtualizacaoUsuario leAtualizacaoUsuario(Usuario usuarioAtual) {
        String nome = lerTexto("Nome [" + usuarioAtual.getNome() + "]: ");
        String email = lerTexto("E-mail [" + usuarioAtual.getEmail() + "]: ");
        String senha = lerTexto("Nova senha (vazio mantém): ");
        String pergunta = lerTexto("Pergunta secreta [" + usuarioAtual.getPerguntaSecreta() + "]: ");
        String resposta = lerTexto("Nova resposta secreta (vazio mantém): ");
        return new DadosAtualizacaoUsuario(nome, email, senha, pergunta, resposta);
    }

    public void mostraUsuario(Usuario usuario) {
        System.out.println("MEUS DADOS");
        System.out.println("ID.................: " + usuario.getId());
        System.out.println("NOME...............: " + usuario.getNome());
        System.out.println("E-MAIL.............: " + usuario.getEmail());
        System.out.println("PERGUNTA SECRETA...: " + usuario.getPerguntaSecreta());
    }

    public String lerTexto(String rotulo) {
        System.out.print(rotulo);
        return scanner.nextLine().trim();
    }

    public void mensagem(String texto) {
        System.out.println(texto);
    }
}
