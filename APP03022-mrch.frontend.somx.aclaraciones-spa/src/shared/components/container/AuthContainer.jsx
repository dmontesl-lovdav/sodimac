import ConfigurationBuilder from '@/components/configuration/ConfigurationBuilder';

function AuthContainer() {

    const auth = ConfigurationBuilder.authenticator;
    auth.handleLoginResponse();

}

export default AuthContainer;
