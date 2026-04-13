package modelo;

import aed3.RegistroArvoreBMais;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParNomeIDCurso implements RegistroArvoreBMais<ParNomeIDCurso> {

    private static final short TAMANHO_NOME = 120;
    private static final short TAMANHO = (short) (TAMANHO_NOME + 4);

    private String nome;
    private int idCurso;

    public ParNomeIDCurso() {
        this("", -1);
    }

    public ParNomeIDCurso(String nome) {
        this(nome, -1);
    }

    public ParNomeIDCurso(String nome, int idCurso) {
        this.nome = normalizar(nome);
        this.idCurso = idCurso;
    }

    public String getNome() {
        return nome;
    }

    public int getIdCurso() {
        return idCurso;
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        byte[] campo = new byte[TAMANHO_NOME];
        byte[] origem = this.nome.getBytes(StandardCharsets.UTF_8);
        int limite = Math.min(origem.length, TAMANHO_NOME);
        System.arraycopy(origem, 0, campo, 0, limite);
        dos.write(campo);
        dos.writeInt(this.idCurso);

        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        byte[] campo = new byte[TAMANHO_NOME];
        dis.readFully(campo);
        int fim = 0;
        while (fim < campo.length && campo[fim] != 0) {
            fim++;
        }
        this.nome = normalizar(new String(campo, 0, fim, StandardCharsets.UTF_8));
        this.idCurso = dis.readInt();
    }

    @Override
    public int compareTo(ParNomeIDCurso outro) {
        String meuNome = normalizar(this.nome);
        String nomeOutro = normalizar(outro.nome);

        if (meuNome.isEmpty()) {
            return 0;
        }

        int comparacao = meuNome.compareTo(nomeOutro);
        if (comparacao != 0) {
            return comparacao;
        }

        return this.idCurso == -1 ? 0 : this.idCurso - outro.idCurso;
    }

    @Override
    public ParNomeIDCurso clone() {
        return new ParNomeIDCurso(this.nome, this.idCurso);
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }
}
