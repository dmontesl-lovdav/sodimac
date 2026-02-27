import { localHomeStore } from '../store/localStore';
import jwtDecode, { JwtPayload } from "jwt-decode";

/*
 * The entry point for initiating login and token requests
 */
export default class Authenticator {

    private readonly adminGroup: string;
    private readonly proveedorGroup: string;
    private readonly defaultToken: string;

    public constructor(adminGroup: string, proveedorGroup: string, defaultToken?: string) {
        this.adminGroup = adminGroup;
        this.proveedorGroup = proveedorGroup;
        this.defaultToken = defaultToken ?? "";
    }

    /*
     * Check if the current user is an admin
     */
    public async isAdmin(): Promise<boolean> {

        const token = this.defaultToken !== "" ? this.defaultToken : localHomeStore.getState().authentication.token;

        if (!token) {
            throw new Error('No token');
        }

        const decoded = jwtDecode<JwtPayload>(token);
        return true;
    }

    /*
    * Check if the current user is an external user
    */
    public async isProveedor(): Promise<boolean> {

        const token = this.defaultToken !== "" ? this.defaultToken : localHomeStore.getState().authentication.token;

        if (!token) {
            throw new Error('No token');
        }

        const decoded = jwtDecode<JwtPayload>(token);
        return true;
    }
}
