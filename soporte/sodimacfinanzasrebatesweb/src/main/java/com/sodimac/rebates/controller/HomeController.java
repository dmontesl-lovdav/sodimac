package com.sodimac.rebates.controller;

import static com.sodimac.rebates.util.Constants.SET_CHARACTERS_RECOVER;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.service.IMailerService;
import com.sodimac.rebates.service.IUsuarioService;
import com.sodimac.rebates.util.EmailBuilder;
import com.sodimac.rebates.util.Mail;
import com.sodimac.rebates.util.RandomString;

@Controller
public class HomeController extends BaseController {

	@Value("${sodimac.rebates.version}")
	private String version;

	@Autowired
	private IUsuarioService serviceUsuario;

	@Autowired
	private IMailerService serviceMail;
	
	@GetMapping("/")
	public String index(Model model, HttpServletRequest request, HttpSession session) {
		model.addAttribute("version", version);
	    Optional.<Object>ofNullable(session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION")).ifPresent(exc -> {
	          Exception exception = (Exception)exc;
	          model.addAttribute("error", exception.getMessage());
	        });
	    Object obj = session.getAttribute("SPRING_SECURITY_CONTEXT");
	    if (obj == null) {
	      return "login.html";
	    } 
	    
	    return "redirect:/inicio";
	}
	
	
	@GetMapping("/inicio")
	public String inicio(@Nullable @ModelAttribute("section") String section, @Nullable @ModelAttribute("exception") String exception, Model model, HttpServletRequest request) {	
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		Set<String> roles = sesion.getPermisos() != null && !sesion.getPermisos().isEmpty() ? sesion.getPermisos().stream()
		     .map(r -> r.toString()).collect(Collectors.toSet()) : Collections.emptySet();

		System.out.println(roles);
		getModelAttributes(model, sesion);
		
		if (Strings.isNotBlank(section)) {
			model.addAttribute("section", section);
			model.addAttribute("exception", exception);
		} else 
			model.addAttribute("section", "start");

		return "/inicio";
	}

	@GetMapping("/recover")
	public String loginRecoverPass() {

		return "fragments/login:: formRecover";
	}

	@ResponseBody
	@PostMapping("/recover")
	public Usuario loginSendEmail(@RequestBody Usuario usuario, Model model) {

		Usuario response = new Usuario();
		Usuario user = new Usuario();

		try {

			user = serviceUsuario.getUserEmail(usuario.getUsuario());

		} catch (Exception ex) {
			response.setTitle("Error");
			response.setMessage("No se pudo acceder al Sistema, verifique conexiones");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		if (user != null) {

			if (!user.isActivo()) {
				response.setTitle("Error");
				response.setMessage("La cuenta se encuentra inactiva");
				response.setTypeMessage(3);
				response.setCode(false);

				return response;
			}

			RandomString passwordGenerado = new RandomString(8, new SecureRandom(), SET_CHARACTERS_RECOVER);
			boolean sendEmail;
			String password = passwordGenerado.nextString();
			Date date = new Date();
			user.setPass(password);
			user.setCambioPassword(true);
			user.setFechaActualizacion(date);

			boolean guardado = serviceUsuario.save(user);

			if (!guardado) {
				response.setTitle("Error en Base de Datos");
				response.setMessage("No se pudo establecer la conexión");
				response.setTypeMessage(2);
				response.setCode(false);

				return response;
			}

			// obtenerConfiguracionBctPorParametro("Mail.From")
			// Método para obtener desde BD y ponerlo dentro del FROM fabian140290@gmail.com
			// | Notificaciones.TI.Sodimac.MX@gmail.com
			Mail mail = new EmailBuilder().From("notificaciones.ti_sod@sodimac.com.mx").To(user.getUsuario())
					.Template("cambioPass_rebatesWebMail.html")
					.AddContext("Usuario", user.getNombre() + " " + user.getApellidoPaterno())
					.AddContext("Contrasenia", password).Subject("Cambiar contraseña").createMail();

			try {

				sendEmail = serviceMail.sendMail(mail, true);

			} catch (Exception ex) {
				response.setTitle("Error");
				response.setMessage("No se pudo enviar el Correo electrónico, intente más tarde");
				response.setTypeMessage(3);
				response.setCode(false);
				System.out.println(ex.getMessage());

				return response;
			}

			if (!sendEmail) {

				// Generic
				response.setTitle("Error");
				response.setMessage("No se pudo enviar el Correo electrónico, intente más tarde");
				response.setTypeMessage(3);
				response.setCode(false);

				return response;
			}

			// Usuario
			response.setId(user.getId());
			response.setUsuario(user.getUsuario());

			// Generic
			response.setTitle("OK");
			response.setMessage(
					"Te hemos enviado un correo electrónico con la nueva contraseña temporal. Revisa tu correo (es posible que se encuentre en carpeta 'Otros' o 'Spam') y sigue las instrucciones");
			response.setTypeMessage(1);
			response.setCode(true);

			return response;
		}

		// Generic
		response.setTitle("Advertencia");
		response.setMessage("No existe un Usuario con ese correo electrónico");
		response.setTypeMessage(2);
		response.setCode(false);

		return response;
	}

	@PostMapping("/change")
	public String loginChangePass(@RequestBody Usuario usuario, Model model) {

		model.addAttribute("passtemp", usuario.getPass());
		model.addAttribute("idUser", usuario.getId());
		model.addAttribute("email", usuario.getUsuario());

		return "fragments/login:: formChange";
	}

	@ResponseBody
	@PostMapping("/update")
	public Usuario loginUpdatePass(@RequestBody Usuario usuario, Model model, SessionStatus status) {

		Usuario response = new Usuario();
		Usuario user = new Usuario();

		try {

			user = serviceUsuario.getById(usuario.getId(), usuario.getPass());

		} catch (Exception ex) {
			response.setTitle("Error");
			response.setMessage("No se pudo acceder al Sistema, verifique conexiones");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		if (user != null) {

			if (!user.isActivo()) {
				response.setTitle("Cuenta inactiva");
				response.setMessage("La cuenta con la que desea ingresar se encuentra suspendida");
				response.setTypeMessage(3);
				response.setCode(false);

				return response;
			}

			Date date = new Date();
			user.setPass(usuario.getNewPassword());
			user.setCambioPassword(false);
			user.setFechaActualizacion(date);

			boolean guardado = serviceUsuario.save(user);

			if (!guardado) {
				response.setTitle("Error en Base de Datos");
				response.setMessage("No se pudo establecer la conexión, intente de nuevo");
				response.setTypeMessage(2);
				response.setCode(false);

				return response;
			}

			// Generic
			response.setTitle("OK");
			response.setMessage("Contraseña actualizada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
			status.setComplete();

			return response;
		}

		// Generic
		response.setTitle("Advertencia");
		response.setMessage("Contraseña temporal incorrecta, favor de validar");
		response.setTypeMessage(2);
		response.setCode(false);

		return response;
	}

	@GetMapping("/logout")
	public String logout(Model model, HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		session.setAttribute("validSession", null);
		session.setAttribute("sesion", null);
		session.setAttribute("paginas", null);
		session.invalidate();
		
		model.addAttribute("sesion", null);

		return "redirect:/";
	}
	
	@GetMapping("/401")
	public String unauthorized() {
		return  "/error/401";
	}
	
	@GetMapping("/403")
	public String forbidden(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("section", "forbidden");
		return "redirect:/inicio";
	}
	
	@GetMapping("/404")
	public String notFound(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("section", "notFound");
		return "redirect:/inicio";
	}
	
	@GetMapping("/500")
	public String internalError(@RequestParam(required = false) String exception, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("section", "internalError");
		redirectAttributes.addFlashAttribute("exception", exception);
		return "redirect:/inicio";
	}

}
