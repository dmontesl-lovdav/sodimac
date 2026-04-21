import { useState } from 'react';
import { Link } from 'react-router-dom';

const styles = {
  container: {
    minHeight: '100vh',
    width: '100%',
    display: 'flex',
    flexDirection: 'column' as const,
    backgroundColor: '#ffffff',
  },
  header: {
    width: '100%',
    padding: '1rem 2rem 0 2rem',
  },
  breadcrumb: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    fontSize: '0.875rem',
    color: '#6b7280',
  },
  breadcrumbLink: {
    color: '#003865',
    textDecoration: 'none',
  },
  main: {
    width: '100%',
    backgroundColor: '#ffffff',
    padding: '1.5rem 2rem 3rem 2rem',
  },
  section: {
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    padding: '1.5rem',
    backgroundColor: 'transparent',
  },
  title: {
    fontSize: '1.5rem',
    fontWeight: 500,
    marginBottom: '1rem',
    color: '#374151',
  },
  grid: {
    display: 'grid',
    gap: '1.5rem',
    gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))',
  },
  card: {
    position: 'relative' as const,
    display: 'flex',
    flexDirection: 'column' as const,
    justifyContent: 'space-between',
    borderRadius: '1rem',
    border: '1px solid #CFE1F5',
    backgroundColor: '#EAF3FB',
    padding: '2rem',
    boxShadow: '0 1px 2px 0 rgb(0 0 0 / 0.05)',
    transition: 'box-shadow 0.2s ease',
    minHeight: '180px',
  },
  cardDark: {
    backgroundColor: '#E4EFF7',
  },
  cardTitle: {
    fontWeight: 500,
    fontSize: '1.25rem',
    color: '#333333',
    margin: 0,
  },
  cardDescription: {
    fontSize: '0.875rem',
    color: '#374151',
    marginTop: '0.75rem',
    maxWidth: '28rem',
    lineHeight: 1.5,
  },
  cardButton: {
    marginTop: '2rem',
    alignSelf: 'flex-start',
    padding: '0.625rem 1.5rem',
    borderRadius: '0.375rem',
    border: '1px solid #002d4c',
    color: '#002d4c',
    fontSize: '0.875rem',
    fontWeight: 500,
    backgroundColor: 'transparent',
    cursor: 'pointer',
    transition: 'background-color 0.2s ease',
    textDecoration: 'none',
  },
  iconWrapper: {
    position: 'absolute' as const,
    right: '1.5rem',
    top: '50%',
    transform: 'translateY(-50%)',
    height: '6rem',
    width: '6rem',
    pointerEvents: 'none' as const,
    userSelect: 'none' as const,
    opacity: 0.15,
    color: '#002d4c',
  },
};

const ProveedoresIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}>
    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
  </svg>
);

const BloqueoIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}>
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
  </svg>
);

const CatalogsIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}>
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
    <line x1="8" y1="6" x2="16" y2="6" />
    <line x1="8" y1="10" x2="16" y2="10" />
    <line x1="8" y1="14" x2="12" y2="14" />
  </svg>
);

interface CardItemProps {
  title: string;
  description: string;
  to: string;
  buttonText: string;
  Icon: React.FC;
  dark?: boolean;
}

function CardItem({ title, description, to, buttonText, Icon, dark = false }: CardItemProps) {
  const [isHovered, setIsHovered] = useState(false);
  const [isButtonHovered, setIsButtonHovered] = useState(false);

  return (
    <div
      style={{
        ...styles.card,
        ...(dark ? styles.cardDark : {}),
        boxShadow: isHovered
          ? '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)'
          : '0 1px 2px 0 rgb(0 0 0 / 0.05)',
      }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div style={{ paddingRight: '7rem' }}>
        <h4 style={styles.cardTitle}>{title}</h4>
        <p style={styles.cardDescription}>{description}</p>
      </div>

      <Link to={to} style={{ marginTop: '2rem', alignSelf: 'flex-start', textDecoration: 'none' }}>
        <button
          style={{
            ...styles.cardButton,
            backgroundColor: isButtonHovered ? '#e6f1ff' : 'transparent',
          }}
          onMouseEnter={() => setIsButtonHovered(true)}
          onMouseLeave={() => setIsButtonHovered(false)}
        >
          {buttonText}
        </button>
      </Link>

      <div style={styles.iconWrapper}>
        <Icon />
      </div>
    </div>
  );
}

export default function CatalogosContainer() {
  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <div style={styles.breadcrumb}>
          <a href="/" style={styles.breadcrumbLink}>Inicio</a>
          <span>/</span>
          <span>Catálogos</span>
        </div>
      </div>

      <main style={styles.main}>
        <section style={styles.section}>
          <h3 style={styles.title}>Cuéntanos, ¿Qué necesitas?</h3>

          <div style={styles.grid}>
            <CardItem
              title="Administración de Proveedores"
              description="Gestiona el catálogo de proveedores: crear, editar, eliminar y consultar información."
              to="/catalogos/proveedores"
              buttonText="Ver Proveedores"
              Icon={ProveedoresIcon}
            />
            <CardItem
              title="Bloqueo de Proveedores"
              description="Administra los bloqueos de pago a proveedores por rangos de fecha."
              to="/catalogos/bloqueos"
              buttonText="Ver Bloqueos"
              Icon={BloqueoIcon}
              dark
            />
            <CardItem
              title="Gestión de Catálogos"
              description="Busca, gestiona y consulta catálogos junto con sus elementos y cambios realizados."
              to="/catalogos/catalogs"
              buttonText="Ver Catálogos"
              Icon={CatalogsIcon}
            />
          </div>
        </section>
      </main>
    </div>
  );
}
