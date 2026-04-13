package com.sodimac.rebates.service;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.repository.UsuarioRepository;

@Service
public class UsuarioService implements IUsuarioService {

	@Autowired
	private UsuarioRepository userRepo;

	@Autowired
	private ISeguridadService seguridadService;

	@Override
	public Usuario getById(Integer id, String oldPassword) {

		Optional<Usuario> optional = userRepo.findById(id);
		String password = "";

		if (optional.isPresent()) {

			try {

				password = seguridadService.encriptar(oldPassword);

			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
					| UnsupportedEncodingException e) {

				// TODO: añadir a bitacora
				e.printStackTrace();
				System.out.println(e.getMessage());

				return null;
			}

			return userRepo.findByUsuarioAndPass(optional.get().getUsuario(), password);
		}

		return null;
	}

	public Usuario getUser(String usuario, String pass) {

		Usuario response = new Usuario();
		String password = "";

		try {
			password = seguridadService.encriptar(pass);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
				| UnsupportedEncodingException e) {

			// TODO: añadir a bitacora
			e.printStackTrace();
			System.out.println(e.getMessage());

			return null;
		}

		response = userRepo.findByUsuarioAndPass(usuario, password);

		if (response != null) {

			String passDecrypt = "";

			try {

				passDecrypt = seguridadService.desencriptar(password);

			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
					| UnsupportedEncodingException e) {

				// TODO: añadir a bitacora
				e.printStackTrace();
				System.out.println(e.getMessage());

				return null;
			}

			response.setPass(passDecrypt);
		}

		return response;
	}

	@Override
	public Usuario getUserEmail(String usuario) {

		return userRepo.findByUsuario(usuario);
	}

	@Override
	public boolean save(Usuario usuario) {

		String pswCifrado = "";

		try {

			pswCifrado = seguridadService.encriptar(usuario.getPass());

		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
				| UnsupportedEncodingException e) {

			// TODO: añadir a bitacora
			e.printStackTrace();
			System.out.println(e.getMessage());

			return false;
		}

		usuario.setPass(pswCifrado);

		try {

			userRepo.save(usuario);

		} catch (Exception ex) {

			// TODO: añadir a bitacora
			ex.printStackTrace();
			System.out.println(ex.getMessage());

			return false;
		}

		return true;
	}

	public static void main(String[] args) throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
		ISeguridadService seguridadService = new SeguridadService();
		System.out.println(seguridadService.desencriptar("MDAwMDAwMDAwMDAwMDAwMO7NDeLMkz1dQCWSGHi3wUU="));
	}
}
