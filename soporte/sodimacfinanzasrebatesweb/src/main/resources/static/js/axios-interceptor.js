axios.interceptors.response.use(response => {
	return response;
}, error => {
	if (error.response.status === 401) {
		Swal.fire({
			title: 'Session expired',
			text: 'Please sign in again!',
			type: 'error',
		}).then(result => {
			if (result.value) {
				location.reload();
			}
		});
	}

	return Promise.reject(error);
});