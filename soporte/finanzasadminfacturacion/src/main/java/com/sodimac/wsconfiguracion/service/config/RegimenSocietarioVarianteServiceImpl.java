package com.sodimac.wsconfiguracion.service.config;

import java.io.Console;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.RegimenDeCapital;
import com.sodimac.wsconfiguracion.dto.RegimenDeCapitalDto;
import com.sodimac.wsconfiguracion.entity.config.RegimenSocietarioVarianteEntity;
import com.sodimac.wsconfiguracion.repository.config.RegimenSocietarioVarianteRepository;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

@Service("regimenSocietarioVarianteServiceImplConfig")
public class RegimenSocietarioVarianteServiceImpl implements RegimenSocietarioVarianteService {

	
	@Autowired
	@Qualifier("regimenSocietarioVarianteRepositoryConfig")
	private RegimenSocietarioVarianteRepository regimenSocietarioVarianteRepository;
	
	@Override
	public ClientResponseTYPE<RegimenDeCapitalDto> validaRazonSocial(String razonSocial) {
		//String razonSocial = "Mi patito.com Sociedad AnÓnima ";
		RegimenDeCapitalDto regimenDeCapitalDto = new RegimenDeCapitalDto();
		ClientResponseTYPE<RegimenDeCapitalDto> regimenDeCapital = new ClientResponseTYPE<RegimenDeCapitalDto>(new RegimenDeCapitalDto());
		String razonSocialNormalizada = normalizaTexto(razonSocial);
		List<String> regimenSocitarios = obtieneRegimenSocietario();
		
        List<String> sociedadesContenidas = regimenSocitarios
                .stream()
                .filter(regimenSocitarioItm -> razonSocialNormalizada.contains(regimenSocitarioItm) && validaRegimenAlFinal(razonSocialNormalizada, regimenSocitarioItm))
                .collect(Collectors.toList());

        if(sociedadesContenidas.size() == 0) {
        	regimenDeCapitalDto.setEstatus(1);
        	regimenDeCapitalDto.setEstatusDescripcion("Razón Social Valida");
        	
        } else {
        	regimenDeCapitalDto.setEstatus(2);
        	regimenDeCapitalDto.setEstatusDescripcion("Razón Social Invalida");
        	UtilsApi.setRespuesta(regimenDeCapital.getRespuesta(), ECodigo.RazonSocialInvalida);
        }
        //regimenDeCapital.getData().setRegimenDeCapital(regimenDeCapitalDto);
        regimenDeCapital.setData(regimenDeCapitalDto);
		return regimenDeCapital;
	}
	
	private boolean validaRegimenAlFinal(String razonSocialNormalizada, String regimenSocitario) {
		int longRSN = razonSocialNormalizada.length();
		int longRS = regimenSocitario.length();
		
		String lastDigits = "";   //substring containing last characters
		if (longRSN > longRS) 
		{
			lastDigits = razonSocialNormalizada.substring(longRSN - longRS);
		} 
		else
		{
			lastDigits = razonSocialNormalizada;
		}
		
		return (lastDigits.equals(regimenSocitario));
	}
	
	private List<String> obtieneRegimenSocietario(){
		List<RegimenSocietarioVarianteEntity> regimenSocitarios = regimenSocietarioVarianteRepository.findAll();
		List<String> result = regimenSocitarios.stream().map(temp -> {
			String textoNormalizado = normalizaTexto(temp.getSociedadNombre());
            return textoNormalizado;
        }).collect(Collectors.toList());
		
		return result;
	}
	
	private String normalizaTexto(String sociedad) {
		//Elminia acentos
		String string = Normalizer.normalize(sociedad, Normalizer.Form.NFD);
		string = string.replaceAll("\\p{M}", "");
		
		//Quita espacios en blanco y convierte a mayusculas
		string = string.replaceAll(" ", "").toUpperCase();
		
		return string;
	}
}
