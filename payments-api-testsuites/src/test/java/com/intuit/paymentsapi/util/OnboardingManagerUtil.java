package com.intuit.paymentsapi.util;

import java.util.Map;

import org.testng.annotations.Test;

import com.intuit.obsvalidator.onboarding.OnboardingManager;

public class OnboardingManagerUtil {
	
	private String userEmailId = System.getProperty("email_id");
	private String userEmailDomain = System.getProperty("domain");
	private String userEmailPwd = System.getProperty("pwd");
	private String channel = System.getProperty("ch");
	private String src = System.getProperty("src");
	private String offerId = System.getProperty("offer_id");
	private String brandId = System.getProperty("brand_id");
	private String decision = System.getProperty("decision");
	
	
	
	private void populateOBSInputDataFields(){
		//first try to set field through System property variable as above, if null, then default to value from properties file
		if(userEmailId==null || userEmailId.isEmpty()){
			userEmailId=PropertyUtil.getValue(Constants.DEFAULT_USER_EMAIL_ID);
		}
		if(userEmailDomain==null || userEmailDomain.isEmpty()){
			userEmailDomain=PropertyUtil.getValue(Constants.DEFAULT_USER_EMAIL_DOMAIN);
		}
		if(userEmailPwd==null || userEmailPwd.isEmpty()){
			userEmailPwd=PropertyUtil.getValue(Constants.DEFAULT_USER_EMAIL_PWD);
		}
		if(channel==null || channel.isEmpty()){
			channel=PropertyUtil.getValue(Constants.DEFAULT_CHANNEL);
		}
		if(src==null || src.isEmpty()){
			src=PropertyUtil.getValue(Constants.DEFAULT_SOURCE);
		}
		if(offerId==null || offerId.isEmpty()){
			offerId=PropertyUtil.getValue(Constants.DEFAULT_OFFER_ID);
		}
		if(brandId==null || brandId.isEmpty()){
			brandId=PropertyUtil.getValue(Constants.DEFAULT_BRAND_ID);
		}
		if(decision==null || decision.isEmpty()){
			decision=PropertyUtil.getValue(Constants.DEFAULT_DECISION);
		}
	}
	
	private Map<String, Object> createMerchant() throws Exception{
		populateOBSInputDataFields();
		OnboardingManager om = new OnboardingManager();
		return om.createMerchant(om.createOBSInputDataObject(userEmailId, userEmailDomain, channel, src, userEmailPwd, offerId, brandId, decision));		
	}
	
	@Test
	public String getRealmId() throws Exception{
		Map<String, Object> map = createMerchant();
		OnboardingManager om = new OnboardingManager();
		return om.getRealmId(map);		
	}
}
