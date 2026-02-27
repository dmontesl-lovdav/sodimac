export const DATE_FORMAT_EXPR = "^20[0-9]{2}-[0-9]{2}-[0-9]{2}$";
export const DATE_PROTO = "2000-00-00";

export const requestStatus = [
    { "id": 10, "description": "Sin atender", "class": "warning" },
    { "id": 20, "description": "En atención", "class": "info" },
    { "id": 30, "description": "Resuelto", "class": "success" },
    { "id": 40, "description": "Cancelado", "class": "error" },
    { "id": 50, "description": "Rechazado", "class": "error" },
];

export function byCreationTimeSorter(l, r) {
    if (!l || !r) {
        return 0;
    }
    if (!l.creationTime || !r.creationTime) {
        return 0;
    }
    return r.creationTime > l.creationTime ? 1 : -1;
}

export function extractDate(value) {
    if (!value || !value.match(DATE_FORMAT_EXPR)) {
        return;
    }

    value = value.split("-");
    return new Date(parseInt(value[0]), parseInt(value[1]) - 1, parseInt(value[2]));
}

export function translateDate(value) {
    if (!value) {
        return "---";
    }
    try {
        let _date = new Date(value);
        return `${_date.getFullYear()}-${String(_date.getMonth() + 1).padStart(2, '0')}-${String(_date.getDate()).padStart(2, '0')}`;
    } catch (error) {
        return "---";
    }
}

export function translateHour(value) {
    if (!value) {
        return "---";
    }
    try {
        let _date = new Date(value);
        return `${String(_date.getHours()).padStart(2, '0')}:${String(_date.getMinutes()).padStart(2, '0')}`;
    } catch (error) {
        return "---";
    }
}

export function translateIdToString(id, catalog) {
    try {
        for (let element of catalog) {
            if (element["id"] === id) {
                return element["description"];
            }
        }

        return "Sin información";
    } catch (error) {
        console.log(error);
        return "---";
    }
}

export async function loadCatalog(apiClient, type, callback, parentId) {
    try {
        const response = await apiClient.getCatalog(type, parentId);
        if (response.length > 0) {
            callback(response);
        } else {
            callback([{ 'id': null, 'description': 'Sin opciones' }]);
        }
        return true;
    } catch (e) {
        console.log(e);
        return false;
    }
}

export const getStatusDescription = (clazz) => {
    const status = requestStatus.find((s) => {
        switch (clazz) {
            case 23: return s.id === 10;
            case 24: return s.id === 20;
            case 25: return s.id === 30;
            case 26: return s.id === 40;
            case 52: return s.id === 50;
            default: return false;
        }
    });
    return status ? status.description : 'Sin información';
};

export const getStatusClass = (clazz) => {
    const status = requestStatus.find((s) => {
        switch (clazz) {
            case 23: return s.id === 10;
            case 24: return s.id === 20;
            case 25: return s.id === 30;
            case 26: return s.id === 40;
            case 52: return s.id === 50;
            default: return false;
        }
    });
    return status ? status.class : 'default';
};
