package com.sodimac.cfdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sodimac.cfdi.models.ImpuestoFacturaModel;

public class ReadXmlTest {

	@Test
	public void test() {
		
		
	    try {
	    	FileInputStream fis = new FileInputStream("C:\\Users\\g_daf01\\Desktop\\factura-impuestos.xml");
			String data = IOUtils.toString(fis, "UTF-8");
			Document document = this.obtenerDocumentXml(data);
			List<ImpuestoFacturaModel> listImpuesto = this.getImpuestos(document);
			for (ImpuestoFacturaModel imp : listImpuesto) { 
				System.out.println( imp.getBase());
				System.out.println( new BigDecimal(imp.getTasa()) );
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	private Document obtenerDocumentXml(String xml) {
		Document document = null;
		try {
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xml));
	        document = documentBuilder.parse(is);
	        document.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
        return document;		
	}

	private List<ImpuestoFacturaModel> getImpuestos(Document document) {
		List<ImpuestoFacturaModel> listImpuesto = new ArrayList<ImpuestoFacturaModel>();
		NodeList nodeImpuestos = document.getElementsByTagName("cfdi:Impuestos");
	    for (int i = 0; i < nodeImpuestos.getLength(); i++) {
	        
	        Node currentImpuesto = nodeImpuestos.item(i);
	        Element eElement = (Element) currentImpuesto;
	        boolean TotalImpuestosTrasladados = StringUtils.isEmpty(eElement.getAttribute("TotalImpuestosTrasladados"));
	        if (!TotalImpuestosTrasladados) {
	        	
	        	System.out.println(currentImpuesto + " " + TotalImpuestosTrasladados);
	        	NodeList listImpuestos = eElement.getChildNodes();
	    	    for (int j = 0; j < listImpuestos.getLength(); j++) {
	    	        Node nodeTraslados = listImpuestos.item(j);
	    	        
	    	        if (nodeTraslados.getNodeType() == Node.ELEMENT_NODE) {
	    	            NodeList listTraslados = nodeTraslados.getChildNodes();
	    	        	for (int k = 0; k< listTraslados.getLength(); k++) {
	    	        		Node traslado = listTraslados.item(k);
	    	        		
	    	        		if (traslado.getNodeType() == Node.ELEMENT_NODE) {
			    	            Element eElementTraslado = (Element) traslado;
			    	            
			    	            String base = eElementTraslado.getAttribute("Base");
			    	            String tipoImpuesto = eElementTraslado.getAttribute("Impuesto");
			    	            String tasa = eElementTraslado.getAttribute("TasaOCuota");
			    	            String importe = eElementTraslado.getAttribute("Importe");
								
								ImpuestoFacturaModel imp = new ImpuestoFacturaModel();
								imp.setBase(base);
								imp.setTipoImpuesto(tipoImpuesto);
								imp.setTasa(tasa);
								imp.setImporte(importe);
								
								listImpuesto.add( imp );
	    	        		}
	    	        	}//for (int k = 0; k< listTraslados.getLength(); k++)
	    	        }
	    	        
	    	    }//for (int j = 0; j < listImpuestos.getLength(); j++)
	    	    
	        }
	    }//for (int i = 0; i < nodeImpuestos.getLength(); i++)
		return listImpuesto;
	}
}
