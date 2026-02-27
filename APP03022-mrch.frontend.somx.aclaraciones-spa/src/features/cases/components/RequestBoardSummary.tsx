import { useEffect } from 'react';

interface Props {
  list: { status: number }[];
}

/* ───── Componente ───── */
export default function RequestBoardSummary({ list }: Props) {
  const total = list.length;
  const abiertos = list.filter(r => r.status === 20).length;
  const pendientes = list.filter(r => r.status === 10).length;
  const rechazados = list.filter(r => r.status === 40).length;

  const items = [
    { v: total, lbl: 'Total de casos', bg: '#d6e9ff', fg: '#0062b1' },
    { v: abiertos, lbl: 'Abiertos', bg: '#d9f4e0', fg: '#2e7d32' },
    { v: pendientes, lbl: 'Pendientes', bg: '#fff4cc', fg: '#b79500' },
    { v: rechazados, lbl: 'Rechazados', bg: '#ffd6d9', fg: '#c62828' },
  ];

  /* Inyeccion de estilos */
  useEffect(() => {
    if (document.getElementById('rs-styles')) return;
    const style = document.createElement('style');
    style.id = 'rs-styles';
    style.textContent = css;
    document.head.appendChild(style);
    return () => style.remove();
  }, []);

  return (
    <div className="rs-wrapper">
      {items.map(({ v, lbl, bg, fg }, i) => (
        <div key={lbl} className={`rs-block ${i ? 'with-left-sep' : ''}`}>
          <span className="rs-label">{lbl}:</span>
          <span className="rs-chip" style={{ background: bg, color: fg }}>
            {v}
          </span>
        </div>
      ))}
    </div>
  );
}

/* ───── Estilos ───── */
const css = `
  .rs-wrapper{
    width:100%;
    border:1px solid #dcdfe5;
    border-radius:4px;
    padding:20px 24px;
    display:flex;
    justify-content:flex-start;
    gap:48px;
    box-sizing:border-box;
    overflow-x:auto;
  }

  .rs-block{
    display:flex;
    flex-direction:column;
    align-items:center;
    position:relative;
    min-width:110px;
    gap:6px;
    font-weight:600;
    font-size:15px;
  }

  .rs-block.with-left-sep::before{
    content:'';
    position:absolute;
    left:-24px;
    top:2px; bottom:2px;
    width:1px;
    background:#dcdfe5;
  }

  .rs-label{ white-space:nowrap; }

  .rs-chip{
    min-width:34px;
    height:26px;
    border-radius:4px;
    font-size:14px;
    line-height:26px;
    text-align:center;
  }

  @media(max-width:640px){
    .rs-wrapper{ gap:24px; }
  }
`;
