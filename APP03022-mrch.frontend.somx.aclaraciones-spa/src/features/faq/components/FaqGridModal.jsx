import React from 'react';
import '../styles/FaqGridContainer.css';

export default function FaqGridModal({ faq, onClose }) {
    return (
        <div className="faq-modal-overlay" onClick={onClose}>
            <div
                className="faq-modal-dialog"
                onClick={(e) => e.stopPropagation()}
            >
                <button className="faq-modal-close" onClick={onClose}>
                    ×
                </button>

                <h4 className="faq-modal-title">{faq.question}</h4>

                <p className="faq-modal-text">
                    {faq.answer}
                </p>
            </div>
        </div>
    );
}
