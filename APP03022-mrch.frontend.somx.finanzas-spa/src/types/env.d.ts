declare const process: {
    env: {
        NODE_ENV?: string;
        API_BASE_URL?: string;
        REACT_APP_AUTH_DEFAULT_TOKEN?: string;
        AUTH_DEFAULT_TOKEN?: string;
        [key: string]: string | undefined;
    };
};