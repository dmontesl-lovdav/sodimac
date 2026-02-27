import { AuthToken, Configuration } from "@rtl/mrch.frontend.cross.common-interfaces";

export interface GlobalStoreState {
  authentication: AuthToken;
  configuration: Configuration;
}
