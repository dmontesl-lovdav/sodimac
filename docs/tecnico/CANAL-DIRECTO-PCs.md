# Canal directo entre PC personal y PC Sodimac

Ambas PCs están en la misma red local. Objetivo: reemplazar el flujo actual (commit/push/pull vía GitHub) por un canal directo.

## IPs de esta PC (personal)

| Adaptador | IP | Uso probable |
|-----------|-----|--------------|
| Wi-Fi | `192.168.0.121` | Red doméstica (gateway 192.168.0.1) |
| Ethernet 6 | `10.97.52.147` | VPN corporativa Sodimac |
| Conexión de área local | `10.110.1.185` | VPN/túnel secundario |
| VMware VMnet1 | `192.168.40.1` | Virtual (ignorar) |
| VMware VMnet8 | `192.168.43.1` | Virtual (ignorar) |
| WSL Hyper-V | `172.30.80.1` | Virtual (ignorar) |

**Para conectar desde la PC de Sodimac:** usar la IP que corresponda a la red compartida entre ambas PCs.
- Si ambas están en la misma red WiFi/LAN doméstica → `192.168.0.121`
- Si se conectan vía VPN corporativa de Sodimac → `10.97.52.147` o `10.110.1.185`

Verificar con `Test-NetConnection -ComputerName <IP> -Port 445` desde la PC de Sodimac.

## Opciones viables

### 1. SSH directo
Permite ejecutar comandos, `rsync`/`scp`, montar carpetas remotas. Requiere instalar OpenSSH Server (suele estar bloqueado por políticas).

### 2. Carpeta compartida SMB (Windows nativo)
La más simple y rara vez bloqueada. Se comparte el workspace desde una PC y se monta como unidad de red en la otra. Se trabaja como si fuera local.

### 3. Syncthing
Sincronización bidireccional P2P en LAN, sin servidor. Reemplaza GitHub como "puente" pero en tiempo real. No requiere admin.

### 4. VS Code Remote-SSH / Remote-Tunnels
Si funciona SSH, se abre el workspace de Sodimac desde la PC personal directamente en VS Code. Remote-Tunnels incluso funciona sin SSH (usa cuenta Microsoft/GitHub como broker).

## Obstáculo real
Política corporativa de Sodimac: firewall, bloqueo de puertos entrantes, restricción de software instalable. Si Claude está bloqueado allá, SSH/Syncthing probablemente también.

## Recomendación
Probar primero **carpeta compartida SMB** (nativo, sin instalar nada).

---

## Comandos de diagnóstico (ejecutar en PowerShell en la PC de Sodimac)

### 1. SSH (cliente y servidor)
```powershell
Get-WindowsCapability -Online | Where-Object Name -like 'OpenSSH*'
Get-Service sshd -ErrorAction SilentlyContinue
ssh -V
```
Buscar `State: Installed`. Si `sshd` existe y corre, ya hay servidor SSH.

### 2. SMB / Compartir carpetas
```powershell
Get-SmbServerConfiguration | Select EnableSMB2Protocol
Get-Service LanmanServer
Get-SmbShare
```
Si `LanmanServer` está `Running`, se pueden compartir carpetas.

### 3. Firewall — puertos entrantes
```powershell
Get-NetFirewallRule -DisplayGroup "File and Printer Sharing" | Select DisplayName, Enabled
Get-NetFirewallRule -DisplayGroup "OpenSSH Server" -ErrorAction SilentlyContinue | Select DisplayName, Enabled
```

### 4. IP y conectividad de red
```powershell
ipconfig | findstr IPv4
Test-NetConnection -ComputerName <IP-de-PC-personal> -Port 445
```

### 5. Permisos de administrador
```powershell
([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
```

### 6. Software ya instalado
```powershell
Get-ChildItem "C:\Program Files","C:\Program Files (x86)" -Directory | Select Name
winget list 2>$null | findstr /I "sync ssh git vscode"
```

## Siguiente paso
Ejecutar los comandos de diagnóstico en la PC de Sodimac y revisar la salida para decidir qué ruta implementar.

---

## Escaneo desde PC personal hacia PC Sodimac (2026-04-15)

IP de la PC Sodimac: **`192.168.0.141`** (misma LAN doméstica que `192.168.0.121`).

**Resultado del escaneo de puertos:**

| Puerto | Servicio | Estado |
|--------|----------|--------|
| **445** | **SMB (compartir carpetas)** | ✅ Abierto |
| **139** | NetBIOS | ✅ Abierto |
| **135** | RPC | ✅ Abierto |
| 22 | SSH | ❌ Cerrado |
| 3389 | RDP | ❌ Cerrado |
| 5985 | WinRM | ❌ Cerrado |
| 80 | HTTP | ❌ Cerrado |
| 443 | HTTPS | ❌ Cerrado |

Ping ICMP bloqueado, pero SMB responde → **camino viable: carpeta compartida SMB**.

---

## Plan de conexión SMB

### Paso 1 — En la PC de Sodimac (PowerShell como admin)

Verificar si ya hay algo compartido:
```powershell
Get-SmbShare
```

Crear share del workspace:
```powershell
New-SmbShare -Name "workspace-sodimac" -Path "C:\workspace-sodimac" -FullAccess "$env:USERNAME"
```

Obtener el nombre de usuario (para el paso 2):
```powershell
whoami
```

Confirmar firewall permite SMB entrante:
```powershell
Get-NetFirewallRule -DisplayGroup "File and Printer Sharing" | Where-Object Enabled -eq True | Select DisplayName
```

### Paso 2 — Desde la PC personal (PowerShell)

Listar recursos visibles en la PC Sodimac (diagnóstico):
```powershell
net view \\192.168.0.141
```

Montar el share como unidad Z:
```powershell
net use Z: \\192.168.0.141\workspace-sodimac /user:<USUARIO-SODIMAC>
```
(Pedirá la contraseña de Windows de esa PC.)

Verificar acceso:
```powershell
dir Z:\
```

### Paso 3 — Abrir en VS Code

```powershell
code Z:\
```

Editar directamente; los cambios se escriben en la PC de Sodimac.

### Desmontar al terminar
```powershell
net use Z: /delete
```

---

## Resultados del diagnóstico en PC Sodimac (2026-04-15)

Ver log crudo: [net.txt](net.txt)

| Aspecto | Resultado |
|---------|-----------|
| Host\Usuario | `cd-rosas\g_dco018` |
| IP | `192.168.0.141` |
| **Admin local** | **❌ NO** |
| LanmanServer (SMB) | ✅ Running |
| SMB2 Protocol | ✅ Habilitado |
| Shares existentes | Solo `ADMIN$`, `C$`, `IPC$` (defecto, requieren admin remoto) |
| `New-SmbShare` | ❌ Acceso denegado |
| SSH client | ✅ OpenSSH 9.5p2 |
| SSH server (`sshd`) | ❌ No instalado |
| `Test-NetConnection 192.168.0.121:445` | ❌ TCP failed + Ping timeout |
| Software relevante | Git, TortoiseGit, GitHub Desktop, **WinSCP**, DBeaver, Netskope, ForeScout, Fortinet |

### Diagnóstico
- Sin admin en PC Sodimac → **plan SMB original descartado** (no se puede crear share ni instalar SSH server allá).
- SMB entrante en PC personal (A8) bloqueado → firewall local o AP isolation. Hay que validar del otro lado (se pudo llegar de A8→Sodimac antes, así que la red permite al menos un sentido).
- Software de seguridad corporativo (Netskope, ForeScout) puede filtrar tráfico adicional.

---

## Plan B — Invertir el flujo (SSH server en PC personal)

Como la PC personal (A8) sí tiene admin, exponer ahí el servicio y que la PC Sodimac se conecte saliendo (que suele estar menos restringido).

### Paso 1 — En PC personal (A8, PowerShell admin)

Instalar OpenSSH Server:
```powershell
Add-WindowsCapability -Online -Name OpenSSH.Server~~~~0.0.1.0
Start-Service sshd
Set-Service -Name sshd -StartupType Automatic
```

Abrir puerto 22 en firewall:
```powershell
New-NetFirewallRule -Name sshd -DisplayName 'OpenSSH Server (sshd)' -Enabled True -Direction Inbound -Protocol TCP -Action Allow -LocalPort 22
```

Verificar:
```powershell
Get-Service sshd
Get-NetTCPConnection -LocalPort 22
```

### Paso 2 — Desde PC Sodimac, probar conexión

Test puerto:
```powershell
Test-NetConnection -ComputerName 192.168.0.121 -Port 22
```

Si `TcpTestSucceeded: True`:
```powershell
ssh dmont@192.168.0.121
```
(Contraseña: la de Windows de la PC personal.)

### Paso 3 — Usar WinSCP para transferencia de archivos

WinSCP ya está instalado en PC Sodimac. Nueva sesión:
- **Protocolo:** SFTP
- **Host:** `192.168.0.121`
- **Puerto:** 22
- **Usuario:** `dmont`
- **Contraseña:** Windows

Permite navegar, subir y bajar archivos a/desde PC personal. Mejor que GitHub como "buzón" rápido.

### Paso 4 (opcional) — Repo git bare en PC personal

Crear un remote directo (sin GitHub) en PC personal:
```powershell
# En A8:
mkdir C:\repos-bare\workspace-sodimac.git
cd C:\repos-bare\workspace-sodimac.git
git init --bare
```

Desde PC Sodimac, agregar remote:
```powershell
cd C:\workspace-sodimac
git remote add personal ssh://dmont@192.168.0.121/C:/repos-bare/workspace-sodimac.git
git push personal dmontes
```

Bidireccional, rápido, sin pasar por GitHub.

---

## Plan C — Syncthing portable (sin admin en ningún lado)

Si el Plan B falla por bloqueo corporativo (Netskope/ForeScout), Syncthing portable no requiere instalación ni admin y trabaja P2P en LAN.

1. Descargar Syncthing portable en ambas PCs: https://syncthing.net/downloads/
2. Ejecutar en ambas → abre UI en `http://localhost:8384`.
3. En cada PC añadir la otra como "Remote Device" (por ID).
4. Compartir la carpeta `C:\workspace-sodimac`.
5. Sincronización bidireccional automática.

Ventajas: no requiere admin, no requiere puerto entrante fijo, atraviesa NAT.

**Riesgo:** si Netskope/ForeScout inspeccionan ejecutables nuevos, pueden bloquear Syncthing. Probar.

---

## Estado actual (2026-04-15)

**PC personal (A8) — Plan B Paso 1 completado:**
- `sshd` Running / Automatic ✅
- Regla firewall `OpenSSH Server` Inbound Allow ✅
- Puerto 22 escuchando en `0.0.0.0:22` ✅
- Usuario de conexión: `dmont`
- IP: `192.168.0.121`

## Decisión pendiente
- [x] Paso 1 Plan B — OpenSSH Server operativo en A8.
- [ ] Paso 2 — Ejecutar desde PC Sodimac: `Test-NetConnection -ComputerName 192.168.0.121 -Port 22`
- [ ] Paso 3 — Si TCP succeeded → `ssh dmont@192.168.0.121` (password: Windows de A8).
- [ ] Paso 4 — Configurar WinSCP / git remote bare.
- [ ] Si Paso 2 falla → Plan C (Syncthing portable).
