
export default function GenericSelect({
  value,
  onChange,
  options = [],
  placeholder = 'Select…',
  disablePlaceholder = false,
  widthClass = 'somx-w-40',
  selectClassName = '',
  containerClassName = '',
  ...props
}) {
  return (
    <div className={`somx-select-wrapper ${containerClassName}`}>
      <select
        value={value}
        onChange={onChange}
        className={`somx-select ${widthClass} ${selectClassName} ${value === "" ? "withOpaque" : ""}`}
        {...props}
      >
        {placeholder && (
          <option value="" disabled={disablePlaceholder}>
            {placeholder}
          </option>
        )}
        {options.map(({ value: v, label }) => (
          <option key={v} value={v}>
            {label}
          </option>
        ))}
      </select>

      <span className="somx-select-caret">▾</span>
    </div>
  );
}