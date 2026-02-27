import { useEffect, useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import categoryImg from '@assets/category.png';
import { GenericModal } from '@shared/components/ui';
import { getCategories } from '@/features/categories/api';

import './styles/AllCategories.css';

export default function AllCategories({ devTestingRoutes }) {
    const [categories, setCategories] = useState([]);
    const [visibleCount, setVisibleCount] = useState(4);
    const [previewFile, setPreviewFile] = useState(null); // se mantiene por si se usa en otro lado
    const [opLoading, setOpLoading] = useState(false);
    const previewCanvas = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        (async () => {
            try {
                setOpLoading(true);
                const data = await getCategories({ includeIcons: true, active: 'true' });
                setCategories(
                    (Array.isArray(data?.content) ? data.content : [])
                        .filter((c) => c.isActive)
                );
            } catch (err) {
                console.error('Error al cargar categorías:', err);
            } finally {
                setOpLoading(false);
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

    const handleShowMore = () => {
        const total = categories.length;
        const step = 4;
        if (visibleCount < total) setVisibleCount((prev) => Math.min(total, prev + step));
    };

    const handleShowLess = () => {
        const step = 4;
        if (visibleCount > 4) setVisibleCount((prev) => Math.max(4, prev - step));
    };

    const handleCategoryClick = (id) => {
        navigate(`/help-center/faqs/category?categoryId=${id}`);
    };

    return (
        <>
            <section className="hac-section">
                <div className="hac-header">
                    <h3 className="hac-title">Todas las categorías</h3>

                    {devTestingRoutes && (
                        <>
                            <Link to="/categories">
                                <button type="button" className="hac-header-btn">
                                    Ver categorías
                                </button>
                            </Link>
                            <Link to="/mantenedor">
                                <button type="button" className="hac-header-btn">
                                    Mantenedores
                                </button>
                            </Link>
                            {/* <Link to="/playground/ui">
                                <button type="button" className="hac-header-btn">
                                    UI Playground
                                </button>
                            </Link> */}
                        </>
                    )}
                </div>

                <div className="hac-grid" style={{ overflow: 'hidden' }}>
                    {categories.slice(0, visibleCount).map((c) => (
                        <div
                            key={c.id}
                            className="hac-card"
                            onClick={() => handleCategoryClick(c.id)}
                            title={`Ver preguntas frecuentes de ${c.name}`}
                        >
                            {c.icon ? (
                                <canvas
                                    width="64"
                                    height="64"
                                    className="hac-card-icon"
                                    ref={(canvas) => drawIcon(canvas, c.icon, c.iconName)}
                                />
                            ) : (
                                <img
                                    src={categoryImg}
                                    alt={c.name}
                                    className="hac-card-img"
                                />
                            )}
                            <span className="hac-card-name">{c.name}</span>
                        </div>
                    ))}
                </div>

                {categories.length > 4 && (
                    <div className="hac-controls">
                        <button
                            type="button"
                            onClick={handleShowLess}
                            disabled={visibleCount <= 4}
                            className={`hac-control-btn ${visibleCount <= 4 ? 'hac-control-btn-disabled' : ''
                                }`}
                        >
                            Ver menos
                        </button>
                        <button
                            type="button"
                            onClick={handleShowMore}
                            disabled={visibleCount >= categories.length}
                            className={`hac-control-btn ${visibleCount >= categories.length
                                ? 'hac-control-btn-disabled'
                                : ''
                                }`}
                        >
                            Ver más
                        </button>
                    </div>
                )}
            </section>

            <GenericModal
                visible={opLoading}
                variant="loading"
                message="Cargando categorías…"
            />
        </>
    );
}
