package org.egov.filters.route;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.client.ClientException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.io.IOUtils;
import org.egov.model.AuthRequestWrapper;
import org.egov.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.http.client.ClientHttpResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class AuthFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private ProxyRequestHelper helper = new ProxyRequestHelper();
    private RibbonRoutingFilter delegateFilter;
    private HttpServletRequest originalRequest;
    private String originalRequestUri;
    private URL originalRouteHost;
    private HashMap<String, List<String>> originalRequestQueryParams;

    public AuthFilter(RibbonRoutingFilter delegateFilter) {
        this.delegateFilter = delegateFilter;
    }

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return delegateFilter.filterOrder();
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        return !request.getRequestURI().contains("/user") && request.getHeader("auth_token") != null;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        try {
            saveOriginalRequestItems(ctx);
            prepareToRouteToAuthService(ctx);
            ClientHttpResponse authResponse = (ClientHttpResponse) delegateFilter.run();
            prepareToRouteToOriginalService(ctx);

            if (isAuthenticationSuccessful(authResponse)) {
                String user = getUserFromAuthResponse(authResponse);
                ctx.addZuulRequestHeader("user_info", user);
                return delegateFilter.run();
            } else {
                logError(ctx);
                abort(ctx, authResponse);
            }
        } catch (Exception e) {
            ctx.set("error.status_code", SC_INTERNAL_SERVER_ERROR);
            ctx.set("error.exception", e);
            logError(ctx);
        }

        return null;
    }

    private void saveOriginalRequestItems(RequestContext ctx) {
        originalRequestQueryParams = (HashMap<String, List<String>>) ctx.getRequestQueryParams();
        originalRequest = ctx.getRequest();
        originalRequestUri = originalRequest.getRequestURI();
        originalRouteHost = ctx.getRouteHost();
    }

    private void logError(RequestContext ctx) {
        log.error("ERROR");
        log.error(String.format("STATUS_CODE: %s", ctx.get("error.status_code")));
        log.error(String.format("ERROR_MESSAGE: %s", ctx.get("error.message")));
        if (ctx.get("error.exception") != null) {
            Exception ex = (Exception) ctx.get("error.exception");
            String stackTrace = Arrays.toString(ex.getStackTrace());
            log.error(String.format("TRACE: %s", stackTrace));
        }
    }

    private void abort(RequestContext ctx, ClientHttpResponse authResponse) throws ClientException, IOException {
        if (authResponse != null) {
            setResponse(authResponse);
        }
        ctx.setSendZuulResponse(false);
    }

    private void prepareToRouteToOriginalService(RequestContext ctx) throws MalformedURLException {
        ctx.setRouteHost(new URL(originalRouteHost.getProtocol(), originalRouteHost.getHost(),
                originalRouteHost.getPort(), "/"));
        ctx.setRequestQueryParams(originalRequestQueryParams);
        ctx.set("requestURI", originalRequestUri);
        ctx.setRequest(originalRequest);

    }

    private void prepareToRouteToAuthService(RequestContext ctx) {
        AuthRequestWrapper authRequestWrapper = new AuthRequestWrapper(ctx.getRequest());
        HashMap<String, List<String>> parameters = new HashMap<>();
        List<String> paramValues = new ArrayList<>();
        paramValues.add(authRequestWrapper.getHeader("auth_token"));
        parameters.put("access_token", paramValues);

        ctx.set("serviceId", "user");
        ctx.setRequestQueryParams(parameters);
        ctx.set("requestURI", "/user/_details");
        ctx.setRequest(authRequestWrapper);
    }

    private String getUserFromAuthResponse(ClientHttpResponse authResponse) throws IOException {
        String authResponseBody = IOUtils.toString(authResponse.getBody(), "utf-8");
        ObjectMapper mapper = new ObjectMapper();
        User authenticatedUser = mapper.readValue(authResponseBody, User.class);
        return mapper.writeValueAsString(authenticatedUser);
    }

    private boolean isAuthenticationSuccessful(ClientHttpResponse response) throws IOException {
        return response != null && response.getStatusCode().is2xxSuccessful();
    }

    private void setResponse(ClientHttpResponse resp) throws ClientException, IOException {
        helper.setResponse(resp.getStatusCode().value(),
                resp.getBody() == null ? null : resp.getBody(), resp.getHeaders());
    }

}