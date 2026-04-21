import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { localHomeStore } from './store/localStore';
import App from './App';

createRoot(document.getElementById('root')!).render(
  <Provider store={localHomeStore}>
    <StrictMode>
      <App />
    </StrictMode>
  </Provider>,
);