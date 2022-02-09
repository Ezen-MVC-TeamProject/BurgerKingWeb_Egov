package com.ezen.burger.dao;

import java.util.ArrayList;
import java.util.HashMap;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper(value="AddressDAO")
public interface AddressDAO {

	void b_getMyAddress(HashMap<String, Object> paramMap);

	void b_setUserAddress(HashMap<String, Object> paramMap);

	void b_setGuestAddress(HashMap<String, Object> gvo);

	void b_selectAddressByDong(HashMap<String, Object> paramMap);

	/*
	 * public MyAddressVO getMyAddress(int mseq);
	 * public void updateUserAddress(MyAddressVO mavo);
	 */
}
