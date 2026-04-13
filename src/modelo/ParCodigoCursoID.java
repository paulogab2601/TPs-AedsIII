package modelo;

import aed3.RegistroHashExtensivel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParCodigoCursoID implements RegistroHashExtensivel<ParCodigoCursoID> {

    private static final short TAMANHO_CODIGO = 12;
    private static final short TAMANHO = (short) (4 + TAMANHO_CODIGO);

    private String codigo;
    private int idCurso;

    public ParCodigoCursoID() {
        this("", -1);
    }

    public ParCodigoCursoID(String codigo, int idCurso) {
        this.codigo = normalizar(codigo);
        this.idCurso = idCurso;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public boolean mesmoCodigo(String outroCodigo) {
        return this.codigo.equals(normalizar(outroCodigo));
    }

    @Override
    public int hashCode() {
        return this.codigo.hashCode();
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.idCurso);
        byte[] campo = new byte[TAMANHO_CODIGO];
        byte[] origem = this.codigo.getBytes(StandardCharsets.UTF_8);
        int limite = Math.min(origem.length, TAMANHO_CODIGO);
        System.arraycopy(origem, 0, campo, 0, limite);
        dos.write(campo);

        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.idCurso = dis.readInt();
        byte[] campo = new byte[TAMANHO_CODIGO];
        dis.readFully(campo);

        int fim = 0;
        while (fim < campo.length && campo[fim] != 0) {
            fim++;
        }
        this.codigo = normalizar(new String(campo, 0, fim, StandardCharsets.UTF_8));
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
