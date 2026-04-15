package modelo;

import java.io.File;
import aed3.Arquivo;
import aed3.HashExtensivel;

public class ArquivoUsuario extends Arquivo<Usuario> {

    private final HashExtensivel<ParEmailID> indiceEmail;

    public ArquivoUsuario() throws Exception {
        super("usuarios", Usuario.class.getConstructor());
        indiceEmail = new HashExtensivel<>(
                ParEmailID.class.getConstructor(),
                4,
                "." + File.separator + "dados" + File.separator + "usuarios" + File.separator + "usuarios_email.d.db",
                "." + File.separator + "dados" + File.separator + "usuarios" + File.separator + "usuarios_email.c.db"
        );
    }

    public Usuario readPorEmail(String email) throws Exception {
        ParEmailID par = lerParEmail(email);
        if (par == null) {
            return null;
        }
        return super.read(par.getId());
    }

    @Override
    public int create(Usuario usuario) throws Exception {
        if (usuario == null) {
            throw new Exception("Usuário inválido.");
        }

        String emailNormalizado = normalizarEmail(usuario.getEmail());
        if (emailNormalizado.isEmpty()) {
            throw new Exception("E-mail inválido.");
        }

        if (lerParEmail(emailNormalizado) != null) {
            throw new Exception("E-mail já cadastrado.");
        }

        usuario.setEmail(emailNormalizado);
        int id = super.create(usuario);
        indiceEmail.create(new ParEmailID(emailNormalizado, id));
        return id;
    }

    @Override
    public boolean update(Usuario novoUsuario) throws Exception {
        if (novoUsuario == null) {
            return false;
        }

        Usuario usuarioAntigo = super.read(novoUsuario.getId());
        if (usuarioAntigo == null) {
            return false;
        }

        String emailAntigo = normalizarEmail(usuarioAntigo.getEmail());
        String emailNovo = normalizarEmail(novoUsuario.getEmail());
        if (emailNovo.isEmpty()) {
            throw new Exception("E-mail inválido.");
        }

        if (!emailAntigo.equals(emailNovo)) {
            ParEmailID existente = lerParEmail(emailNovo);
            if (existente != null && existente.getId() != novoUsuario.getId()) {
                throw new Exception("E-mail já cadastrado.");
            }
        }

        novoUsuario.setEmail(emailNovo);
        boolean atualizado = super.update(novoUsuario);
        if (!atualizado) {
            return false;
        }

        if (!emailAntigo.equals(emailNovo)) {
            indiceEmail.delete(emailAntigo.hashCode());
            indiceEmail.create(new ParEmailID(emailNovo, novoUsuario.getId()));
        } else {
            indiceEmail.update(new ParEmailID(emailNovo, novoUsuario.getId()));
        }

        return true;
    }

    @Override
    public boolean delete(int id) throws Exception {
        Usuario usuario = super.read(id);
        if (usuario == null) {
            return false;
        }

        boolean excluiu = super.delete(id);
        if (excluiu) {
            indiceEmail.delete(normalizarEmail(usuario.getEmail()).hashCode());
        }
        return excluiu;
    }

    @Override
    public void close() throws Exception {
        super.close();
        indiceEmail.close();
    }

    private ParEmailID lerParEmail(String email) throws Exception {
        String emailNormalizado = normalizarEmail(email);
        if (emailNormalizado.isEmpty()) {
            return null;
        }

        ParEmailID par = indiceEmail.read(emailNormalizado.hashCode());
        if (par != null && par.mesmoEmail(emailNormalizado)) {
            return par;
        }
        return null;
    }

    private static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
