import { StatusPill, types } from "@/shared/components/ui/statusPill/StatusPill";
import { ReceptionStatusOptions } from "../../interfaces";

interface OrderPillStatusProps {
    status: number
}
export const OrderPillStatus = ({status}: OrderPillStatusProps) => {
    const filtered = ReceptionStatusOptions.filter((option) => { return option.value === Number(status) })
    const option = filtered && filtered.length > 0 ? filtered[0] : { type: "error", label: "Desconocido" };
    return (<StatusPill type={option.type as types}>{option.label}</StatusPill>);
};