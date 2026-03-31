package com.sodimac.bctfacturacion;

import java.text.ParseException;
import java.util.Scanner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import com.sodimac.bctfacturacion.enums.EProcesos;
import com.sodimac.bctfacturacion.job.SodimacJob;
import com.sodimac.bctfacturacion.job.impl.PortalInHouseToEstFacturasJob;
import com.sodimac.bctfacturacion.job.impl.PortalInHouseToFacturasClienteJob;
import com.sodimac.bctfacturacion.job.impl.ReplicarPuntosJob;
import com.sodimac.bctfacturacion.job.impl.ReplicarVentasJob;
import com.sodimac.bctfacturacion.job.impl.TimbradoGlobalJob;

@SpringBootApplication
public class BctfacturacionApplication {

	public static void main(String[] args) {
		
		SpringApplication sa = new SpringApplication(BctfacturacionApplication.class);
        sa.setBannerMode(Banner.Mode.OFF);
        sa.setLogStartupInfo(false);
        
        ApplicationContext context = sa.run(args);
       
        if (args != null && args.length > 0) {
        	int selection = Integer.parseInt(args[0]);
        	EProcesos procesoSel = EProcesos.getProceso(selection);
       	   try {
				executeProcess(context, procesoSel);
			} catch (ParseException e) {
				System.out.println("Error al intentar procesar la sincronización " + e.getMessage());
			}
       	 
        } else {
        	execute(context);
        }
        
//        EProcesos procesoSel = EProcesos.ADMIN_PUNTOS_CES;
//    	try {
//			executeProcess(context, procesoSel);
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
	}
	
	@SuppressWarnings({ "resource", "unused" })
	private static void execute(ApplicationContext context) {
		System.out.println("");
    	System.out.println("*********************************");
    	System.out.println("************* Menu **************");
    	System.out.println("");
    	
    	System.out.println("Seleccionar una opcion:");
    	
    	int selection = 0;
         for (EProcesos proc : EProcesos.values()) {
             System.out.printf("[%s] - %s%n", proc.getIdProceso(), proc.getDescripcion());
         }
         String input = new Scanner(System.in).nextLine();
         try{
             selection = Integer.parseInt(input);
             
             EProcesos procesoSel = EProcesos.getProceso(selection);
             if (procesoSel == null) {
            	 System.out.println("No es una opcion valida.");
             } else {
            	 
            	 try {
					executeProcess(context, procesoSel);
				} catch (ParseException e) {
					System.out.println("Error al intentar procesar la sincronización " + e.getMessage());
				}
             }
         }catch(NumberFormatException e){
             System.out.println("No es una opcion valida.");
             selection = 0;
         }
	}
	
	@SuppressWarnings("incomplete-switch")
	private static void executeProcess(ApplicationContext context, EProcesos procesoSel) throws ParseException {
		switch(procesoSel){
	  		case FACTURAS_TO_BCT:
	  			System.out.println("Inicio del proceso " + EProcesos.FACTURAS_TO_BCT.getDescripcion());
	  			SodimacJob job = context.getBean(PortalInHouseToEstFacturasJob.class);
	  			job.sincroniza();
	  			System.out.println("Fin del proceso " + EProcesos.FACTURAS_TO_BCT.getDescripcion());
	  			break;
	  			
	  		case VENTA_CAB:
	  			System.out.println("Inicio del proceso " + EProcesos.VENTA_CAB.getDescripcion());
	  			
	  			SodimacJob jobVentas = context.getBean(ReplicarVentasJob.class);
	  			jobVentas.sincroniza();
	  			System.out.println("Fin del proceso " + EProcesos.VENTA_CAB.getDescripcion());
	  			break;
	  			
	  		case FACTURAS_TO_FISCAL:
	  			System.out.println("Inicio del proceso " + EProcesos.FACTURAS_TO_FISCAL.getDescripcion());
	  			SodimacJob jobFiscal = context.getBean(PortalInHouseToFacturasClienteJob.class);
	  			jobFiscal.sincroniza();
	  			jobFiscal = null;
	  			System.out.println("Fin del proceso " + EProcesos.FACTURAS_TO_FISCAL.getDescripcion());
	  			break;
	  			
	  		case FACTURA_GLOBAL:
	  			System.out.println("Inicio del proceso " + EProcesos.FACTURA_GLOBAL.getDescripcion());
	  			SodimacJob jobGlobal = context.getBean(TimbradoGlobalJob.class);
	  			try
	  			{
	  			  jobGlobal.sincroniza();
	  			}
	  			catch(Exception ex)
	  			{
	  			  System.out.print(ex.toString());
	  			}
	  			
	  			jobGlobal=null;
	  			System.out.println("Fin del proceso " + EProcesos.FACTURA_GLOBAL.getDescripcion());
	  			break;
	  		case ADMIN_PUNTOS_CES:
	  			System.out.println("Inicio del proceso " + EProcesos.ADMIN_PUNTOS_CES.getDescripcion());
	  			ReplicarPuntosJob jobPuntosCes = context.getBean(ReplicarPuntosJob.class);
	  			jobPuntosCes.sincroniza();
	  			jobFiscal = null;
	  			System.out.println("Fin del proceso " + EProcesos.ADMIN_PUNTOS_CES.getDescripcion());
	  			break;
	  			
		 }
	}
}
