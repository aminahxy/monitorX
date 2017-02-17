package com.sina.data.rest.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public abstract class BaseResource extends ServerResource {
    private static final Log LOG = LogFactory.getLog(BaseResource.class);

    protected String userName;
    protected String fromIP;
    protected JSONObject requestedJson;

    protected final static String SUCCESS_CODE = "0";
    protected final static String SUCCESS_DESC = "success";

    protected final static String FAILED = "failed";

    @Override
    protected void doInit() throws ResourceException {
        try {
            requestedJson = new JsonRepresentation(getRequestEntity()).getJsonObject();
            LOG.info("requestedJson:" + requestedJson);
        } catch (JSONException e1) {
        } catch (Exception e1) {
        }
        userName = getQuery().getFirstValue("userName");
        if (userName == null && requestedJson != null) {
            try {
                userName = requestedJson.getString("userName");
            } catch (JSONException e) {
            }
        }
        fromIP = getClientInfo().getAddress();



        init();
    }

    abstract void init();

    @Override
    public Representation get() throws ResourceException {
        return getOper();
    }

    @Override
    public Representation put(Representation entity) throws ResourceException {
        return putOper(entity);
    }

    @Override
    public Representation delete() throws ResourceException {
        return delOper();
    }

    @Override
    public Representation post(Representation entity) {
        long startTime = System.currentTimeMillis();
        Representation r = postOper(entity);
        long endTime = System.currentTimeMillis();
        LOG.info("-----cost---"+((endTime-startTime)/1000) + " s");
        return r;
    }

    /**
     * GET Operation for sub resource class inherit
     *
     * @return
     * @throws ResourceException
     */
    protected Representation getOper() throws ResourceException {
        return status(SUCCESS_CODE, SUCCESS_DESC);
    }

    protected Representation postOper(Representation entity)
            throws ResourceException {
        return status(SUCCESS_CODE, SUCCESS_DESC);
    }

    /**
     * PUT Operation for sub resource class inherit
     *
     * @return
     * @throws ResourceException
     */
    protected Representation putOper(Representation entity)
            throws ResourceException {
        return status(SUCCESS_CODE, SUCCESS_DESC);
    }

    /**
     * DELETE Operation for sub resource class inherit
     *
     * @return
     * @throws ResourceException
     */
    protected Representation delOper() throws ResourceException {
        return status(SUCCESS_CODE, SUCCESS_DESC);
    }


    protected Representation status(String code, String desc, JSONArray data) {
        JSONObject json = new JSONObject();
        try {
            json.put("code", code);
            json.put("desc", desc);
            if (data != null) {
                json.put("data", data);
            }
        } catch (JSONException e) {
        }
        return new JsonRepresentation(json);

    }

    protected Representation status(String code, String desc) {
        JSONObject json = new JSONObject();
        try {
            json.put("code", code);
            json.put("desc", desc);
        } catch (JSONException e) {
        }
        return new JsonRepresentation(json);
    }

}
