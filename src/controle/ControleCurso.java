package controle;

import modelo.ArquivoCurso;
import modelo.Curso;
import modelo.Usuario;
import visao.VisaoCurso;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class ControleCurso {

    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ArquivoCurso arquivoCurso;
    private final VisaoCurso visaoCurso;

    public ControleCurso(ArquivoCurso arquivoCurso, Scanner scanner) {
        this.arquivoCurso = arquivoCurso;
        this.visaoCurso = new VisaoCurso(scanner);
    }

    public void menuMeusCursos(Usuario usuarioLogado) throws Exception {
        while (true) {
            ArrayList<Curso> cursos = arquivoCurso.listarCursosDoUsuarioOrdenadosPorNome(usuarioLogado.getId());

            visaoCurso.exibirCabecalho("Início > Meus cursos");
            visaoCurso.mostrarListaCursos(cursos);
            System.out.println();
            System.out.println("(A) Novo curso");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.println();

            String opcao = visaoCurso.lerTexto("Opção: ").toUpperCase();
            System.out.println();

            if ("A".equals(opcao)) {
                criarCurso(usuarioLogado);
            } else if ("R".equals(opcao)) {
                return;
            } else if (opcao.matches("\\d+")) {
                int indice = Integer.parseInt(opcao);
                if (indice >= 1 && indice <= cursos.size()) {
                    menuCurso(cursos.get(indice - 1));
                } else {
                    visaoCurso.mensagem("Curso inválido.\n");
                }
            } else {
                visaoCurso.mensagem("Opção inválida.\n");
            }
        }
    }

    private void criarCurso(Usuario usuarioLogado) throws Exception {
        visaoCurso.exibirCabecalho("Início > Meus cursos > Novo curso");
        VisaoCurso.DadosCurso dados = visaoCurso.leCurso();

        if (dados.nome.isBlank() || dados.dataInicio.isBlank() || dados.descricao.isBlank()) {
            visaoCurso.mensagem("\nTodos os campos do curso são obrigatórios.\n");
            return;
        }

        LocalDate dataInicio = converterData(dados.dataInicio);
        if (dataInicio == null) {
            visaoCurso.mensagem("\nData inválida. Use o formato dd/MM/yyyy.\n");
            return;
        }

        Curso curso = new Curso();
        curso.setNome(dados.nome);
        curso.setDescricao(dados.descricao);
        curso.setDataInicio(dataInicio);
        curso.setEstado(0);
        curso.setIdUsuario(usuarioLogado.getId());

        try {
            int id = arquivoCurso.create(curso);
            Curso salvo = arquivoCurso.read(id);
            visaoCurso.mensagem("\nCurso criado com sucesso! Código compartilhável: " + salvo.getCodigoCompartilhavel() + "\n");
        } catch (Exception e) {
            visaoCurso.mensagem("\nFalha ao criar curso: " + e.getMessage() + "\n");
        }
    }

    private void menuCurso(Curso cursoBase) throws Exception {
        while (true) {
            Curso curso = arquivoCurso.read(cursoBase.getId());
            if (curso == null) {
                visaoCurso.mensagem("Curso não existe mais.\n");
                return;
            }

            visaoCurso.exibirCabecalho("Início > Meus Cursos > " + curso.getNome());
            visaoCurso.mostraCurso(curso);
            System.out.println();
            System.out.println("(A) Gerenciar inscritos no curso");
            System.out.println("(B) Corrigir dados do curso");
            System.out.println("(C) Encerrar inscrições");
            System.out.println("(D) Concluir curso");
            System.out.println("(E) Cancelar curso");
            System.out.println();
            System.out.println("(R) Retornar ao menu anterior");
            System.out.println();

            String opcao = visaoCurso.lerTexto("Opção: ").toUpperCase();
            System.out.println();

            switch (opcao) {
                case "A" -> visaoCurso.mensagem("Funcionalidade de inscritos será implementada no TP2.\n");
                case "B" -> atualizarCurso(curso);
                case "C" -> encerrarInscricoes(curso);
                case "D" -> concluirCurso(curso);
                case "E" -> {
                    if (cancelarCurso(curso)) {
                        return;
                    }
                }
                case "R" -> {
                    return;
                }
                default -> visaoCurso.mensagem("Opção inválida.\n");
            }
        }
    }

    private void atualizarCurso(Curso atual) throws Exception {
        VisaoCurso.DadosCurso dados = visaoCurso.leAtualizacaoCurso(atual);

        Curso atualizado = new Curso();
        atualizado.setId(atual.getId());
        atualizado.setIdUsuario(atual.getIdUsuario());
        atualizado.setCodigoCompartilhavel(atual.getCodigoCompartilhavel());
        atualizado.setEstado(atual.getEstado());
        atualizado.setNome(dados.nome.isBlank() ? atual.getNome() : dados.nome);
        atualizado.setDescricao(dados.descricao.isBlank() ? atual.getDescricao() : dados.descricao);

        if (dados.dataInicio.isBlank()) {
            atualizado.setDataInicio(atual.getDataInicio());
        } else {
            LocalDate novaData = converterData(dados.dataInicio);
            if (novaData == null) {
                visaoCurso.mensagem("\nData inválida. Use o formato dd/MM/yyyy.\n");
                return;
            }
            atualizado.setDataInicio(novaData);
        }

        boolean ok = arquivoCurso.update(atualizado);
        if (ok) {
            visaoCurso.mensagem("\nCurso atualizado com sucesso.\n");
        } else {
            visaoCurso.mensagem("\nNão foi possível atualizar o curso.\n");
        }
    }

    private void encerrarInscricoes(Curso curso) throws Exception {
        if (curso.getEstado() != 0) {
            visaoCurso.mensagem("Não é possível encerrar inscrições neste estado.\n");
            return;
        }

        boolean ok = arquivoCurso.alterarEstado(curso.getId(), 1);
        if (ok) {
            visaoCurso.mensagem("Inscrições encerradas.\n");
        } else {
            visaoCurso.mensagem("Falha ao encerrar inscrições.\n");
        }
    }

    private void concluirCurso(Curso curso) throws Exception {
        if (curso.getEstado() != 0 && curso.getEstado() != 1) {
            visaoCurso.mensagem("Apenas cursos ativos podem ser concluídos.\n");
            return;
        }

        boolean ok = arquivoCurso.alterarEstado(curso.getId(), 2);
        if (ok) {
            visaoCurso.mensagem("Curso concluído com sucesso.\n");
        } else {
            visaoCurso.mensagem("Falha ao concluir o curso.\n");
        }
    }

    private boolean cancelarCurso(Curso curso) throws Exception {
        boolean ok = arquivoCurso.cancelarCurso(curso.getId());
        if (ok) {
            visaoCurso.mensagem("Curso cancelado e removido com sucesso.\n");
            return true;
        }

        visaoCurso.mensagem("Falha ao cancelar curso.\n");
        return false;
    }

    private LocalDate converterData(String texto) {
        try {
            return LocalDate.parse(texto, FORMATADOR_DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
