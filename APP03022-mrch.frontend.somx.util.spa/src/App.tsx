import '@/App.css';
import React from 'react';
import * as ReactDOMClient from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HashRouter, Route, Routes } from 'react-router-dom';

import { Layout } from '@shared/components/container/Layout';
import UtilContainer from '@/features/maintainers/UtilContainer';
import { ParameterConfigPage } from '@/features/parameters/ParameterConfigPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      refetchOnMount: false,
      refetchOnReconnect: false,
      retry: 1,
      staleTime: 30 * 1000,
    },
  },
});

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <HashRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<UtilContainer />} />
            <Route path="/util" element={<UtilContainer />} />
            <Route path="/util/parametros" element={<ParameterConfigPage />} />
          </Routes>
        </Layout>
      </HashRouter>
    </QueryClientProvider>
  );
};

const isLocalStandalone =
  typeof document !== 'undefined' && !!document.getElementById('root');

let bootstrap = async () => { };
let mount = async () => { };
let unmount = async () => { };

if (!isLocalStandalone) {
  const singleSpaReact = require('single-spa-react').default;

  const lifecycles = singleSpaReact({
    React,
    ReactDOMClient,
    rootComponent: App,
    errorBoundary(err: unknown, errInfo: unknown, props: unknown) {
      console.error('Error in single-spa-react [util] App:', err, errInfo, props);
      return <div>Error loading util module</div>;
    },
  });

  bootstrap = lifecycles.bootstrap;
  mount = lifecycles.mount;
  unmount = lifecycles.unmount;
}

export { bootstrap, mount, unmount };
export default App;