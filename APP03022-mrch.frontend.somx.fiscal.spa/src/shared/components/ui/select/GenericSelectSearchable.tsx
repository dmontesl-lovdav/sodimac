import { useEffect, useMemo, useRef, useState, type ChangeEvent } from "react";
import "./styles/GenericSelectSearchable.css";
import "./styles/GenericSelect.css";

type Option = {
  value: string;
  label: string;
};

function findSelectedOption(value: string, options: Option[]): Option | undefined {
  return options.find((o) => String(o.value) === String(value));
}

function isOptionSelected(opt: Option, value: string): boolean {
  return String(opt.value) === String(value);
}

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
    const found = findSelectedOption(value, options);
    setQuery(found ? found.label : "");
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
            <button
              key={opt.value}
              type="button"
              className="gss-option"
              aria-selected={isOptionSelected(opt, value)}
              onClick={() => handleSelect(opt)}
            >
              {opt.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
