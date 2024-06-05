package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class MasterDataRequest {

	private Object  city;
	private Object maritalStatus;
	private Object relationShip;
	private Object annualIncome;
	private Object occupationType;
	private Object employerType;
	private Object selfEmployerType;
	private Object organizationType;
	private Object natureOfBusiness;
	private Object product;
	private Object branch;
	private Object email;
	private Object residenceType;
	private Object firmType;
	private SelfEmployedProcessional selfEmployedProcessional;
}
