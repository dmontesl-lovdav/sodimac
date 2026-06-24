import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import { ReceptionStatusOptions } from "../../interfaces";
import "./ReceptionPillStatus.css";

interface OrderPillStatusProps {
    status: number
}
export const OrderPillStatus = ({status}: OrderPillStatusProps) => {
    const filtered = ReceptionStatusOptions.filter((option) => { return option.value === Number(status) })
    const option =
        filtered && filtered.length > 0
            ? filtered[0]
            : ({ type: "error", label: "Desconocido", description: "Desconocido" } as const);
    return (
        <div className="rc-pill-status">
            <StatusPill type={option.type}>{option.description ?? option.label}</StatusPill>
        </div>
    );
};