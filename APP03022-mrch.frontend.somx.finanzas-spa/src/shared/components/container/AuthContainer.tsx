import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

function AuthContainer() {
    const auth = ConfigurationBuilder.authenticator;
    // (solo isAdmin / isProveedor).
    void auth.isAdmin();
    return null;
}

export default AuthContainer;