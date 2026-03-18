package com.sodimac.facturacion.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {
 
    @RequestMapping(value = "errors", method = RequestMethod.GET)
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {
         
        ModelAndView errorPage = new ModelAndView("errorPage");
        String errorMsg = "";
        int httpErrorCode = getErrorCode(httpRequest);
 
        switch (httpErrorCode) {
            case 400: {
                errorMsg = "Solicitud inválida";
                break;
            }
            case 401: {
                errorMsg = "No autorizado";
                break;
            }
            case 404: {
                errorMsg = "Recurso no encontrado";
                break;
            }
            case 500: {
                errorMsg = "Error interno del servidor";
                break;
            }
        }
               
        errorPage.addObject("numeroMsg", httpErrorCode);
        errorPage.addObject("errorMsg", errorMsg);
        return errorPage;
    }
     
    private int getErrorCode(HttpServletRequest httpRequest) {
        return (Integer) httpRequest
          .getAttribute("javax.servlet.error.status_code");
    }
}