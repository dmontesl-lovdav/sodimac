import { APP_DEV } from "./shared/constants/environment";
import { RootState } from "./store/localStore";
import { useAppSelector } from "./store/hooks/useAppSelector";
import React from "react";

export interface PrivateRouteProps {
  children: React.JSX.Element;
}

const PrivateRoute = ({ children }: PrivateRouteProps): React.JSX.Element | null => {
  const isLogged = useAppSelector(
    ({ authentication }: RootState) => authentication.isLogged
  );

  if (APP_DEV) {
    return children;
  }

  if (!isLogged) {
    return <div />;
  }

  return children;
};
export default PrivateRoute;
