import React from 'react';

type DemoCardProps = {
    title: string;
    desc?: string;
    right?: React.ReactNode;
    children: React.ReactNode;
    id?: string; // para ancla directa (#buttons, #inputs, etc.)
};

export default function DemoCard({ title, desc, right, children, id }: DemoCardProps) {
    return (
        <section id={id} className="scroll-mt-24">
            <div className="bg-white/90 backdrop-blur border border-slate-200 rounded-lg shadow-sm">
                <div className="flex items-start justify-between p-4 border-b border-slate-100">
                    <div>
                        <h3 className="text-base font-semibold text-slate-900">{title}</h3>
                        {desc && <p className="text-sm text-slate-600 mt-1">{desc}</p>}
                    </div>
                    {right && <div className="ml-4">{right}</div>}
                </div>
                <div className="p-5">{children}</div>
            </div>
        </section>
    );
}
