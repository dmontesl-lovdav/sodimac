import type { FC } from 'react';
import emptyStateImage from '@/shared/images/lobby.svg';

export const EmptyState: FC = () => {
  return (
    <section className="parameters-empty-state">
      <img
        src={emptyStateImage}
        alt=""
        className="parameters-empty-state__image"
      />
      <p className="parameters-empty-state__message">
        Para visualizar los parámetros, por favor utilice los filtros y presione
        &apos;Buscar&apos;
      </p>
    </section>
  );
};
