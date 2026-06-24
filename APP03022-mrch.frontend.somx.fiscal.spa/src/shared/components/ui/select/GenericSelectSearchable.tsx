import { useEffect, useMemo, useRef, useState, type ChangeEvent } from "react";
import "./styles/GenericSelectSearchable.css";
import "./styles/GenericSelect.css";

type Option = {
  value: string;
  label: string;
};

type Props = {
  value: string;
  onChange: (e: { target: { value: string } }) => void;
  options: Option[];
  placeholder?: string;
  widthClass?: string;
  containerClassName?: string;
};

export default function GenericSelectSearchable({
  value,
  onChange,
  options = [],
  placeholder = "Buscar…",
  widthClass = "gs-100",
  containerClassName = "",
}: Props) {
  const [open, setOpen] = useState<boolean>(false);
  const [query, setQuery] = useState<string>("");
  const ref = useRef<HTMLDivElement>(null);

  const filtered = useMemo<Option[]>(() => {
    if (!query) return options;
    return options.filter((o) =>
      o.label.toLowerCase().includes(query.toLowerCase())
    );
  }, [query, options]);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (!ref.current?.contains(e.target as Node)) {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  useEffect(() => {
    if (!value) {
      setQuery("");
    } else {
      const found = options.find((o) => String(o.value) === String(value));
      if (found) setQuery(found.label);
    }
  }, [value, options]);

  const handleSelect = (opt: Option) => {
    onChange({ target: { value: opt.value } });
    setQuery(opt.label);
    setOpen(false);
  };

  const handleClear = () => {
    setQuery("");
    onChange({ target: { value: "" } });
    setOpen(false);
  };

  return (
    <div
      ref={ref}
      className={`generic-select-searchable ${widthClass} ${containerClassName}`.trim()}
    >
      <input
        value={query}
        placeholder={placeholder}
        onChange={(e: ChangeEvent<HTMLInputElement>) => {
          setQuery(e.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        className="gss-input"
      />

      {query && (
        <button
          type="button"
          className="gss-clear"
          onClick={handleClear}
          aria-label="Limpiar"
        >
          ✕
        </button>
      )}

      <span className="generic-select-caret">▾</span>

      {open && (
        <div className="gss-dropdown">
          {filtered.length === 0 && (
            <div className="gss-empty">Sin resultados</div>
          )}

          {filtered.map((opt) => (
            <div
              key={opt.value}
              className="gss-option"
              onClick={() => handleSelect(opt)}
            >
              {opt.label}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
