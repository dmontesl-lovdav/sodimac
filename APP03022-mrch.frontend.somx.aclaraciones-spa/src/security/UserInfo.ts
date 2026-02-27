/*
 * The UI also has an entity for user info which derives from the contents of the id token
 */
export interface UserInfo {
    name?: string;
    email?: string;
    groups?: string[];
}