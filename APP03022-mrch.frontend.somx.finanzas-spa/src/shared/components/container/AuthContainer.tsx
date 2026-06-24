import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

function AuthContainer() {
    const auth = ConfigurationBuilder.authenticator;
    // (solo isAdmin / isProveedor).
    auth.isAdmin();
    return null;
}

export default AuthContainer;