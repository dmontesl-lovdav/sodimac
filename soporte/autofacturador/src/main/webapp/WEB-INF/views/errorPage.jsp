<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<!DOCTYPE html>
<html lang="es-mx"> 
<head>
    <title> SODIMAC México - Facturación </title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link type="text/css" rel="stylesheet" href="./resources/css/bootstrap.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/custom.css" />
    <link type="image/ico" rel="icon" href="resources/img/favicon.ico">
</head>
<body>

    <header class="header">
        <nav class="navbar navbar-static-top">
            <div class="nav_header">
                <div class="container px-0">
                    <a href="https://www.sodimac.com.mx/sodimac-mx/" class="logo navbar-brand float-left text-white text-center">
                        <img src="resources/img/header.jpg" alt="logo" class="img-fluid navbar_brand_img">
                    </a>
                    <div class="top_right_nav">
                        <div class="float-right">
                            <h2 class="main-H">Servicio de facturación</h2>
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    </header>

	<div class="container">
	
		<br><br><br>
		<div class="row">
			<div class="col-md-6">
			<p><h1>Error ${numeroMsg}</h1></p>
			<p><h4>${errorMsg}</h4></p>
			<p><h4>¡Ups! algo salió mal, inténtalo de nuevo.</h4></p>
			<br>
			<a href="/facturacion">Regresar</a>
			</div>
			<div class="col-md-6">
				<img src="resources/img/errorPage.png"  alt="titulo">
			</div>			
		</div>
		<br><br>
		
		<div id="divFooter">
		    <jsp:include page="footer.jsp"/>
		    
		</div>
		
	</div>
	
</body>
</html>
