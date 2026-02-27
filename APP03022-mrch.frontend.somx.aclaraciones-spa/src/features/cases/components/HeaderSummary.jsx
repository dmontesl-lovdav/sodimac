export default function HeaderSummary({ headers }) {

    function buildCells() {
        if (!headers || !headers.length || headers.length === 0) {
            return;
        }

        return headers.map((cell, index) => {
            let _class = "m-5 pr-5";
            _class += index < (headers.length - 1) ? " border-gray-200 border-r-1" : "";

            return (
                <div class={_class}>
                    <div>{cell.name}</div>
                    <div class="font-bold">{cell.value}</div>
                </div>
            );
        }
        );
    }

    return (
        <div class="mt-5 flex border-2 border-solid border-gray-200 rounded-sm">
            {buildCells()}
        </div>
    );
}