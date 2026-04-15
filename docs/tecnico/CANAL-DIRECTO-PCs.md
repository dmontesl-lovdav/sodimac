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

## Respuestas pendientes
- [ ] Resultado de `Get-SmbShare` en la PC Sodimac (¿ya hay share?).
- [ ] Usuario de Windows de la PC Sodimac (`whoami`).
- [ ] ¿Firewall permite SMB entrante? (regla "File and Printer Sharing" habilitada).
