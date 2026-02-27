import documentIcon from "@assets/document.svg";
import paymentIcon from "@assets/payment.svg";
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { Link } from 'react-router-dom';

interface FinanzasCard {
    title: string,
    description: string,
    link: string,
    caption: string,
    icon?: any,
}

const DEFAULT_CARDS: FinanzasCard[] = [
    {
        title: "Listado de Recepciones",
        description: "Consulta las recepciones de las órdenes de servicio y su estatus.",
        link: "/recepciones",
        caption: "Ver Recepciones",
        icon: documentIcon
    },
    {
        title: "Pagos a Proveedores",
        description: "Consulta y gestiona pagos realizados, carga complementos fiscales y exporta información.",
        link: "/",
        caption: "Ver pagos",
        icon: paymentIcon
    },
    {
        title: "Listado de Guías",
        description: "Visualiza las guías de embarques registradas.",
        link: "/guias",
        caption: "Ver Guías",
    },
];

function decorateIcon(icon: any) {
    return (
        <div className="absolute right-6 top-1/2 -translate-y-1/2 h-24 w-24 pointer-events-none select-none opacity-90">
            <img src={icon} width={"64px"} className="opacity-75" />
        </div>
    );
}

function buildCard(card: FinanzasCard, index: number) {
    return (
        <div key={index} className="relative flex flex-col justify-between rounded-2xl border border-[#D9E6F5] bg-[#f3f6f9] p-8 md:p-9 shadow-sm hover:shadow-md transition-shadow">
            <div className={card.icon ? "pr-28 lg:pr-36" : ""}>
                <h4 className="font-medium text-xl text-[#333333]">{card.title}</h4>
                <p className="text-sm text-gray-700 mt-3 max-w-md">
                    {card.description}
                </p>
            </div>

            <Link to={card.link} className="mt-8 self-start">
                <button className="px-5 py-2 rounded border border-[#003865] text-[#003865] text-sm font-medium hover:bg-[#e6f1ff] cursor-pointer">
                    {card.caption}
                </button>
            </Link>
                {card.icon && decorateIcon(card.icon)}
        </div>
    );
}

export default function FinanzasContainer({ cards = DEFAULT_CARDS }: { cards?: FinanzasCard[] }) {
    return (
        <div className="min-h-screen w-full flex flex-col bg-white">
            <div className="w-full px-8 pt-4">
                <Breadcrumb items={[{ label: 'Inicio', to: '/' }, { label: 'Finanzas' }]} />
            </div>

            <main className="w-full bg-white px-8 pt-6 pb-12">
                <section className="space-y-6 border border-gray-200 rounded-xl p-6">
                    <h3 className="text-2xl font-medium mb-4 text-gray-700">Cuéntanos, ¿Qué necesitas?</h3>
                    <div className="grid gap-10 lg:grid-cols-3">
                        {cards.map((card: FinanzasCard, index: number) => buildCard(card, index))}
                    </div>
                </section>
            </main>
        </div>
    );
}