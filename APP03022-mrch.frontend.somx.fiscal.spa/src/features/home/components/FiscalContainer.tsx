import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import FiscalCardsList from './parts/FiscalCardsList';

const styles = {
    container: {
        minHeight: '100vh',
        width: '100%',
        display: 'flex',
        flexDirection: 'column' as const,
        backgroundColor: '#ffffff',
    },
    breadcrumbWrapper: {
        width: '100%',
        padding: '1rem 2rem 0 2rem',
    },
    main: {
        width: '100%',
        backgroundColor: '#ffffff',
        padding: '1.5rem 2rem 3rem 2rem',
    },
    section: {
        border: '1px solid #e5e7eb',
        borderRadius: '0.75rem',
        padding: '1.5rem',
    },
    title: {
        fontSize: '1.5rem',
        fontWeight: 500,
        marginBottom: '1rem',
        color: '#374151',
    },
};

export default function FiscalContainer() {
    return (
        <div style={styles.container}>
            <div style={styles.breadcrumbWrapper}>
                <Breadcrumb items={[{ label: 'Inicio', to: '/' }, { label: 'Fiscal' }]} />
            </div>

            <main style={styles.main}>
                <section style={styles.section}>
                    <h3 style={styles.title}>
                        Cuéntanos, ¿Qué necesitas?
                    </h3>

                    <FiscalCardsList />
                </section>
            </main>
        </div>
    );
}
