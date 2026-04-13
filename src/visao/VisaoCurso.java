package visao;

import modelo.Curso;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class VisaoCurso {

    public static class DadosCurso {
        public final String nome;
        public final String dataInicio;
        public final String descricao;

        public DadosCurso(String nome, String dataInicio, String descricao) {
            this.nome = nome;
            this.dataInicio = dataInicio;
            this.descricao = descricao;
        }
    }

    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Scanner scanner;

    public VisaoCurso(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibirCabecalho(String breadcrumb) {
        System.out.println("EntrePares 1.0");
        System.out.println("--------------");
        System.out.println("> " + breadcrumb);
        System.out.println();
    }

    public void mostrarListaCursos(List<Curso> cursos) {
        System.out.println("CURSOS");
        if (cursos.isEmpty()) {
            System.out.println("(nenhum curso cadastrado)");
            return;
        }

        int i = 1;
        for (Curso curso : cursos) {
            String data = curso.getDataInicio() != null ? curso.getDataInicio().format(FORMATADOR_DATA) : "--/--/----";
            System.out.println("(" + i + ") " + curso.getNome() + " - " + data);
            i++;
        }
    }

    public void mostraCurso(Curso curso) {
        System.out.println("CÓDIGO........: " + curso.getCodigoCompartilhavel());
        System.out.println("NOME..........: " + curso.getNome());
        System.out.println("DESCRIÇÃO.....: " + curso.getDescricao());
        String data = curso.getDataInicio() != null ? curso.getDataInicio().format(FORMATADOR_DATA) : "--/--/----";
        System.out.println("DATA DE INÍCIO: " + data);
        System.out.println();
        System.out.println(descricaoEstado(curso.getEstado()));
    }

    public DadosCurso leCurso() {
        String nome = lerTexto("Nome: ");
        String dataInicio = lerTexto("Data de início (dd/MM/yyyy): ");
        String descricao = lerTexto("Descrição: ");
        return new DadosCurso(nome, dataInicio, descricao);
    }

    public DadosCurso leAtualizacaoCurso(Curso cursoAtual) {
        String nome = lerTexto("Nome [" + cursoAtual.getNome() + "]: ");
        String dataAtual = cursoAtual.getDataInicio() != null ? cursoAtual.getDataInicio().format(FORMATADOR_DATA) : "";
        String dataInicio = lerTexto("Data de início [" + dataAtual + "] (dd/MM/yyyy): ");
        String descricao = lerTexto("Descrição [" + cursoAtual.getDescricao() + "]: ");
        return new DadosCurso(nome, dataInicio, descricao);
    }

    public String lerTexto(String rotulo) {
        System.out.print(rotulo);
        return scanner.nextLine().trim();
    }

    public void mensagem(String texto) {
        System.out.println(texto);
    }

    private static String descricaoEstado(int estado) {
        return switch (estado) {
            case 0 -> "Este curso está aberto para inscrições!";
            case 1 -> "Este curso está ativo sem novas inscrições.";
            case 2 -> "Este curso foi concluído.";
            case 3 -> "Este curso foi cancelado.";
            default -> "Estado do curso não reconhecido.";
        };
    }
}
