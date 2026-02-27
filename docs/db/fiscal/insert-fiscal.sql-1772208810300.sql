INSERT INTO tenant_fiscal.flyway_schema_history (installed_rank,"version",description,"type",script,checksum,installed_by,installed_on,execution_time,success) VALUES
	 (1,'1','create fiscal tables','SQL','V1__create_fiscal_tables.sql',1768443775,'wwwb2bportal','2025-11-20 15:44:17.625663',17258,true),
	 (2,'10','insert fiscal data','SQL','V10__insert_fiscal_data.sql',926607674,'wwwb2bportal','2025-11-20 15:44:35.871913',3252,true),
	 (3,'11','alter invoice payment conditions length','SQL','V11__alter_invoice_payment_conditions_length.sql',1753233252,'wwwb2bportal','2025-11-20 15:44:39.952303',195,true),
	 (4,'12','drop invoice total calculation constraint','SQL','V12__drop_invoice_total_calculation_constraint.sql',1808211336,'wwwb2bportal','2025-11-20 15:44:40.961153',197,true),
	 (5,'13','create tax tables','SQL','V13__create_tax_tables.sql',1859188082,'wwwb2bportal','2025-11-20 15:44:41.977403',2794,true);
