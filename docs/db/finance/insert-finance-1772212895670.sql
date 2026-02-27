INSERT INTO tenant_finance.sap_document (sap_document_uuid,document_number,reference_number,vendor_number,amount,"source",doc_sap,message,sap_status,document_type,created_by,created_at,updated_by,updated_at) VALUES
	 ('d5962c28-3817-415c-bfa4-df2a39417deb'::uuid,'DOC-2024-001','REF-DOC-001',1001,10000.00,1,'SAP-DOC-001','Documento de prueba',1,'IN',1,'2025-11-10 09:04:52.026742',NULL,NULL),
	 ('ae5237e1-a362-44fe-9221-0c4e5f6e09b8'::uuid,'DOC-2024-002','REF-DOC-002',1002,15000.00,1,'SAP-DOC-002','Factura de servicios',1,'SV',1,'2025-11-10 09:04:52.026742',NULL,NULL),
	 ('cd8f2b2a-905e-44bc-b499-8a509c5c2950'::uuid,'DOC-DEMO-001','REF-DEMO-001',9999,5000.00,1,'SAP-DEMO-001','Documento de demostración',1,'DM',1,'2025-11-10 09:04:52.026742',NULL,NULL);
