import controle.ControleCurso;
import controle.ControleUsuario;
import modelo.ArquivoCurso;
import modelo.ArquivoUsuario;
import modelo.Usuario;

import java.util.Scanner;

public class Principal {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArquivoUsuario arquivoUsuario = null;
        ArquivoCurso arquivoCurso = null;

        try {
            arquivoUsuario = new ArquivoUsuario();
            arquivoCurso = new ArquivoCurso();

            ControleUsuario controleUsuario = new ControleUsuario(arquivoUsuario, arquivoCurso, scanner);
            ControleCurso controleCurso = new ControleCurso(arquivoCurso, scanner);

            executarAplicacao(scanner, controleUsuario, controleCurso);

        } catch (Exception e) {
            System.out.println("Erro fatal na aplicação: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (arquivoUsuario != null) {
                    arquivoUsuario.close();
                }
            } catch (Exception e) {
                System.out.println("Falha ao fechar arquivo de usuários: " + e.getMessage());
            }

            try {
                if (arquivoCurso != null) {
                    arquivoCurso.close();
                }
            } catch (Exception e) {
                System.out.println("Falha ao fechar arquivo de cursos: " + e.getMessage());
            }

            scanner.close();
        }
    }

    private static void executarAplicacao(Scanner scanner, ControleUsuario controleUsuario, ControleCurso controleCurso) throws Exception {
        boolean executando = true;

        while (executando) {
            exibirTelaInicial();
            String opcao = scanner.nextLine().trim().toUpperCase();
            System.out.println();

            switch (opcao) {
                case "A" -> {
                    Usuario usuario = controleUsuario.fluxoLogin();
                    if (usuario != null) {
                        menuPrincipal(scanner, controleUsuario, controleCurso, usuario);
                    }
                }
                case "B" -> controleUsuario.fluxoCadastro();
                case "S" -> executando = false;
                default -> System.out.println("Opção inválida.\n");
            }
        }

        System.out.println("Até logo!");
    }

    private static void menuPrincipal(Scanner scanner, ControleUsuario controleUsuario, ControleCurso controleCurso, Usuario usuarioLogado) throws Exception {
        Usuario sessao = usuarioLogado;
        boolean ativo = true;

        while (ativo && sessao != null) {
            System.out.println("EntrePares 1.0");
            System.out.println("--------------");
            System.out.println("> Início");
            System.out.println();
            System.out.println("(A) Meus dados");
            System.out.println("(B) Meus cursos");
            System.out.println("(C) Minhas inscrições");
            System.out.println();
            System.out.println("(S) Sair");
            System.out.println();
            System.out.print("Opção: ");

            String opcao = scanner.nextLine().trim().toUpperCase();
            System.out.println();

            switch (opcao) {
                case "A" -> sessao = controleUsuario.menuMeusDados(sessao);
                case "B" -> controleCurso.menuMeusCursos(sessao);
                case "C" -> System.out.println("Funcionalidade disponível apenas no TP2.\n");
                case "S" -> ativo = false;
                default -> System.out.println("Opção inválida.\n");
            }
        }
    }

    private static void exibirTelaInicial() {
        System.out.println("EntrePares 1.0");
        System.out.println("--------------");
        System.out.println();
        System.out.println("(A) Login");
        System.out.println("(B) Novo usuário");
        System.out.println();
        System.out.println("(S) Sair");
        System.out.println();
        System.out.print("Opção: ");
    }
}
