import { Link } from 'react-router-dom';
import { SecurityBreadcrumb } from './components/SecurityBreadcrumb';
import iconSecurity from '@assets/icons/alert-up.png';
import { useSecurityAdmin } from './hooks/useSecurityAdmin';
import { APP_KEYS, useSecurityContext } from '@shared/security';
import './styles/SecurityContainer.css';

import iconUser from '@assets/icons/alert-up.png';
import iconApplication from '@assets/icons/alert-up.png';
import iconProfile from '@assets/icons/alert-up.png';
import iconEvent from '@assets/icons/alert-up.png';
import iconPermission from '@assets/icons/alert-up.png';
import iconAttribute from '@assets/icons/alert-up.png';

type AccessCard = {
  title: string;
  icon?: string;
  desc: string;
  to: string;
  adminOnly?: boolean;
  requiredAnyApp?: string[];
};

const cards: AccessCard[] = [
  {
    title: 'Catálogo Usuarios',
    desc: 'Consulta de perfil, roles, aplicaciones y atributos por usuario.',
    to: '/seguridad/gestion-usuarios',
    adminOnly: true,
    icon: iconUser,
  },
  {
    title: 'Aplicativo Evento',
    desc: 'Relación entre módulos (aplicativos) y procesos (eventos) del sistema.',
    to: '/seguridad/aplicativo-evento',
    icon: iconApplication,
    requiredAnyApp: [APP_KEYS.PROFILE_ADMIN, APP_KEYS.PERMISSIONS_ADMIN],
  },
  {
    title: 'Perfil Aplicativo',
    desc: 'Relación entre perfiles y módulos (aplicativos) existentes en el sistema.',
    to: '/seguridad/perfil-aplicativo',
    icon: iconProfile,
    requiredAnyApp: [APP_KEYS.PROFILE_ADMIN],
  },
  {
    title: 'Perfil Evento',
    desc: 'Relación entre perfiles y procesos (eventos) del sistema.',
    to: '/seguridad/perfil-evento',
    icon: iconEvent,
    requiredAnyApp: [APP_KEYS.PROFILE_ADMIN],
  },
  {
    title: 'Perfil Usuario',
    desc: 'Relación entre perfiles y usuarios existentes en el sistema.',
    to: '/seguridad/perfil-usuario',
    icon: iconProfile,
    requiredAnyApp: [APP_KEYS.PROFILE_ADMIN],
  },
  {
    title: 'Rol Usuario',
    desc: 'Relación entre roles y usuarios existentes en el sistema.',
    to: '/seguridad/rol-usuario',
    icon: iconProfile,
    requiredAnyApp: [APP_KEYS.ROLES_ADMIN],
  },
  {
    title: 'Rol Permiso',
    desc: 'Relación entre roles y permisos existentes en el sistema.',
    to: '/seguridad/rol-permiso',
    icon: iconPermission,
    requiredAnyApp: [APP_KEYS.ROLES_ADMIN, APP_KEYS.PERMISSIONS_ADMIN],
  },
  {
    title: 'Usuario Atributo',
    desc: 'Relación entre usuarios y atributos existentes en el sistema.',
    to: '/seguridad/usuario-atributo',
    icon: iconAttribute,
    requiredAnyApp: [APP_KEYS.PROFILE_ADMIN],
  },
];

export function SecurityContainer() {
  const { isAdmin, isLoading: isAdminLoading } = useSecurityAdmin(true);
  const sec = useSecurityContext();
  const visibleCards = cards.filter((c) => {
    if (c.adminOnly && !(!isAdminLoading && isAdmin)) return false;
    if (c.requiredAnyApp) {
      if (sec.isLoading) return false;
      if (!sec.hasAnyApp(c.requiredAnyApp)) return false;
    }
    return true;
  });

  return (
    <div className="util-root">
      <SecurityBreadcrumb items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso']} />
      <main className="util-main">
        <section className="util-box">
          <h1 className="maintainers-title">Control de Acceso</h1>
          <p className="security-hub-intro">Operaciones del módulo de autorización.</p>
          <section className="cards-grid">
            {visibleCards.map((card) => (
              <Link key={card.to} to={card.to} className="card">
                <div className="card-content">
                  <img src={card.icon ?? iconSecurity} className="card-icon" alt="" />
                  <div className="card-right">
                    <h3 className="card-title">{card.title}</h3>
                    <p className="card-desc">{card.desc}</p>
                  </div>
                </div>
              </Link>
            ))}
          </section>
        </section>
      </main>
    </div>
  );
}
