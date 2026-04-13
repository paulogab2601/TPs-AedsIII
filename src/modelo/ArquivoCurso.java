package modelo;

import aed3.Arquivo;
import aed3.ArvoreBMais;
import aed3.HashExtensivel;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ArquivoCurso extends Arquivo<Curso> {

    private static final String ALFABETO = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TAMANHO_CODIGO = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HashExtensivel<ParCodigoCursoID> indiceCodigo;
    private final ArvoreBMais<ParIntInt> indiceUsuarioCurso;
    private final ArvoreBMais<ParNomeIDCurso> indiceNomeCurso;

    public ArquivoCurso() throws Exception {
        super("cursos", Curso.class.getConstructor());

        indiceCodigo = new HashExtensivel<>(
                ParCodigoCursoID.class.getConstructor(),
                4,
                ".\\dados\\cursos\\cursos_codigo.d.db",
                ".\\dados\\cursos\\cursos_codigo.c.db"
        );

        indiceUsuarioCurso = new ArvoreBMais<>(
                ParIntInt.class.getConstructor(),
                5,
                ".\\dados\\cursos\\cursos_usuario_curso.idx.db"
        );

        indiceNomeCurso = new ArvoreBMais<>(
                ParNomeIDCurso.class.getConstructor(),
                5,
                ".\\dados\\cursos\\cursos_nome.idx.db"
        );
    }

    public Curso readPorCodigo(String codigo) throws Exception {
        ParCodigoCursoID par = lerParCodigo(codigo);
        if (par == null) {
            return null;
        }
        return super.read(par.getIdCurso());
    }

    @Override
    public int create(Curso curso) throws Exception {
        if (curso == null) {
            throw new Exception("Curso inválido.");
        }

        String nomeNormalizado = normalizarNome(curso.getNome());
        if (nomeNormalizado.isEmpty()) {
            throw new Exception("Nome do curso inválido.");
        }
        curso.setNome(nomeNormalizado);

        String codigo = normalizarCodigo(curso.getCodigoCompartilhavel());
        if (codigo.isEmpty()) {
            codigo = gerarCodigoCompartilhavelUnico();
        } else {
            ParCodigoCursoID existente = lerParCodigo(codigo);
            if (existente != null) {
                throw new Exception("Código compartilhável já em uso.");
            }
        }
        curso.setCodigoCompartilhavel(codigo);

        int id = super.create(curso);
        indiceCodigo.create(new ParCodigoCursoID(codigo, id));
        indiceUsuarioCurso.create(new ParIntInt(curso.getIdUsuario(), id));
        indiceNomeCurso.create(new ParNomeIDCurso(curso.getNome(), id));
        return id;
    }

    @Override
    public boolean update(Curso novoCurso) throws Exception {
        if (novoCurso == null) {
            return false;
        }

        Curso antigo = super.read(novoCurso.getId());
        if (antigo == null) {
            return false;
        }

        String nomeNovo = normalizarNome(novoCurso.getNome());
        if (nomeNovo.isEmpty()) {
            throw new Exception("Nome do curso inválido.");
        }
        novoCurso.setNome(nomeNovo);

        String codigoAntigo = normalizarCodigo(antigo.getCodigoCompartilhavel());
        String codigoNovo = normalizarCodigo(novoCurso.getCodigoCompartilhavel());
        if (codigoNovo.isEmpty()) {
            codigoNovo = codigoAntigo;
        }
        if (!codigoAntigo.equals(codigoNovo)) {
            ParCodigoCursoID existente = lerParCodigo(codigoNovo);
            if (existente != null && existente.getIdCurso() != novoCurso.getId()) {
                throw new Exception("Código compartilhável já em uso.");
            }
        }
        novoCurso.setCodigoCompartilhavel(codigoNovo);

        boolean atualizou = super.update(novoCurso);
        if (!atualizou) {
            return false;
        }

        if (antigo.getIdUsuario() != novoCurso.getIdUsuario()) {
            indiceUsuarioCurso.delete(new ParIntInt(antigo.getIdUsuario(), antigo.getId()));
            indiceUsuarioCurso.create(new ParIntInt(novoCurso.getIdUsuario(), novoCurso.getId()));
        }

        if (!normalizarNome(antigo.getNome()).equals(normalizarNome(novoCurso.getNome()))) {
            indiceNomeCurso.delete(new ParNomeIDCurso(antigo.getNome(), antigo.getId()));
            indiceNomeCurso.create(new ParNomeIDCurso(novoCurso.getNome(), novoCurso.getId()));
        }

        if (!codigoAntigo.equals(codigoNovo)) {
            indiceCodigo.delete(codigoAntigo.hashCode());
            indiceCodigo.create(new ParCodigoCursoID(codigoNovo, novoCurso.getId()));
        } else {
            indiceCodigo.update(new ParCodigoCursoID(codigoNovo, novoCurso.getId()));
        }

        return true;
    }

    @Override
    public boolean delete(int id) throws Exception {
        Curso curso = super.read(id);
        if (curso == null) {
            return false;
        }

        boolean excluiu = super.delete(id);
        if (excluiu) {
            indiceCodigo.delete(normalizarCodigo(curso.getCodigoCompartilhavel()).hashCode());
            indiceUsuarioCurso.delete(new ParIntInt(curso.getIdUsuario(), curso.getId()));
            indiceNomeCurso.delete(new ParNomeIDCurso(curso.getNome(), curso.getId()));
        }
        return excluiu;
    }

    public boolean alterarEstado(int idCurso, int novoEstado) throws Exception {
        Curso curso = super.read(idCurso);
        if (curso == null) {
            return false;
        }
        curso.setEstado(novoEstado);
        return update(curso);
    }

    public boolean cancelarCurso(int idCurso) throws Exception {
        return delete(idCurso);
    }

    public ArrayList<Integer> listarIdsCursosDoUsuario(int idUsuario) throws Exception {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<ParIntInt> pares = indiceUsuarioCurso.read(new ParIntInt(idUsuario, -1));
        if (pares == null) {
            return ids;
        }

        for (ParIntInt par : pares) {
            ids.add(par.getNum2());
        }
        return ids;
    }

    public ArrayList<Curso> listarCursosDoUsuarioOrdenadosPorNome(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = new ArrayList<>();
        ArrayList<Integer> ids = listarIdsCursosDoUsuario(idUsuario);
        for (Integer id : ids) {
            Curso curso = super.read(id);
            if (curso != null) {
                cursos.add(curso);
            }
        }

        Collections.sort(cursos, Comparator
                .comparing((Curso c) -> normalizarNome(c.getNome()))
                .thenComparing(Curso::getDataInicio, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Curso::getId));
        return cursos;
    }

    public boolean usuarioPossuiCursoAtivo(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = listarCursosDoUsuarioOrdenadosPorNome(idUsuario);
        for (Curso curso : cursos) {
            if (curso.getEstado() == 0 || curso.getEstado() == 1) {
                return true;
            }
        }
        return false;
    }

    public void excluirTodosCursosDoUsuario(int idUsuario) throws Exception {
        ArrayList<Integer> ids = listarIdsCursosDoUsuario(idUsuario);
        for (Integer id : ids) {
            delete(id);
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        indiceCodigo.close();
    }

    private ParCodigoCursoID lerParCodigo(String codigo) throws Exception {
        String codigoNormalizado = normalizarCodigo(codigo);
        if (codigoNormalizado.isEmpty()) {
            return null;
        }

        ParCodigoCursoID par = indiceCodigo.read(codigoNormalizado.hashCode());
        if (par != null && par.mesmoCodigo(codigoNormalizado)) {
            return par;
        }
        return null;
    }

    private String gerarCodigoCompartilhavelUnico() throws Exception {
        String codigo;
        do {
            codigo = gerarCodigo(TAMANHO_CODIGO);
        } while (lerParCodigo(codigo) != null);
        return codigo;
    }

    private static String gerarCodigo(int tamanho) {
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    private static String normalizarNome(String nome) {
        return nome == null ? "" : nome.trim();
    }

    private static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim();
    }
}
