
export const localDeployment = (process.env.AUTH_DEFAULT_TOKEN ?? '') !== '';

const ConfigurationBuilder = {
    localDeployment,
} as const;

export default ConfigurationBuilder;

