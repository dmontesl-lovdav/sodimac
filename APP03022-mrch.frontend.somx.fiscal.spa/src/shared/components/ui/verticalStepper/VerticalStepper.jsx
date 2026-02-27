import stepperCaption from './StepperCaption.svg';

export function Step({ children }) {
    return (
        <div>{children}</div>
    );
};

export function VerticalStepper({ children }) {
    if (!children || children.length === 0) {
        return (<></>);
    }

    function renderBorder(index, length) {
        if (index < children.length - 1) {
            return (<div style={{ width: "1px", height: "100%", marginLeft: "15px", borderLeft: "#0D9BD3 solid 1px" }}></div>)
        }
        return (<></>);
    }


    return (<div>{
        children.map((child, index) => {
            return (
                <div style={{ display: 'flex' }} key={index}>
                    <div style={{ marginRight: "1.5em" }}>
                        <div style={{ backgroundColor: "white", backgroundImage: `url(${stepperCaption})`, fontWeight: 700, paddingTop: "0.2em", color: "white", textAlign: "center", width: "32px", height: "32px" }}>
                            {index + 1}
                        </div>
                        {renderBorder(index, children.length)}
                    </div>
                    <div style={{ width: "100%" }}>
                        {child}
                    </div>
                </div>)
        })}</div>
    );
}