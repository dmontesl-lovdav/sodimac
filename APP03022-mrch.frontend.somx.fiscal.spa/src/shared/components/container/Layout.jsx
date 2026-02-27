const styles = {
    outer: {},
    inner: {
        padding: '2rem',
        backgroundColor: '#f9fafb',
    },
};

export const Layout = ({ children }) => {
    return (
        <div style={styles.outer}>
            <div style={styles.inner}>{children}</div>
        </div>
    );
};
