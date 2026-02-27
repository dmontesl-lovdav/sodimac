import FiscalCard from './FiscalCard';
import { InvoiceIcon, ComplementIcon, SearchIcon, DocIcon } from './FiscalIcons';

const styles = {
    grid: {
        display: 'grid',
        gap: '2.5rem',
        gridTemplateColumns: 'repeat(1, 1fr)',
    },
};

// Media query simulation through responsive grid
const getGridStyle = () => {
    const width = typeof window !== 'undefined' ? window.innerWidth : 1024;
    if (width >= 1024) {
        return { ...styles.grid, gridTemplateColumns: 'repeat(3, 1fr)' };
    }
    if (width >= 768) {
        return { ...styles.grid, gridTemplateColumns: 'repeat(2, 1fr)' };
    }
    return styles.grid;
};

export default function FiscalCardsList() {
    return (
        <div style={getGridStyle()}>
            <FiscalCard
                title="Facturas"
                description="Consulta facturas publicadas, descarga XML/PDF y gestiona documentos fiscales."
                to="/fiscal/facturas"
                buttonText="Ver facturas"
                Icon={InvoiceIcon}
            />

             <FiscalCard
                title="Notas de Crédito"
                description="Visualiza las notas de crédito registradas."
                to="/fiscal/notas-credito"
                buttonText="Ver notas de Crédito"
                Icon={DocIcon}
            />

            

            <FiscalCard
                title="Consulta de Complementos"
                description="Consulta el historial de complementos publicados y su estatus de validación."
                to="/fiscal/consulta-complemento-pago"
                buttonText="Ver complementos"
                Icon={SearchIcon}
            />
        </div>
    );
}
