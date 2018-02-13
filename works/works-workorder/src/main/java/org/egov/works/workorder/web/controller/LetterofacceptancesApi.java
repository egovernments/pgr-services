/**
 * NOTE: This class is auto generated by the swagger code generator program (2.2.3).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.egov.works.workorder.web.controller;

import io.swagger.annotations.*;
import org.egov.works.workorder.web.contract.ErrorRes;
import org.egov.works.workorder.web.contract.LetterOfAcceptanceRequest;
import org.egov.works.workorder.web.contract.LetterOfAcceptanceResponse;
import org.egov.works.workorder.web.contract.RequestInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-11-10T13:18:24.260Z")

@Api(value = "letterofacceptances", description = "the letterofacceptances API")
public interface LetterofacceptancesApi {

    @ApiOperation(value = "Create new Letter Of Acceptance(s).", notes = "To create new Letter Of Acceptance in the system. API supports bulk creation with max limit as defined in the Letter Of Acceptance Request. Please note that either whole batch succeeds or fails, there's no partial batch success. To create one Letter Of Acceptance, please pass array with one Letter Of Acceptance object.  Letter Of Acceptance can be created for approved detailed estimate after L1 Tender finalized offline status set to Detailed Estimate.  The Schedule A (SOR and Non SOR) selected while creating the detailed estimate will be populated in LOA as bill of quantity(BOQ).  The the LOA number and Agreement date will be user entered in case of spillover work and auto generated in case of new work.  Workflow will not be there for spillover work and new works will have workflow. So the spillover work will be created on approved status. Whether the LOA is spillover or not is decided based on the flag workOrderCreated from Abstract/Detailed Estimate.  ", response = LetterOfAcceptanceResponse.class, tags = {
            "Letter Of Acceptance", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Letter Of Acceptance(s) created successfully", response = LetterOfAcceptanceResponse.class),
        @ApiResponse(code = 400, message = "Letter Of Acceptance(s) creation failed", response = ErrorRes.class) })
    
    @RequestMapping(value = "/letterofacceptances/_create",
        method = RequestMethod.POST)
    ResponseEntity<LetterOfAcceptanceResponse> letterofacceptancesCreatePost(
            @ApiParam(value = "Details of new Letter Of Acceptance(s) + RequestInfo meta data.", required = true) @Valid @RequestBody LetterOfAcceptanceRequest letterOfAcceptanceRequest,
            @RequestParam(required = false) Boolean isRevision);


    @ApiOperation(value = "Get the list of Letter Of Acceptance(s) defined in the system.", notes = "Search and get Letter Of Acceptance(s) based on defined search criteria. Currently search parameters are only allowed as HTTP query params.  In case multiple parameters are passed Letter Of Acceptance(s) will be searched as an AND combination of all the parameters.  Maximum result size is restricted based on the maxlength of Letter Of Acceptance as defined in LetterOfAcceptanceResponse model.  Search results will be sorted by the sortProperty Provided in the parameters ", response = LetterOfAcceptanceResponse.class, tags={ "Letter Of Acceptance", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Letter Of Acceptance(s) Retrieved Successfully", response = LetterOfAcceptanceResponse.class),
        @ApiResponse(code = 400, message = "Invalid input.", response = ErrorRes.class) })
    
    @RequestMapping(value = "/letterofacceptances/_search",
        method = RequestMethod.POST)
    ResponseEntity<LetterOfAcceptanceResponse> letterofacceptancesSearchPost(@NotNull @ApiParam(value = "Unique id for a tenant.", required = true) @RequestParam(value = "tenantId", required = true) String tenantId, @ApiParam(value = "Parameter to carry Request metadata in the request body") @Valid @RequestBody RequestInfo requestInfo, @Min(0) @Max(100) @ApiParam(value = "Number of records returned.", defaultValue = "20") @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize, @ApiParam(value = "Page number", defaultValue = "1") @RequestParam(value = "pageNumber", required = false, defaultValue = "1") Integer pageNumber, @ApiParam(value = "This takes any field from the Object seperated by comma and asc,desc keywords. example name asc,code desc or name,code or name,code desc", defaultValue = "id") @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy, @Size(max = 50) @ApiParam(value = "Comma separated list of LOA Number to get Letter Of Acceptance(s)") @RequestParam(value = "loaNumbers", required = false) List<String> loaNumbers, @ApiParam(value = "Matches exact or like string given regardless of case-sensitive.") @RequestParam(value = "loaNumberLike", required = false) String loaNumberLike, @Size(max = 50) @ApiParam(value = "Comma separated list of Ids of Letter Of Acceptance to get the Letter Of Acceptance(s)") @RequestParam(value = "ids", required = false) List<String> ids, @Size(max = 50) @ApiParam(value = "Comma seperated list of Detailed Estimate Numbers to get the Letter Of Acceptance(s)") @RequestParam(value = "detailedEstimateNumbers", required = false) List<String> detailedEstimateNumbers, @ApiParam(value = "Matches exact or like string given regardless of case-sensitive.") @RequestParam(value = "detailedEstimateNumberLike", required = false) String detailedEstimateNumberLike, @ApiParam(value = "The file number for a Letter Of Acceptance") @RequestParam(value = "fileNumber", required = false) String fileNumber, @ApiParam(value = "Epoch time for Letter Of Acceptance when it is created in the system") @RequestParam(value = "fromDate", required = false) Long fromDate, @ApiParam(value = "Epoch time for Letter Of Acceptance when it is created in the system") @RequestParam(value = "toDate", required = false) Long toDate, @Size(max = 50) @ApiParam(value = "Comma separated list of the Department for which Letter Of Acceptance belongs to.") @RequestParam(value = "department", required = false) List<String> department, @Size(max = 50) @ApiParam(value = "Comma separated list of the LOA Status") @RequestParam(value = "statuses", required = false) List<String> statuses, @Size(max = 50) @ApiParam(value = "Comma separated list of Names of the contractor to which Letter Of Acceptance belongs to.") @RequestParam(value = "contractorNames", required = false) List<String> contractorNames, @Size(max = 50) @ApiParam(value = "Comma separated list of codes of the contractor to which Letter Of Acceptance belongs to.") @RequestParam(value = "contractorCodes", required = false) List<String> contractorCodes, @ApiParam(value = "Matches exact or like string given regardless of case-sensitive.") @RequestParam(value = "contractorCodeLike", required = false) String contractorCodeLike, @ApiParam(value = "Matches exact or like string given regardless of case-sensitive.") @RequestParam(value = "contractorNameLike", required = false) String contractorNameLike, @ApiParam(value = "Boolean value of the LOA whether its Spillover or not") @RequestParam(value = "spillOverFlag", required = false) Boolean spillOverFlag, @ApiParam(value = "if this value is true, API returns all the LOAs where WorkOrder is created. if this values is false, API returns all the LOAs where WorkOrder is not created. If no value then it returns all the LOAs.") @RequestParam(value = "workOrderExists", required = false) Boolean workOrderExists, @ApiParam(value = "if this value is true, API returns all the LOAs where Offline Status is AGREEMENT_ORDER_SIGNED. if this values is false, API returns all the LOAs where Offline Status is not created. If no value then it returns all the LOAs.") @RequestParam(value = "withAllOfflineStatusAndWONotCreated", required = false) Boolean withAllOfflineStatusAndWONotCreated, @ApiParam(value = "if this value is true, API returns all the LOAs where Milestone is created. if this values is false, API returns all the LOAs where Milestone is not created. If no value then it returns all the LOAs.") @RequestParam(value = "milestoneExists", required = false) Boolean milestoneExists, @ApiParam(value = "if this value is true, API returns all the LOAs where bill is created. if this values is false, API returns all the LOAs where bill is not created. If no value then it returns all the LOAs.") @RequestParam(value = "billExists", required = false) Boolean billExists, @ApiParam(value = "if this value is true, API returns all the LOAs where ARF is created. if this values is false, API returns all the LOAs where ARF is not created. If no value then it returns all the LOAs.") @RequestParam(value = "contractorAdvanceExists", required = false) Boolean contractorAdvanceExists, @ApiParam(value = "if this value is true, API returns all the LOAs where MB is approved. if this values is false, API returns all the LOAs where MB is not approved. If no value then it returns all the LOAs.") @RequestParam(value = "mbExists", required = false) Boolean mbExists, @ApiParam(value = "Returns all LOA where passed offline status is set for LOA.") @RequestParam(value = "offlineStatus", required = false) String offlineStatus, @ApiParam(value = "Returns all LOA for passed Loa estimate Id's.") @RequestParam(value = "loaEstimateId", required = false) String loaEstimateId);


    @ApiOperation(value = "Update existing Letter Of Acceptance(s).", notes = "To update existing Letter Of Acceptance in the system. API supports bulk updation with max limit as defined in the Letter Of Acceptance Request. Please note that either whole batch succeeds or fails, there's no partial batch success. To update one Letter Of Acceptance, please pass array with one Letter Of Acceptance object.   Only the creator can edit all the fields when the Letter Of Acceptance in creator inbox(in Rejected status) or in drafts. Update during workflow will have all the fields readonly except few fields which are allowed to modify during each stage of workflow including Workflow Details. The status and workflow state will be updated during each stage of workflow approval. ", response = LetterOfAcceptanceResponse.class, tags={ "Letter Of Acceptance", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Letter Of Acceptance(s) updated successfully", response = LetterOfAcceptanceResponse.class),
        @ApiResponse(code = 400, message = "Letter Of Acceptance(s) updation failed", response = ErrorRes.class) })
    
    @RequestMapping(value = "/letterofacceptances/_update",
        method = RequestMethod.POST)
    ResponseEntity<LetterOfAcceptanceResponse> letterofacceptancesUpdatePost(
            @ApiParam(value = "Details of Letter Of Acceptance(s) + RequestInfo meta data.", required = true) @Valid @RequestBody LetterOfAcceptanceRequest letterOfAcceptanceRequest,
            @RequestParam(required = false) Boolean isRevision);

}
