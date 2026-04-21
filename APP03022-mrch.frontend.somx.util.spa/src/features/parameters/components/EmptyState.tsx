import { FC } from 'react';
import lobbyImg from '@/shared/images/lobby.svg';

import '../styles/ParameterContainer.css';

export const EmptyState: FC = () => {
  return (
    <div className="param-empty">
      <img src={lobbyImg} alt="" />
      <p>Realiza una búsqueda para ver los parámetros configurados.</p>
    </div>
  );
};
