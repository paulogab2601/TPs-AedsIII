package modelo;

import aed3.RegistroArvoreBMais;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParIntInt implements RegistroArvoreBMais<ParIntInt> {

    private int num1;
    private int num2;
    private final short TAMANHO = 8;

    public ParIntInt() {
        this(-1, -1);
    }

    public ParIntInt(int n1) {
        this(n1, -1);
    }

    public ParIntInt(int n1, int n2) {
        this.num1 = n1;
        this.num2 = n2;
    }

    public int getNum1() {
        return num1;
    }

    public int getNum2() {
        return num2;
    }

    @Override
    public ParIntInt clone() {
        return new ParIntInt(this.num1, this.num2);
    }

    @Override
    public short size() {
        return this.TAMANHO;
    }

    @Override
    public int compareTo(ParIntInt a) {
        if (this.num1 != a.num1) {
            return this.num1 - a.num1;
        } else {
            return this.num2 == -1 ? 0 : this.num2 - a.num2;
        }
    }

    @Override
    public String toString() {
        return String.format("%3d", this.num1) + ";" + String.format("%-3d", this.num2);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.num1);
        dos.writeInt(this.num2);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.num1 = dis.readInt();
        this.num2 = dis.readInt();
    }
}
