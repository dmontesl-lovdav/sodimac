import React from 'react';

type Props = {
    children: React.ReactNode;
    fallback?: React.ReactNode;
};

type State = {
    hasError: boolean;
    error?: Error | null;
};

class ErrorBoundary extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            hasError: false,
            error: null,
        };
    }

    static getDerivedStateFromError(error: Error): State {
        return {
            hasError: true,
            error,
        };
    }

    componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
        console.error('Frontend ErrorBoundary caught an error:', error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            return (
                this.props.fallback ?? (
                    <div style={{ padding: '24px', textAlign: 'center' }}>
                        <h2>Ocurrió un error inesperado</h2>
                        <p>No fue posible cargar esta vista.</p>
                    </div>
                )
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;