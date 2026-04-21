interface IconButtonProps {
    icon: string;
    title: string;
    onClick: () => void;
    disabled?: boolean;
}
export const IconButton: React.FC<IconButtonProps> = ({ icon, title, onClick, disabled = false }) => {
    return (
        <button
            title={title}
            onClick={onClick}
            className="gt-action-btn"
            disabled={disabled}
        >
            <img src={icon} alt={title} className="gt-action-icon" />
        </button>
    );
}