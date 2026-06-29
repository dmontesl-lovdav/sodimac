import type { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";

export const MAX_MB = 10;
export const MAX_BYTES = MAX_MB * 1024 * 1024;

export const BREADCRUMB: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
  { label: "Notas de Crédito", to: "/fiscal/notas-credito" },
  { label: "Publicar Nota de Crédito" },
];
