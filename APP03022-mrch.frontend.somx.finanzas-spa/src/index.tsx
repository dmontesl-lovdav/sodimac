import './styles.css';
import './App.css';

import { createRoot } from 'react-dom/client';
import { HashRouter } from 'react-router-dom';
import { Provider } from 'react-redux';

import App from './App';
import { localHomeStore } from './store/localStore';

const rootEl = document.getElementById('root')!;
const root = createRoot(rootEl);

root.render(
    <Provider store={localHomeStore}>
        <HashRouter>
            <App />
        </HashRouter>
    </Provider>
);
