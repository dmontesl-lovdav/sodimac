# Comandos Git Utiles

Referencia rapida de comandos git usados en el proyecto.

---

## Exportar proyecto sin historial de git

Exporta un proyecto limpio (sin carpeta `.git`) para compartir o desplegar:

```bash
# Exportar como ZIP
git archive --format=zip --output=../batch-fiscal-download.zip HEAD

# Exportar como TAR.GZ
git archive --format=tar.gz --output=../batch-fiscal-download.tar.gz HEAD

# Exportar solo una carpeta especifica
git archive --format=zip --output=../solo-src.zip HEAD src/
```

Ejecutar desde la raiz del proyecto que se quiere exportar.

---

## Ramas

```bash
# Ver rama actual
git branch

# Cambiar de rama
git checkout dmontes

# Crear rama y cambiar a ella
git checkout -b nueva-rama

# Ver ramas remotas
git branch -r

# Eliminar rama local
git branch -d nombre-rama
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

# Ver cambios de un commit especifico
git show abc1234
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

## Problemas comunes en Windows

```bash
# Error con archivo "nul" (nombre reservado en Windows)
# Agregar todo excepto "nul"
git add --all -- ':!nul'

# O agregar archivos especificos en lugar de --all
git add src/ docs/ pom.xml
```

---

*Este archivo se actualiza conforme se usen nuevos comandos utiles en el proyecto.*
