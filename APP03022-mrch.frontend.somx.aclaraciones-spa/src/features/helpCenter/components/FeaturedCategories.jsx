import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCategories } from '@/features/categories/api';
import categoryImg from '@assets/category.png';

import './styles/FeaturedCategories.css';

export default function FeaturedCategories() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        (async () => {
            try {
                const data = await getCategories({ includeIcons: true, active: 'true' });
                const actives = (Array.isArray(data?.content) ? data.content : [])
                    .filter((c) => c.isActive);
                setItems(actives.slice(0, 3));
            } catch (e) {
                console.error('No se pudieron cargar categorías destacadas', e);
                setItems([]);
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    const base64ToBlob = (base64, mimeType) => {
        const byteChars = atob(base64);
        const byteNumbers = new Array(byteChars.length);
        for (let i = 0; i < byteChars.length; i++) byteNumbers[i] = byteChars.charCodeAt(i);
        return new Blob([new Uint8Array(byteNumbers)], { type: mimeType });
    };

    const drawIcon = (canvas, iconBase64, iconName) => {
        if (!canvas || !iconBase64) return;
        const mime = iconName?.toLowerCase().endsWith('.png') ? 'image/png' : 'image/jpeg';
        const blob = base64ToBlob(iconBase64, mime);
        createImageBitmap(blob)
            .then((bmp) => {
                const ctx = canvas.getContext('2d');
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                const size = Math.min(canvas.width, canvas.height);
                const ratio = Math.min(size / bmp.width, size / bmp.height);
                const w = bmp.width * ratio;
                const h = bmp.height * ratio;
                const x = (canvas.width - w) / 2;
                const y = (canvas.height - h) / 2;
                ctx.drawImage(bmp, x, y, w, h);
            })
            .catch((err) => console.error('Error dibujando ícono:', err));
    };

    const goToCategory = (id) =>
        navigate(`/help-center/faqs/category?categoryId=${id}`);

    return (
        <section className="hfc-section">
            <h3 className="hfc-title">Categorías más consultadas</h3>

            {loading ? (
                <div className="hfc-grid">
                    {[0, 1, 2].map((i) => (
                        <div key={i} className="hfc-skeleton" />
                    ))}
                </div>
            ) : items.length === 0 ? (
                <div className="hfc-empty">No hay categorías para mostrar.</div>
            ) : (
                <div className="hfc-grid">
                    {items.map((c) => (
                        <div
                            key={c.id}
                            className="hfc-card"
                            onClick={() => goToCategory(c.id)}
                            title={`Ver preguntas frecuentes de ${c.name}`}
                        >
                            {c.icon ? (
                                <canvas
                                    width="64"
                                    height="64"
                                    className="hfc-icon"
                                    ref={(canvas) => drawIcon(canvas, c.icon, c.iconName)}
                                />
                            ) : (
                                <img
                                    src={categoryImg}
                                    alt=""
                                    className="hfc-icon-img"
                                />
                            )}
                            <div className="hfc-card-body">
                                <h4 className="hfc-card-title">{c.name}</h4>
                                {c.description && (
                                    <p className="hfc-card-desc">{c.description}</p>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </section>
    );
}
