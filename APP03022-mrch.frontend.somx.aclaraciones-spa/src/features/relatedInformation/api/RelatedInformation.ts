/* Interface que usa todo el front-end */
export default interface RelatedInformation {
    id?: number;
    title: string;
    link: string;
    image?: string | null;   // Base-64 o null
    imageName?: string;      // nombre del archivo (jpg, png…)
    isActive?: boolean;
}
