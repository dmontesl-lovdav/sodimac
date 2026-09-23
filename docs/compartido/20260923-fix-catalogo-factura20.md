# Fix catalogo UAT — Factura estatus 20 "Cancelada" — 2026-09-23

Problema: al cancelar la factura, la respuesta muestra estatus 20 con nombre
"Error en la contabilizacion" en vez de "Cancelada". Es la descripcion (ES) del
catalogo CatEstatusFactura en UAT, que quedo con el nombre viejo (v1.0(2)); en
v1.0(5) se renombro a "Cancelada" pero ese cambio no se aplico en UAT.
Funcionalmente 20 = Cancelada (las cascadas corren bien); solo es la etiqueta.

NOTA: el nombre lo lee fiscal-api directo de la BD (no cachea util-api), asi que
el cambio aplica en vivo, sin redeploy.

==========================================================================
1) Correr en la BD Postgres de UAT (shared_catalogs)
==========================================================================
-- Actualiza SOLO el ES (lang_id=1) del value 20 de CatEstatusFactura -> "Cancelada".
-- Portable: resuelve el dict_id por join (no hardcodea id).
UPDATE shared_catalogs.dictionary_lang dl
SET description = 'Cancelada'
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
WHERE dl.dict_id = cd.dict_id
  AND ch.code = 'CatEstatusFactura'
  AND cd.value = '20'
  AND dl.lang_id = 1;

==========================================================================
2) Verificar
==========================================================================
SELECT cd.value, dl.lang_id, dl.description
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = cd.dict_id
WHERE ch.code = 'CatEstatusFactura' AND cd.value = '20'
ORDER BY dl.lang_id;
-- esperado lang_id 1 -> "Cancelada"

==========================================================================
3) Confirmar en el endpoint (opcional)
==========================================================================
-- Volver a cancelar una factura (o consultar una ya cancelada) y ver que
-- estatusNuevoNombre / el nombre del 20 salga "Cancelada".

Nota: EN (lang 2) y PT (lang 3) siguen con nombre viejo ("Accounting error" /
"Erro na contabilizacao"). Si se usan esos idiomas, avisar para corregir tambien;
por ahora solo ES que es el que se muestra.
