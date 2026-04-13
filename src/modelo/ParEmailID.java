package modelo;

import aed3.RegistroHashExtensivel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParEmailID implements RegistroHashExtensivel<ParEmailID> {

    private static final short TAMANHO_EMAIL = 120;
    private static final short TAMANHO = (short) (4 + TAMANHO_EMAIL);

    private String email;
    private int id;

    public ParEmailID() {
        this("", -1);
    }

    public ParEmailID(String email, int id) {
        this.email = normalizar(email);
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }

    public boolean mesmoEmail(String outroEmail) {
        return this.email.equals(normalizar(outroEmail));
    }

    @Override
    public int hashCode() {
        return this.email.hashCode();
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.id);
        byte[] campo = new byte[TAMANHO_EMAIL];
        byte[] origem = this.email.getBytes(StandardCharsets.UTF_8);
        int limite = Math.min(origem.length, TAMANHO_EMAIL);
        System.arraycopy(origem, 0, campo, 0, limite);
        dos.write(campo);

        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        byte[] campo = new byte[TAMANHO_EMAIL];
        dis.readFully(campo);

        int fim = 0;
        while (fim < campo.length && campo[fim] != 0) {
            fim++;
        }
        this.email = normalizar(new String(campo, 0, fim, StandardCharsets.UTF_8));
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }
}
