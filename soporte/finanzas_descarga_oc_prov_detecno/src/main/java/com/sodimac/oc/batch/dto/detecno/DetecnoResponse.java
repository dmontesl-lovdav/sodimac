package com.sodimac.oc.batch.dto.detecno;

import java.util.List;

public class DetecnoResponse {

	private Integer totalCount;
	private List<DetecnoData> data;

	public DetecnoResponse() {
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public List<DetecnoData> getData() {
		return data;
	}

	public void setData(List<DetecnoData> data) {
		this.data = data;
	}

}
