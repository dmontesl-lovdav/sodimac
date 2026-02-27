INSERT INTO tenant_finance.vendor_block (vendor_block_uuid,vendor_number,block_reason,block_description,start_date,end_date,status,auto_unblock,block_type,created_by,created_at,updated_by,updated_at) VALUES
	 ('d60245a7-44b3-4b16-bc26-445c48fb6d8f'::uuid,2001,'CREDIT','Bloqueo por límite de crédito excedido','2024-01-01','2024-03-31',1,true,'TEMP',1,'2025-11-10 09:04:52.026742',NULL,NULL),
	 ('5b0959e5-d228-4e43-9fe5-165de6204ef4'::uuid,2002,'LEGAL','Bloqueo por proceso legal','2024-01-15',NULL,1,false,'PERM',1,'2025-11-10 09:04:52.026742',NULL,NULL),
	 ('44430e1d-cad4-4e7e-9669-06643ff72f79'::uuid,9998,'DEMO','Bloqueo de demostración temporal','2024-01-01','2024-12-31',1,true,'PART',1,'2025-11-10 09:04:52.026742',NULL,NULL);
