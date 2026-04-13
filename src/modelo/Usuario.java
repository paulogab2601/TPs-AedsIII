package modelo;

import aed3.Registro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Usuario implements Registro {

    private int id;
    private String nome;
    private String email;
    private byte[] hashSenha;
    private String perguntaSecreta;
    private byte[] hashRespostaSecreta;

    public Usuario() {
        this(-1, "", "", new byte[0], "", new byte[0]);
    }

    public Usuario(String nome, String email, byte[] hashSenha, String perguntaSecreta, byte[] hashRespostaSecreta) {
        this(-1, nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta);
    }

    public Usuario(int id, String nome, String email, byte[] hashSenha, String perguntaSecreta, byte[] hashRespostaSecreta) {
        this.id = id;
        this.nome = nome != null ? nome : "";
        this.email = email != null ? email : "";
        this.hashSenha = hashSenha != null ? hashSenha : new byte[0];
        this.perguntaSecreta = perguntaSecreta != null ? perguntaSecreta : "";
        this.hashRespostaSecreta = hashRespostaSecreta != null ? hashRespostaSecreta : new byte[0];
    }

    @Override
    public void setId(int i) {
        this.id = i;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome != null ? nome : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public byte[] getHashSenha() {
        return hashSenha;
    }

    public void setHashSenha(byte[] hashSenha) {
        this.hashSenha = hashSenha != null ? hashSenha : new byte[0];
    }

    public String getPerguntaSecreta() {
        return perguntaSecreta;
    }

    public void setPerguntaSecreta(String perguntaSecreta) {
        this.perguntaSecreta = perguntaSecreta != null ? perguntaSecreta : "";
    }

    public byte[] getHashRespostaSecreta() {
        return hashRespostaSecreta;
    }

    public void setHashRespostaSecreta(byte[] hashRespostaSecreta) {
        this.hashRespostaSecreta = hashRespostaSecreta != null ? hashRespostaSecreta : new byte[0];
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.email);

        dos.writeInt(this.hashSenha.length);
        dos.write(this.hashSenha);

        dos.writeUTF(this.perguntaSecreta);

        dos.writeInt(this.hashRespostaSecreta.length);
        dos.write(this.hashRespostaSecreta);

        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.nome = dis.readUTF();
        this.email = dis.readUTF();

        int tamHashSenha = dis.readInt();
        this.hashSenha = new byte[tamHashSenha];
        dis.readFully(this.hashSenha);

        this.perguntaSecreta = dis.readUTF();

        int tamHashResposta = dis.readInt();
        this.hashRespostaSecreta = new byte[tamHashResposta];
        dis.readFully(this.hashRespostaSecreta);
    }
}
