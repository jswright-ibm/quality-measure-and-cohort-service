package com.ibm.cohort.engine.api.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@Generated
public class FakeResult {
	private String aParam = null;

	public FakeResult(String aParam) {
		this.aParam = aParam;
	}

	@ApiModelProperty(value = "A test parameter")
	@JsonProperty("aParam")
	public String getaParam() {
		return aParam;
	}

	public void setaParam(String aParam) {
		this.aParam = aParam;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FakeResult that = (FakeResult) o;

		return aParam != null ? aParam.equals(that.aParam) : that.aParam == null;

	}

	@Override
	public int hashCode() {
		return aParam != null ? aParam.hashCode() : 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FakeResult {\n");

		sb.append("    aParam: ").append(aParam).append("\n");
		sb.append("}");
		return sb.toString();
	}
}
