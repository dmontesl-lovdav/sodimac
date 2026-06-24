declare module '*.css';
declare module '*.scss';
declare module '*.sass';

declare module '*.png' {
  const src: string;
  export default src;
}

declare module '*.jpg' {
  const src: string;
  export default src;
}

declare module '*.jpeg' {
  const src: string;
  export default src;
}

declare module '*.gif' {
  const src: string;
  export default src;
}

declare module '*.svg' {
  const src: string;
  export default src;
}

declare const process: {
  env: {
    NODE_ENV?: string;
    API_URL?: string;
    BACKEND_URL?: string;
    APP_URL?: string;
    API_BASE_URL?: string;
    REACT_APP_API_BASE_URL?: string;
    REACT_APP_AUTH_DEFAULT_TOKEN?: string;
    AUTH_DEFAULT_TOKEN?: string;
    SECURITY_ADMIN_PROFILE_KEYS?: string;
    UTIL_CURRENT_USER_KEY?: string;
    FBC_HOME?: string;
  };
};

declare const require: {
  (moduleName: string): any;
};