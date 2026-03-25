# Comandos Git Utiles

Referencia rapida de comandos git usados en el proyecto.

---

## Exportar proyecto sin historial de git

Exporta un proyecto limpio (sin carpeta `.git`) para compartir o desplegar:

```bash
# Exportar como ZIP
git archive --format=zip --output=../fiscal-download.zip HEAD

# Exportar como TAR.GZ
git archive --format=tar.gz --output=../fiscal-download.tar.gz HEAD

# Exportar solo una carpeta especifica
git archive --format=zip --output=../solo-src.zip HEAD src/
```

Ejecutar desde la raiz del proyecto que se quiere exportar.

---

## Ramas

```bash
# Ver rama actual
git branch

# Ver todas las ramas con ultimo commit, autor y fecha
git for-each-ref --sort=-committerdate refs/heads/ refs/remotes/ --format="%(refname:short) | %(committerdate:short) | %(authorname) | %(subject)"

# Ver ramas remotas
git branch -r
```

### Crear rama

```bash
# Crear rama nueva a partir de la rama actual y cambiar a ella
git checkout -b mi-nueva-rama

# Crear rama a partir de otra rama especifica
git checkout -b mi-nueva-rama origin/main

# Subir la rama nueva al remoto por primera vez
git push -u origin mi-nueva-rama
```

### Cambiar de rama

```bash
# Cambiar a una rama existente
git checkout dmontes

# Cambiar a main
git checkout main

# IMPORTANTE: antes de cambiar de rama, asegurate de no tener cambios sin commit
# Puedes verificar con:
git status

# Si tienes cambios y no quieres hacer commit aun, guardalos con stash:
git stash
git checkout otra-rama
# Cuando regreses, recupera tus cambios:
git checkout dmontes
git stash pop
```

### Eliminar rama

```bash
# Eliminar rama local (solo si ya fue mergeada)
git branch -d nombre-rama

# Eliminar rama remota
git push origin --delete nombre-rama
```

---

## Commits

```bash
# Ver estado de archivos
git status

# Agregar archivos especificos
git add archivo1.java archivo2.java

# Agregar todos los cambios (usar con cuidado)
git add --all

# Commit con mensaje
git commit -m "feat: descripcion del cambio"

# Ver historial de commits
git log --oneline -10

# Ver historial con grafico de ramas
git log --oneline --all --graph

# Ver historial detallado (quien, cuando, que)
git log --format="%h %an %ci %s" -10

# Ver quien subio lo ultimo en cada rama
git for-each-ref --sort=-committerdate refs/heads/ --format="%(refname:short) | %(committerdate:short) | %(authorname) | %(subject)"

# Ver cambios de un commit especifico
git show abc1234

# Ver los archivos modificados en un commit
git show --stat abc1234
```

---

## Merge (unir ramas)

```bash
# Ejemplo: traer los cambios de dmontes a main
# Paso 1: Cambiar a la rama destino (la que recibe los cambios)
git checkout main

# Paso 2: Asegurar que esta actualizada
git pull origin main

# Paso 3: Hacer el merge
git merge dmontes

# Paso 4: Subir el resultado
git push origin main

# Paso 5: Regresar a tu rama de trabajo
git checkout dmontes
```

### Si hay conflictos en el merge

```bash
# Git te indicara los archivos con conflicto
# Abrir cada archivo, buscar las marcas <<<<<<<, =======, >>>>>>>
# Resolver manualmente y luego:
git add archivo-resuelto.java
git commit -m "fix: resolver conflictos del merge dmontes a main"
git push origin main

# Si quieres cancelar el merge y regresar al estado anterior
git merge --abort
```

---

## Sincronizacion con remoto

```bash
# Descargar cambios sin aplicar
git fetch origin

# Descargar y aplicar cambios
git pull origin dmontes

# Subir cambios
git push origin dmontes

# Ver remotos configurados
git remote -v
```

---

## Diferencias

```bash
# Ver cambios no staged
git diff

# Ver cambios staged (listos para commit)
git diff --staged

# Comparar con otra rama
git diff main..dmontes

# Ver commits que tiene una rama y otra no
git log main..dmontes --oneline

# Comparar un archivo especifico
git diff -- src/main/java/MiClase.java
```

---

## Stash (guardar cambios temporalmente)

```bash
# Guardar cambios actuales
git stash

# Guardar con descripcion
git stash push -m "cambios en progreso de STM-335"

# Listar stashes
git stash list

# Recuperar ultimo stash
git stash pop

# Recuperar stash especifico
git stash pop stash@{1}
```

---

## Deshacer cambios

```bash
# Descartar cambios en un archivo (no committed)
git checkout -- archivo.java

# Quitar archivo del staging (sin perder cambios)
git reset HEAD archivo.java

# Revertir un commit (crea nuevo commit inverso)
git revert abc1234
```

---

## Buscar en el historial

```bash
# Buscar commits por mensaje
git log --all --grep="STM-704" --oneline

# Buscar quien modifico una linea de un archivo (blame)
git blame src/main/java/MiClase.java

# Buscar un texto en todo el historial de commits
git log --all -S "DiasPermitidosFacturar" --oneline
```

---

## Eliminar referencias .git de un proyecto externo

Cuando copias un proyecto que tiene su propio historial git y quieres subirlo a otro repo,
primero elimina las carpetas `.git` de forma recursiva.

Abrir **PowerShell**, navegar al directorio del proyecto y ejecutar:

```powershell
Get-ChildItem -Path . -Filter ".git" -Directory -Recurse -Force | Remove-Item -Recurse -Force
```

> Nota: El comando `find . -name ".git" -type d -exec rm -rf {} +` no funciona
> correctamente en Windows. Usar PowerShell.

---

## Problemas comunes en Windows

```bash
# Error con archivo "nul" (nombre reservado en Windows)
# Agregar todo excepto "nul"
git add --all -- ':!nul'

# O agregar archivos especificos en lugar de --all
git add src/ docs/ pom.xml

# Error de line endings (CRLF vs LF)
git config core.autocrlf true
```

---

*Este archivo se actualiza conforme se usen nuevos comandos utiles en el proyecto.*
