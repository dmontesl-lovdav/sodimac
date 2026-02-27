import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import Breadcrumb from '@/shared/components/ui/navigation/Breadcrumb';
import { GenericButton, GenericInput, GenericLinearProgress } from '@shared/components/ui';

import {
    getFeedbackById,
    createFeedback,
    updateFeedback,
} from '@/features/feedback/api';

import deleteIcon from '@assets/delete.svg';

import '../styles/AddEditFeedbackForm.css';

export default function AddEditFeedbackForm() {
    const { id: idFromRoute } = useParams();
    const location = useLocation();
    const nav = useNavigate();

    // --- origen desde Mantenedor ---
    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });

    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    const STATE = { LOADED: 1, LOADING: 2 };
    const [ui, setUI] = useState(STATE.LOADING);

    // form
    const [question, setQuestion] = useState('');
    const [answers, setAnswers] = useState([{ text: '' }]);

    /* load in edit */
    useEffect(() => {
        (async () => {
            try {
                setUI(STATE.LOADING);
                if (idFromRoute) {
                    const data = await getFeedbackById(+idFromRoute);
                    setQuestion(data?.question ?? '');
                    setAnswers(
                        Array.isArray(data?.answers) && data.answers.length
                            ? data.answers.map((a) => ({ text: a?.text ?? '' }))
                            : [{ text: '' }]
                    );
                } else {
                    setQuestion('');
                    setAnswers([{ text: '' }]);
                }
            } finally {
                setUI(STATE.LOADED);
            }
        })();
    }, [idFromRoute]);

    useEffect(() => {
        const handler = () => {
            if (fromMaintainer) {
                nav(withOrigin('/feedback'), { state: stateOrigin });
            } else {
                nav(-1);
            }
        };

        window.addEventListener("country-changed", handler);
        return () => window.removeEventListener("country-changed", handler);
    }, [fromMaintainer, nav, stateOrigin]);

    /* helpers */
    const formValid =
        question.trim().length > 3 && answers.some((a) => a.text.trim().length > 0);

    const addAnswer = () => setAnswers((arr) => [...arr, { text: '' }]);

    const removeAnswer = (idx) =>
        setAnswers((arr) => (arr.length <= 1 ? [{ text: '' }] : arr.filter((_, i) => i !== idx)));

    const updateAnswer = (idx, value) =>
        setAnswers((arr) => arr.map((a, i) => (i === idx ? { text: value } : a)));

    async function handleSave() {
        try {
            setUI(STATE.LOADING);

            const payload = {
                question: question.trim(),
                answers: answers
                    .map((a) => a.text.trim())
                    .filter((t) => t.length > 0)
                    .map((t, i) => ({ text: t, position: i })),
            };

            if (idFromRoute) await updateFeedback(+idFromRoute, payload);
            else await createFeedback(payload);

            if (fromMaintainer) {
                nav(withOrigin('/feedback'), { state: stateOrigin });
            } else {
                nav(-1);
            }
        } catch (e) {
            console.error('Save failed', e);
            setUI(STATE.LOADED);
        }
    }

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Feedback preguntas frecuentes', to: withOrigin('/feedback') },
            { label: idFromRoute ? 'Editar feedback' : 'Agregar feedback' },
        ]
        : [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Feedback preguntas frecuentes', to: '/feedback' },
            { label: idFromRoute ? 'Editar feedback' : 'Agregar feedback' },
        ];

    return (
        <div className="ff-layout">
            {/* breadcrumb */}
            <div className="ff-breadcrumb">
                <Breadcrumb items={breadcrumbItems} />
            </div>

            {/* contenedor principal */}
            <div className="ff-box">
                <div className="ff-inner">
                    {ui === STATE.LOADING && (
                        <GenericLinearProgress indeterminate fullWidth className="ff-loader" />
                    )}

                    {ui === STATE.LOADED && (
                        <div className="ff-content">
                            <h2 className="ff-title">
                                {question?.trim() || 'Nueva pregunta de feedback'}
                            </h2>

                            <GenericInput
                                value={question}
                                onChange={(e) => setQuestion(e.target.value)}
                                name="question"
                                label="Pregunta"
                                maxLength={160}
                                placeholder="¿Por qué no te fue útil la información?"
                                className="ff-input"
                            />

                            {/* botón agregar respuesta */}
                            <div className="ff-add-row">
                                <GenericButton variant="outline" onClick={addAnswer}>
                                    Agregar respuesta
                                </GenericButton>
                            </div>

                            {/* respuestas */}
                            <div className="ff-answers">
                                {answers.map((a, idx) => (
                                    <div key={idx} className="ff-answer-row">
                                        <div className="ff-answer-input">
                                            <GenericInput
                                                value={a.text}
                                                onChange={(e) => updateAnswer(idx, e.target.value)}
                                                name={`answer_${idx}`}
                                                label={`Respuesta ${idx + 1}`}
                                                maxLength={160}
                                                placeholder={`Respuesta ${idx + 1}`}
                                            />
                                        </div>

                                        <button
                                            aria-label="Eliminar respuesta"
                                            onClick={() => removeAnswer(idx)}
                                            className="ff-delete-btn"
                                            type="button"
                                            title="Eliminar respuesta"
                                        >
                                            <img src={deleteIcon} alt="" className="ff-delete-icon" />
                                        </button>
                                    </div>
                                ))}
                            </div>

                            {/* footer */}
                            <div className="ff-footer">
                                <GenericButton
                                    variant="text"
                                    onClick={() =>
                                        fromMaintainer
                                            ? nav(withOrigin('/feedback'), { state: stateOrigin })
                                            : nav(-1)
                                    }
                                >
                                    Volver
                                </GenericButton>

                                <GenericButton
                                    variant="primary"
                                    disabled={!formValid}
                                    onClick={handleSave}
                                >
                                    Guardar
                                </GenericButton>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
