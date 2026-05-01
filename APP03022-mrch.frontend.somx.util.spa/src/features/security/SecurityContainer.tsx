import { Link } from 'react-router-dom';
import { SecurityBreadcrumb } from './components/SecurityBreadcrumb';
import iconSecurity from '@assets/icons/alert-up.png';
import './styles/SecurityContainer.css';

type AccessCard = {
  title: string;
  desc: string;
  to: string;
};

const cards: AccessCard[] = [
  {
    title: 'Aplicativo Evento',
    desc: 'Relación entre módulos (aplicativos) y procesos (eventos) del sistema.',
    to: '/seguridad/aplicativo-evento',
  },

  {
    title: 'Perfil Aplicativo',
    desc: 'Relación entre perfiles y módulos (aplicativos) existentes en el sistema.',
    to: '/seguridad/perfil-aplicativo',
  },
  {
    title: 'Perfil Evento',
    desc: 'Relación entre perfiles y procesos (eventos) del sistema.',
    to: '/seguridad/perfil-evento',
  },

  {
    title: 'Perfil Usuario',
    desc: 'Relación entre perfiles y usuarios existentes en el sistema.',
    to: '/seguridad/perfil-usuario',
  },
 
 
 
  {
    title: 'Rol Usuario',
    desc: 'Relación entre roles y usuarios existentes en el sistema.',
    to: '/seguridad/rol-usuario',
  },
  {
    title: 'Rol Permiso',
    desc: 'Relación entre roles y permisos existentes en el sistema.',
    to: '/seguridad/rol-permiso',
  },
  {
    title: 'Usuario Atributo',
    desc: 'Relación entre usuarios y atributos existentes en el sistema.',
    to: '/seguridad/usuario-atributo',
  },
];

export function SecurityContainer() {
  return (
    <div className="util-root">
      <SecurityBreadcrumb items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso']} />
      <main className="util-main">
        <section className="util-box">
          <h1 className="maintainers-title">Control de Acceso</h1>
          <p className="security-hub-intro">Operaciones del módulo de autorización.</p>
          <section className="cards-grid">
            {cards.map((card) => (
              <Link key={card.to} to={card.to} className="card">
                <div className="card-content">
                  <img src={iconSecurity} className="card-icon" alt="" />
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
