# Fix nombres de estatus de FACTURA (casing + catalogo) — 2026-09-23

Problema: en UAT los nombres de estatus de factura salian mal (ej. 20 = "Error en la
contabilizacion" en vez de "Cancelada"). Dos causas:
1. Casing: el header en UAT es CATESTATUSFACTURA (mayusculas) y fiscal buscaba
   "CatEstatusFactura" con query case-sensitive -> no matcheaba -> caia al enum.
2. Datos: ese catalogo quedo con SOLO 1 valor (status 1); faltaban 2-20.

Fix en 2 partes:
- CODIGO: findCatalogDescription ahora usa UPPER(ch.code)=UPPER(:code) (robusto a casing).
- CATALOGO: repoblar los estatus 2-20 (ES) en UAT.

Reemplaza el intento anterior (20260923-fix-catalogo-factura20.md), que estaba mal
diagnosticado (no era una descripcion, era casing + catalogo casi vacio). Descartalo.

==========================================================================
PARTE 1 - Pase de codigo (PC Sodimac, PowerShell)
==========================================================================
cd C:\local
git checkout dmontes
git pull origin dmontes

$proj = "APP03022-mrch.backend.somx.fiscal-api"
robocopy "C:\local\$proj" "C:\workspace-fbc-github\$proj" /MIR /XD .git node_modules dist target build .idea /XF *.log

cd "C:\workspace-fbc-github\$proj"
git checkout develop
git pull origin develop
git status   # esperado: SOLO src/main/java/.../repository/AddendumRepository.java
git add src/main/java/com/sodimac/fiscal/api/repository/AddendumRepository.java
git commit -m "fix: findCatalogDescription case-insensitive por code (evita nombre por enum fallback)"
git push origin develop

git checkout uat
git pull origin uat
git merge develop --no-ff -m "merge: findCatalogDescription case-insensitive"
git push origin uat

(NOTA: el archivo migration/QA-2026-09-23-repoblar-catestatusfactura.sql tambien
va en el commit por el /MIR; es solo el script, no afecta el build. Si prefieres,
git add solo el .java.)

==========================================================================
PARTE 2 - Repoblar catalogo (BD Postgres de UAT, schema shared_catalogs)
==========================================================================
Correr el script:
  APP03022-mrch.backend.somx.fiscal-api/migration/QA-2026-09-23-repoblar-catestatusfactura.sql

Es idempotente (respeta el value 1 existente, no duplica). Inserta estatus 2-20 con
su descripcion ES bajo el header por code (case-insensitive).

Verificar (esperado 1..20 con nombre; 20 = Cancelada):
  SELECT cd.value, dl.description
  FROM shared_catalogs.catalog_header ch
  JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
  JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = cd.dict_id AND dl.lang_id = 1
  WHERE UPPER(ch.code) = UPPER('CatEstatusFactura')
  ORDER BY (cd.value)::int;

==========================================================================
Notas
==========================================================================
- La PARTE 2 (catalogo) se lee en vivo, sin redeploy (fiscal consulta la BD directo).
- La PARTE 1 (codigo) si requiere deploy del fiscal-api.
- Validado en local (= UAT): la query case-insensitive devuelve "Cancelada" para
  factura 20; build OK.
- Solo ES. EN/PT siguen stale (no se usan hoy en el nombre mostrado).
