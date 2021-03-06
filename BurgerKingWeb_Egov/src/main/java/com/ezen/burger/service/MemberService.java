package com.ezen.burger.service;

import java.util.HashMap;

public interface MemberService {

	void getMember(HashMap<String, Object> paramMap);

	void lastDateUpdate(HashMap<String, Object> paramMap);

	void b_insertMember(HashMap<String, Object> paramMap);

	void b_getMember(HashMap<String, Object> paramMap);

	void b_selectGseq(HashMap<String, Object> paramMap2);

	void b_insertGuest(HashMap<String, Object> paramMap);

	void b_getGuest(HashMap<String, Object> paramMap3);

	void updateMember(HashMap<String, Object> mvo);

	void b_findMember(HashMap<String, Object> paramMap);
	
	void deleteMember(HashMap<String, Object> paramMap);

	void b_findPwd(HashMap<String, Object> paramMap);

	void b_updatePwd(HashMap<String, Object> paramMap);

	void b_getMember2(HashMap<String, Object> paramMap);

	void b_adminUpdateMember(HashMap<String, Object> mvo);

	void b_deleteAddress(HashMap<String, Object> paramMap);
}
