package modelo;

import aed3.Registro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class Curso implements Registro {

    private int id;
    private String nome;
    private LocalDate dataInicio;
    private String descricao;
    private String codigoCompartilhavel;
    private int estado;
    private int idUsuario;

    public Curso() {
        this(-1, "", LocalDate.now(), "", "", 0, -1);
    }

    public Curso(String nome, LocalDate dataInicio, String descricao, int idUsuario) {
        this(-1, nome, dataInicio, descricao, "", 0, idUsuario);
    }

    public Curso(int id, String nome, LocalDate dataInicio, String descricao, String codigoCompartilhavel, int estado, int idUsuario) {
        this.id = id;
        this.nome = nome != null ? nome : "";
        this.dataInicio = dataInicio;
        this.descricao = descricao != null ? descricao : "";
        this.codigoCompartilhavel = codigoCompartilhavel != null ? codigoCompartilhavel : "";
        this.estado = estado;
        this.idUsuario = idUsuario;
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

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao != null ? descricao : "";
    }

    public String getCodigoCompartilhavel() {
        return codigoCompartilhavel;
    }

    public void setCodigoCompartilhavel(String codigoCompartilhavel) {
        this.codigoCompartilhavel = codigoCompartilhavel != null ? codigoCompartilhavel : "";
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
        dos.writeLong(this.dataInicio != null ? this.dataInicio.toEpochDay() : Long.MIN_VALUE);
        dos.writeUTF(this.descricao);
        dos.writeUTF(this.codigoCompartilhavel);
        dos.writeInt(this.estado);
        dos.writeInt(this.idUsuario);

        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.nome = dis.readUTF();
        long epochDay = dis.readLong();
        this.dataInicio = epochDay == Long.MIN_VALUE ? null : LocalDate.ofEpochDay(epochDay);
        this.descricao = dis.readUTF();
        this.codigoCompartilhavel = dis.readUTF();
        this.estado = dis.readInt();
        this.idUsuario = dis.readInt();
    }
}
