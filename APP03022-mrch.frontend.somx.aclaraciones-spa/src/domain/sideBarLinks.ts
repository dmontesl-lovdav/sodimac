import { ParcelConfigObject } from "single-spa";

export interface SidebarProductsLink {
  rolesAccepted: string[];
  path: Promise<ParcelConfigObject>;
  jestMockPath: string;
}
