import { useState } from 'react';
import { Link } from 'react-router-dom';
import { APP_KEYS, PermissionGate } from '@shared/security';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

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
    borderTop: '1px solid #e5e7eb',
    padding: '1.5rem 0 0 0',
  },
  title: {
    fontSize: '1.75rem',
    lineHeight: '2rem',
    fontWeight: 500,
    color: '#262626',
    margin: 0,
    marginBottom: '1.25rem',
  },
  grid: {
    display: 'grid',
    gap: '1.5rem',
    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
  },
  card: {
    display: 'block',
    width: '100%',
    borderRadius: '0.75rem',
    padding: '1.25rem',
    textDecoration: 'none',
    border: '1px solid #E6E8EB',
    backgroundColor: '#ffffff',
    cursor: 'pointer',
    color: 'inherit',
    transition: 'box-shadow 0.2s ease',
  },
  cardContent: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: '1rem',
  },
  cardIcon: {
    width: '48px',
    height: '48px',
    flexShrink: 0,
    color: '#003865',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  cardRight: {
    flex: 1,
    minWidth: 0,
  },
  cardTitle: {
    fontSize: '1rem',
    lineHeight: 1.5,
    fontWeight: 500,
    color: '#1F2937',
    margin: 0,
  },
  cardDesc: {
    marginTop: '0.25rem',
    fontSize: '0.875rem',
    color: '#4B5563',
    lineHeight: 1.5,
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
  Icon: React.FC;
}

function CardItem({ title, description, to, Icon }: Readonly<CardItemProps>) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <Link
      to={to}
      style={{
        ...styles.card,
        boxShadow: isHovered ? '0px 2px 4px rgba(0,0,0,0.08)' : 'none',
      }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div style={styles.cardContent}>
        <div style={styles.cardIcon}>
          <Icon />
        </div>
        <div style={styles.cardRight}>
          <h3 style={styles.cardTitle}>{title}</h3>
          <p style={styles.cardDesc}>{description}</p>
        </div>
      </div>
    </Link>
  );
}

export default function CatalogosHubPage() {
  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <Breadcrumb
          items={withFinanceBreadcrumb([
            { label: 'Gestión de Catálogos' },
          ])}
        />
      </div>

      <main style={styles.main}>
        <section style={styles.section}>
          <h1 style={styles.title}>Gestión de Catálogos</h1>

          <div style={styles.grid}>
            <PermissionGate app={APP_KEYS.SUPPLIERS_CATALOG}>
              <CardItem
                title="Administración de Proveedores"
                description="Gestiona el catálogo de proveedores: crear, editar, eliminar y consultar información."
                to="/util/catalogos/proveedores"
                Icon={ProveedoresIcon}
              />
            </PermissionGate>
            <PermissionGate app={APP_KEYS.SUPPLIER_BLOCKS}>
              <CardItem
                title="Bloqueo de Proveedores"
                description="Administra los bloqueos de pago a proveedores por rangos de fecha."
                to="/util/catalogos/bloqueos"
                Icon={BloqueoIcon}
              />
            </PermissionGate>
            <PermissionGate app={APP_KEYS.CATALOGS_CATALOG}>
              <CardItem
                title="Catálogo de Catálogos"
                description="Busca, gestiona y consulta catálogos junto con sus elementos y cambios realizados."
                to="/util/catalogos/catalogs"
                Icon={CatalogsIcon}
              />
            </PermissionGate>
          </div>
        </section>
      </main>
    </div>
  );
}
