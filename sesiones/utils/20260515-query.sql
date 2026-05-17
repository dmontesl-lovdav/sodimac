-- Q1
SELECT fiscal_payment_uuid, payment_number, company, document_number,
       reference_number, vendor_number, amount, currency, document_type,
       payment_date, status, sap_document, payment_method, bank_account,
       reference_payment, created_by, created_at, updated_by, updated_at
FROM tenant_finance.fiscal_payments
WHERE fiscal_payment_uuid = 'eba316c2-f61a-4705-8c0e-ec38cae9ccf7';

-- Q2
SELECT * FROM tenant_finance.fiscal_payments
WHERE payment_number = 'PAY-DBG-20260512-2330';

-- Q3
SHOW timezone;
SELECT current_setting('TimeZone'), now(), now() AT TIME ZONE 'UTC' AS utc_now;

-- Q4
SELECT fiscal_payment_uuid, payment_number, vendor_number, amount,
       payment_date, status, created_at
FROM tenant_finance.fiscal_payments
ORDER BY created_at DESC
LIMIT 5;


"fiscal_payment_uuid","payment_number","company","document_number","reference_number","vendor_number","amount","currency","document_type","payment_date","status","sap_document","payment_method","bank_account","reference_payment","created_by","created_at","updated_by","updated_at"
eba316c2-f61a-4705-8c0e-ec38cae9ccf7,PAY-DBG-20260512-2330,1,DOC-DBG-001,REF-DBG-001,1001,15000.00,MXN,PP,2026-05-11,1,,,,,,2026-05-15 19:19:03.371,,2026-05-15 19:19:03.371

"fiscal_payment_uuid","payment_number","company","document_number","reference_number","vendor_number","amount","currency","document_type","sap_document","payment_date","status","payment_method","bank_account","reference_payment","created_by","created_at","updated_by","updated_at"
f0e79553-1ae9-489c-843c-ad52bc12a8a7,PAY-DBG-20260512-2330,1,DOC-DBG-001,REF-DBG-001,1001,15000.00,MXN,PP,,2026-05-11,1,,,,,2026-05-14 21:35:38.784,,2026-05-14 21:35:38.784
"36fd627d-675e-4138-87ed-33577bce8dfa",PAY-DBG-20260512-2330,1,DOC-DBG-001,REF-DBG-001,1001,15000.00,MXN,PP,,2026-05-11,1,,,,,2026-05-14 22:29:48.085,,2026-05-14 22:29:48.085
"91afbe88-848f-469e-8116-3bb1bfd93c8b",PAY-DBG-20260512-2330,1,DOC-DBG-001,REF-DBG-001,1001,15000.00,MXN,PP,,2026-05-11,1,,,,,2026-05-14 23:16:08.934,,2026-05-14 23:16:08.934
"73c7f648-4016-4f17-b793-8cd529d5af84",PAY-DBG-20260512-2330,1,DOC-DBG-001,REF-DBG-001,1001,15000.00,MXN,PP,,2026-05-11,1,,,,,2026-05-15 17:08:05.198,,2026-05-15 17:08:05.198
"33167cfe-b787-4fa8-adca-1c3a2a685707",PAY-DBG-20260512-2330,1,DOC-DBG-001,REF-DBG-001,1001,15000.00,MXN,PP,,2026-05-11,1,,,,,2026-05-15 17:17:31.782,,2026-05-15 17:17:31.782
eba316c2-f61a-4705-8c0e-ec38cae9ccf7,PAY-DBG-20260512-2330,1,DOC-DBG-001,REF-DBG-001,1001,15000.00,MXN,PP,,2026-05-11,1,,,,,2026-05-15 19:19:03.371,,2026-05-15 19:19:03.371


"TimeZone"
America/Mexico_City


"current_setting","now","utc_now"
America/Mexico_City,2026-05-15 14:02:29.678 -0600,2026-05-15 20:02:29.678


"fiscal_payment_uuid","payment_number","vendor_number","amount","payment_date","status","created_at"
eba316c2-f61a-4705-8c0e-ec38cae9ccf7,PAY-DBG-20260512-2330,1001,15000.00,2026-05-11,1,2026-05-15 19:19:03.371
"33167cfe-b787-4fa8-adca-1c3a2a685707",PAY-DBG-20260512-2330,1001,15000.00,2026-05-11,1,2026-05-15 17:17:31.782
"73c7f648-4016-4f17-b793-8cd529d5af84",PAY-DBG-20260512-2330,1001,15000.00,2026-05-11,1,2026-05-15 17:08:05.198
"91afbe88-848f-469e-8116-3bb1bfd93c8b",PAY-DBG-20260512-2330,1001,15000.00,2026-05-11,1,2026-05-14 23:16:08.934
"36fd627d-675e-4138-87ed-33577bce8dfa",PAY-DBG-20260512-2330,1001,15000.00,2026-05-11,1,2026-05-14 22:29:48.085


https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/rebates?pageNumber=1&pageSize=10

una duda sobre los servicios para dar de alta un pago y un descuento comercial



fiscal-api (APP03022-mrch.backend.somx.fiscal-api)
Java, va en configmap


SECURITY_ENABLED=true
UTILS_API_ENABLED=true
UTILS_API_URL=http://localhost:3712          # ← cambiar a service UAT del util-api
UTILS_API_TIMEOUT=5000
finanzas-api (APP03022-mrch.backend.somx.finanzas-api)
Node, va en configmap


SECURITY_ENABLED=true
UTIL_API_URL=http://localhost:3712           # ← cambiar a service UAT del util-api
bff.finanzas (APP03022-mrch.bff.somx.ppsomx.finanzas)


UTIL_API_URL=http://localhost:3712           # ← cambiar a service UAT del util-api
bff.fiscal (APP03022-mrch.bff.somx.ppsomx.fiscal)


UTIL_API_URL=http://localhost:3712           # ← cambiar a service UAT del util-api



-------

fiscal-api
 
SECURITY_ENABLED=false
UTILS_API_ENABLED=true
UTILS_API_URL=http://localhost:3712
UTILS_API_TIMEOUT=5000
 
finanzas-api
SECURITY_ENABLED=false
UTIL_API_URL=http://localhost:3712
 
bff.finanzas
UTIL_API_URL=http://localhost:3712
 
bff.fiscal
UTIL_API_URL=http://localhost:3712