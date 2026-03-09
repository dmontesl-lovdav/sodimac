package com.sodimac.cfdi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.menu.CatMenuEntity;
import com.sodimac.cfdi.entity.fiscal.menu.CatPerfilEntity;
import com.sodimac.cfdi.entity.fiscal.menu.CatRolEntity;
import com.sodimac.cfdi.entity.fiscal.menu.CatRolMenuEntity;
import com.sodimac.cfdi.model.menu.MenuByPerfil;
import com.sodimac.cfdi.model.menu.MenuByUsuario;
import com.sodimac.cfdi.model.menu.MenuItem;
import com.sodimac.cfdi.repository.fiscal.menu.CatMenuRepository;
import com.sodimac.cfdi.repository.fiscal.menu.CatPerfilRepository;
import com.sodimac.cfdi.repository.fiscal.menu.CatUsuarioPerfilRepository;
import com.sodimac.cfdi.util.enums.EMenuType;

@Service
public class MenuServiceImpl implements MenuService {

	@Autowired
	private CatMenuRepository catMenuRepository;
	
	@Autowired
	private CatPerfilRepository catPerfilRepository;
	
	
	List<MenuByPerfil> menuByPerfilList = new ArrayList<MenuByPerfil>();
	List<MenuByUsuario> menuByUsuarioList = new ArrayList<MenuByUsuario>();
	
	
	@Override
	public MenuByPerfil getMenuByPerfil(int perfil, String Action, String contextPath) {
		// TODO Auto-generated method stub
		MenuByPerfil menuByPerfil = menuByPerfilList.stream().filter(x -> x.getPerfil() == perfil ).findFirst().orElse(null);
		if (menuByPerfil == null || Action.equals("LOGIN")) {
			if(menuByPerfil != null ) {
				menuByPerfilList.remove(menuByPerfilList.indexOf(menuByPerfil));
			}
				
				
				menuByPerfil = new MenuByPerfil();
				menuByPerfil.setPerfil(perfil);

				// Va buscar el menu a la base de datos por el perfil y arma el menu
				List<CatMenuEntity> catMenuList = catMenuRepository.findMenuByPerfil(perfil);
				for(CatMenuEntity catMenuItem : catMenuList) {
					if(catMenuItem.getUrl() != null && catMenuItem.getUrl() != "") {
						menuByPerfil.getPaginas().add(catMenuItem.getUrl());
					}
					
				}
				menuByPerfil.setLstMenuItem(creaMenu(catMenuList, contextPath));
				menuByPerfilList.add(menuByPerfil);
				
		} 

		return menuByPerfil;
	}
	
	@Override
	public MenuByUsuario getMenuByUser(int idUsuario, String Action, String contextPath) {
		// TODO Auto-generated method stub
		MenuByUsuario menuByUsuario = menuByUsuarioList.stream().filter(x -> x.getIdUsuario() == idUsuario ).findFirst().orElse(null);
		if (menuByUsuario == null || Action.equals("LOGIN")) {
			if(menuByUsuario != null ) {
				menuByUsuarioList.remove(menuByUsuarioList.indexOf(menuByUsuario));
			}
				
				
				menuByUsuario = new MenuByUsuario();
				menuByUsuario.getPaginas().add("/inicio");
				menuByUsuario.setIdUsuario(idUsuario);
				
				List<CatPerfilEntity> catPerfilList = catPerfilRepository.findPerfilesByUser(idUsuario);
				menuByUsuario.setPerfiles(catPerfilList.stream().map(n -> n.getDescripcion()).collect(Collectors.toList()).toString());
				// Va buscar el menu a la base de datos por el perfil y arma el menu
				List<CatMenuEntity> catMenuList = catMenuRepository.findMenuByPerfil(idUsuario);
				for(CatMenuEntity catMenuItem : catMenuList) {
					if(catMenuItem.getUrl() != null && catMenuItem.getUrl() != "") {
						menuByUsuario.getPaginas().add(catMenuItem.getUrl());
					}
					
				}
				menuByUsuario.setLstMenuItem(creaMenu(catMenuList, contextPath));
				menuByUsuarioList.add(menuByUsuario);
				
		} 

		return menuByUsuario;
	}
	
	private List<MenuItem> creaMenu( List<CatMenuEntity> catMenuList, String contextPath){
		List<MenuItem> listMenuItem = new ArrayList<MenuItem>();
		int maxIdPadre = 0;
		if(catMenuList.size() > 0) {
			maxIdPadre = catMenuList.stream().mapToInt(v -> v.getIdpadre()).max().orElseThrow(NoSuchElementException::new);
			List<CatMenuEntity> catMenuEntityLst = new ArrayList<>();
		    for (int i=0; i <= maxIdPadre; i++) {
		    	int idPadre = i;
		    	catMenuEntityLst = catMenuList.stream().filter(v -> v.getIdpadre() == idPadre).collect(Collectors.toList());
		    	
				for(CatMenuEntity catMenuItem : catMenuEntityLst) {
					if (catMenuItem.getIdpadre() == 0 ) { //Es un Modulo-Padre
						MenuItem module = new MenuItem(catMenuItem.getNombre(), EMenuType.Modulo, new ArrayList<>(), "#", catMenuItem.getId(), catMenuItem.getIcon());
						listMenuItem.add(module);
					} else { //es un Item hijo
						MenuItem module = listMenuItem.stream().filter(v -> v.getId() == catMenuItem.getIdpadre()).findFirst().orElse(null);
						module.getLstMenuItem().add(new MenuItem(catMenuItem.getNombre(), EMenuType.Item, new ArrayList<>(), contextPath + catMenuItem.getUrl(), catMenuItem.getId(), catMenuItem.getIcon() ));
					}
					
				}
		    	
		    }
		} 
		

		/*
		 * MenuItem module1 = new MenuItem("Modulo1", EMenuType.Modulo,
		 * Arrays.asList(new MenuItem("Item1", EMenuType.Item, null), new
		 * MenuItem("Item2", EMenuType.Item, null)) ); MenuItem module2 = new
		 * MenuItem("Modulo2", EMenuType.Modulo, Arrays.asList(new MenuItem("Item1",
		 * EMenuType.Item, null), new MenuItem("Item2", EMenuType.Item, null)) );
		 * listMenuItem.add(module1); listMenuItem.add(module2);
		 */
		return listMenuItem;
	}





}
