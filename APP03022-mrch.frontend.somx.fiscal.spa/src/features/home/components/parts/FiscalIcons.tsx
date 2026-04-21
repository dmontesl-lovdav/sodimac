export function SearchIcon(): React.ReactElement {
  return (
    <svg viewBox="0 0 24 24" className="fiscal-icon-svg" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="11" cy="11" r="8" />
      <path d="M21 21l-4.35-4.35" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function DocIcon(): React.ReactElement {
  return (
    <svg viewBox="0 0 24 24" className="fiscal-icon-svg" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M7 3h6l4 4v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z" />
      <path d="M13 3v5h5" />
      <path d="M8 13h8M8 17h8M8 9h4" />
    </svg>
  );
}

export function InvoiceIcon(): React.ReactElement {
  return (
    <svg viewBox="0 0 24 24" className="fiscal-icon-svg" fill="none" stroke="currentColor" strokeWidth="1.5">
      <rect x="3" y="3" width="18" height="18" rx="2" />
      <path d="M3 9h18" />
      <path d="M9 3v18" />
    </svg>
  );
}

export function AddIcon(): React.ReactElement {
  return (
    <svg viewBox="0 0 24 24" className="fiscal-icon-svg" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M12 5v14M5 12h14" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
