package org.egov.tl.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tl.repository.TLRepository;
import org.egov.tl.util.TradeUtil;
import org.egov.tl.validator.TLValidator;
import org.egov.tl.web.models.TradeLicense;
import org.egov.tl.web.models.TradeLicenseRequest;
import org.egov.tl.web.models.TradeLicenseSearchCriteria;
import org.egov.tl.web.models.user.UserDetailResponse;
import org.egov.tl.workflow.ActionValidator;
import org.egov.tl.workflow.TLWorkflowService;
import org.egov.tl.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TradeLicenseService {
	
	@Value("${is.external.workflow.enabled}")
	private Boolean isExternalWorkFlowEnabled;
	
	private WorkflowIntegrator wfIntegrator;

    private EnrichmentService enrichmentService;

    private UserService userService;

    private TLRepository repository;

    private ActionValidator actionValidator;

    private TLValidator tlValidator;

    private TLWorkflowService workflowService;

    private CalculationService calculationService;

    private TradeUtil util;


	@Autowired
	public TradeLicenseService(WorkflowIntegrator wfIntegrator, EnrichmentService enrichmentService,
			UserService userService, TLRepository repository, ActionValidator actionValidator, TLValidator tlValidator,
			TLWorkflowService workflowService, CalculationService calculationService, TradeUtil util) {
		
		this.wfIntegrator = wfIntegrator;
		this.enrichmentService = enrichmentService;
		this.userService = userService;
		this.repository = repository;
		this.actionValidator = actionValidator;
		this.tlValidator = tlValidator;
		this.workflowService = workflowService;
		this.calculationService = calculationService;
		this.util = util;
	}


    /**
     * creates the tradeLicense for the given request
     * @param tradeLicenseRequest The TradeLicense Create Request
     * @return The list of created traddeLicense
     */
    public List<TradeLicense> create(TradeLicenseRequest tradeLicenseRequest){
        Object mdmsData = util.mDMSCall(tradeLicenseRequest);
        tlValidator.validateCreate(tradeLicenseRequest,mdmsData);
        actionValidator.validateCreateRequest(tradeLicenseRequest);
        enrichmentService.enrichTLCreateRequest(tradeLicenseRequest,mdmsData);
        userService.createUser(tradeLicenseRequest);
        calculationService.addCalculation(tradeLicenseRequest);
		
        /*
		 * call workflow service if it's enable else uses internal workflow process
		 */
		if (isExternalWorkFlowEnabled)
			wfIntegrator.callWorkFlow(tradeLicenseRequest);
		repository.save(tradeLicenseRequest);
		return tradeLicenseRequest.getLicenses();
	}


    /**
     *  Searches the tradeLicense for the given criteria if search is on owner paramter then first user service
     *  is called followed by query to db
     * @param criteria The object containing the paramters on which to search
     * @param requestInfo The search request's requestInfo
     * @return List of tradeLicense for the given criteria
     */
    public List<TradeLicense> search(TradeLicenseSearchCriteria criteria, RequestInfo requestInfo){
        List<TradeLicense> licenses;
        tlValidator.validateSearch(requestInfo,criteria);
        enrichmentService.enrichSearchCriteriaWithAccountId(requestInfo,criteria);
         if(criteria.getMobileNumber()!=null){
             licenses = getLicensesFromMobileNumber(criteria,requestInfo);
         }
         else {
             licenses = getLicensesWithOwnerInfo(criteria,requestInfo);
         }
       return licenses;
    }


    private List<TradeLicense> getLicensesFromMobileNumber(TradeLicenseSearchCriteria criteria, RequestInfo requestInfo){
        List<TradeLicense> licenses = new LinkedList<>();
        UserDetailResponse userDetailResponse = userService.getUser(criteria,requestInfo);
        // If user not found with given user fields return empty list
        if(userDetailResponse.getUser().size()==0){
            return Collections.emptyList();
        }
        enrichmentService.enrichTLCriteriaWithOwnerids(criteria,userDetailResponse);
        licenses = repository.getLicenses(criteria);
        // If property not found with given propertyId or oldPropertyId or address fields return empty list
        if(licenses.size()==0){
            return Collections.emptyList();
        }
        licenses = enrichmentService.enrichTradeLicenseSearch(licenses,criteria,requestInfo);
        return licenses;
    }


    /**
     * Returns the tradeLicense with enrivhed owners from user servise
     * @param criteria The object containing the paramters on which to search
     * @param requestInfo The search request's requestInfo
     * @return List of tradeLicense for the given criteria
     */
    public List<TradeLicense> getLicensesWithOwnerInfo(TradeLicenseSearchCriteria criteria,RequestInfo requestInfo){
        List<TradeLicense> licenses = repository.getLicenses(criteria);
        if(licenses.isEmpty())
            return Collections.emptyList();
        licenses = enrichmentService.enrichTradeLicenseSearch(licenses,criteria,requestInfo);
        return licenses;
    }


    /**
     * Updates the tradeLicenses
     * @param tradeLicenseRequest The update Request
     * @return Updated TradeLcienses
     */
    public List<TradeLicense> update(TradeLicenseRequest tradeLicenseRequest){
        Object mdmsData = util.mDMSCall(tradeLicenseRequest);
        tlValidator.validateUpdate(tradeLicenseRequest,mdmsData);
        actionValidator.validateUpdateRequest(tradeLicenseRequest);
        enrichmentService.enrichTLUpdateRequest(tradeLicenseRequest);
	/*
	 * call workflow service if it's enable else uses internal workflow process
	 */
		if (isExternalWorkFlowEnabled)
			wfIntegrator.callWorkFlow(tradeLicenseRequest);
		else
			workflowService.updateStatus(tradeLicenseRequest);

		enrichmentService.postStatusEnrichment(tradeLicenseRequest);
        userService.createUser(tradeLicenseRequest);
        calculationService.addCalculation(tradeLicenseRequest);
        repository.update(tradeLicenseRequest);
        return tradeLicenseRequest.getLicenses();
    }


}
