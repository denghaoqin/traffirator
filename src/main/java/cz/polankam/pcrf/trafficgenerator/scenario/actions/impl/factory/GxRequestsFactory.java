package cz.polankam.pcrf.trafficgenerator.scenario.actions.impl.factory;

import cz.polankam.pcrf.trafficgenerator.scenario.ScenarioContext;
import cz.polankam.pcrf.trafficgenerator.utils.AvpCode;
import cz.polankam.pcrf.trafficgenerator.utils.data.DataProvider;
import cz.polankam.pcrf.trafficgenerator.utils.Vendor;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.gx.ClientGxSession;
import org.jdiameter.api.gx.events.GxCreditControlRequest;
import org.jdiameter.common.impl.app.gx.GxCreditControlRequestImpl;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class containing factories for all Gx related messages. All centered in the factory for better management.
 */
public class GxRequestsFactory {

    /**
     * Create initial CCR request, which is sent during the first connection of the device to the network.
     * @param context scenario context
     * @return CCR initial message
     * @throws Exception in case of any error
     */
    public static GxCreditControlRequest createCcrI(ScenarioContext context) throws Exception {
        ClientGxSession session = context.getGxSession();
        ConcurrentHashMap<String, Object> state = context.getState();
        int requestNumber = (int) state.get("cc_request_number");
        state.put("cc_request_number", requestNumber + 1);

        // *** CREATE REQUEST

        GxCreditControlRequestImpl req = new GxCreditControlRequestImpl(session, context.getGxRealm(), context.getGxServerUri());
        AvpSet avps = req.getMessage().getAvps();

        avps.addAvp(Avp.CC_REQUEST_TYPE, 1, true, false);
        avps.addAvp(Avp.CC_REQUEST_NUMBER, requestNumber, true, false);
        avps.addAvp(AvpCode.FRAMED_IP_ADDRESS, (Integer) state.get("framed_ip"), Vendor.COMMON, true, false);
        avps.addAvp(AvpCode.IP_CAN_TYPE, DataProvider.randomIpCanType(), true, false);
        avps.addAvp(AvpCode.RAT_TYPE, DataProvider.randomRatType(), true, false);
        avps.addAvp(AvpCode.AN_GW_ADDRESS, (InetAddress) state.get("an_gw_address"), Vendor.TGPP, false, false);
        avps.addAvp(AvpCode.GPP_MS_TIMEZONE, 4000, true, false);
        avps.addAvp(AvpCode.CALLED_STATION_ID, (String) state.get("called_station"), Vendor.COMMON, true, false, true);
        avps.addAvp(AvpCode.BEARER_USAGE, 0, Vendor.TGPP, true, false);
        avps.addAvp(AvpCode.ONLINE, 0, Vendor.TGPP, true, false);
        avps.addAvp(AvpCode.OFFLINE, 0, Vendor.TGPP, true, false);
        avps.addAvp(AvpCode.ACCESS_NETWORK_CHARGING_ADDRESS, (InetAddress) state.get("an_gw_address"), Vendor.TGPP, true, false);

        AvpSet subscrMSISDN = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 0, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("msisdn"), true, false, true);

        AvpSet subscrIMSI = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 1, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("imsi"), true, false, true);

        AvpSet suppFeatures = avps.addGroupedAvp(Avp.SUPPORTED_FEATURES, Vendor.TGPP, true, false);
        suppFeatures.addAvp(Avp.VENDOR_ID, (int) Vendor.TGPP, true, false);
        suppFeatures.addAvp(Avp.FEATURE_LIST_ID, 1, Vendor.TGPP, true, false);
        suppFeatures.addAvp(Avp.FEATURE_LIST, 2, Vendor.TGPP, true, false);

        AvpSet userEquipment = avps.addGroupedAvp(Avp.USER_EQUIPMENT_INFO, Vendor.TGPP, false, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_TYPE, 0, Vendor.TGPP, false, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_VALUE, (String) state.get("imei"), Vendor.TGPP, false, false, true);

        AvpSet epsBearer = avps.addGroupedAvp(AvpCode.DEFAULT_EPS_BEARER_QOS, Vendor.TGPP, true, false);
        epsBearer.addAvp(Avp.QOS_CLASS_IDENTIFIER, 5, Vendor.TGPP, true, false);
        AvpSet allocRetPrior = epsBearer.addGroupedAvp(Avp.ALLOCATION_RETENTION_PRIORITY, Vendor.TGPP, true, false);
        allocRetPrior.addAvp(Avp.PRIORITY_LEVEL, 11, Vendor.TGPP, true, false);
        allocRetPrior.addAvp(AvpCode.PRE_EMPTION_CAPABILITY, 1, Vendor.TGPP, true, false);
        allocRetPrior.addAvp(AvpCode.PRE_EMPTION_VULNERABILITY, 1, Vendor.TGPP, true, false);

        AvpSet accessNetCharg = avps.addGroupedAvp(AvpCode.ACCESS_NETWORK_CHARGING_IDENTIFIER_GX, Vendor.TGPP, true, false);
        accessNetCharg.addAvp(Avp.ACCESS_NETWORK_CHARGING_IDENTIFIER_VALUE, (String) state.get("an_ci_gx"), Vendor.TGPP, true, false, true);

        int bitrate = DataProvider.randomBitrate();
        AvpSet qosInfo = avps.addGroupedAvp(Avp.QOS_INFORMATION, Vendor.TGPP, true, false);
        qosInfo.addAvp(Avp.APN_AGGREGATE_MAX_BITRATE_UL, bitrate, Vendor.TGPP, true, false);
        qosInfo.addAvp(Avp.APN_AGGREGATE_MAX_BITRATE_DL, bitrate, Vendor.TGPP, true, false);
        qosInfo.addAvp(Avp.GPP_USER_LOCATION_INFO, DataProvider.randomUserLocation(), Vendor.TGPP, true, false, true);

        // *** RETURN REQUEST

        return req;
    }

    /**
     * Create termination CCR request, which is sent in case of disconnection of device from the network.
     * @param context scenario context
     * @return CCR termination request
     * @throws Exception in case of any error
     */
    public static GxCreditControlRequest createCcrT(ScenarioContext context) throws Exception {
        ClientGxSession session = context.getGxSession();
        ConcurrentHashMap<String, Object> state = context.getState();
        int requestNumber = (int) state.get("cc_request_number");
        state.put("cc_request_number", requestNumber + 1);

        // *** CREATE REQUEST

        GxCreditControlRequestImpl req = new GxCreditControlRequestImpl(session, context.getGxRealm(), context.getGxServerUri());
        AvpSet avps = req.getMessage().getAvps();

        avps.addAvp(Avp.CC_REQUEST_TYPE, 3, true, false);
        avps.addAvp(Avp.CC_REQUEST_NUMBER, requestNumber, true, false);
        avps.addAvp(AvpCode.FRAMED_IP_ADDRESS, (Integer) state.get("framed_ip"), Vendor.COMMON, true, false);
        avps.addAvp(AvpCode.CALLED_STATION_ID, (String) state.get("called_station"), true, false, true);
        avps.addAvp(AvpCode.ACCESS_NETWORK_CHARGING_ADDRESS, (InetAddress) state.get("an_gw_address"), true, false);
        avps.addAvp(Avp.TERMINATION_CAUSE, 1, true, false);

        AvpSet subscrMSISDN = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 0, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("msisdn"), true, false, true);

        AvpSet subscrIMSI = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 1, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("imsi"), true, false, true);

        AvpSet userEquipment = avps.addGroupedAvp(Avp.USER_EQUIPMENT_INFO, Vendor.TGPP, false, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_TYPE, 0, Vendor.TGPP, false, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_VALUE, (String) state.get("imei"), Vendor.TGPP, false, false, true);

        // *** RETURN REQUEST

        return req;
    }

    /**
     * Revalidation time event. Periodically revalidation is requested to confirm that session is alive,
     * PCEF sends CCR-U with the correct event trigger.
     * @param context scenario context
     * @return CCR update request
     * @throws Exception in case of any error
     */
    public static GxCreditControlRequest createCcrU(ScenarioContext context) throws Exception {
        ClientGxSession session = context.getGxSession();
        ConcurrentHashMap<String, Object> state = context.getState();
        int requestNumber = (int) state.get("cc_request_number");
        state.put("cc_request_number", requestNumber + 1);

        // *** CREATE REQUEST

        GxCreditControlRequestImpl req = new GxCreditControlRequestImpl(session, context.getGxRealm(), context.getGxServerUri());
        AvpSet avps = req.getMessage().getAvps();

        avps.addAvp(Avp.CC_REQUEST_TYPE, 2, true, false);
        avps.addAvp(Avp.CC_REQUEST_NUMBER, requestNumber, true, false);
        avps.addAvp(AvpCode.FRAMED_IP_ADDRESS, (Integer) state.get("framed_ip"), Vendor.COMMON, true, false);
        avps.addAvp(AvpCode.CALLED_STATION_ID, (String) state.get("called_station"), true, false, true);
        avps.addAvp(AvpCode.ACCESS_NETWORK_CHARGING_ADDRESS, (InetAddress) state.get("an_gw_address"), true, false);
        avps.addAvp(AvpCode.EVENT_TRIGGER, 17, true, false);

        AvpSet subscrMSISDN = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 0, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("msisdn"), true, false, true);

        AvpSet subscrIMSI = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 1, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("imsi"), true, false, true);

        AvpSet userEquipment = avps.addGroupedAvp(Avp.USER_EQUIPMENT_INFO, true, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_TYPE, 0, true, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_VALUE, (String) state.get("imei"), true, false, true);

        // *** RETURN REQUEST

        return req;
    }

    /**
     * Create lost connection update request, this request is sent by the PCEF in case of sudden lost connection of
     * the device. Lost connection CCR message is subtype of the CCR-U messages.
     * @param context scenario context
     * @return CCR lost connection update request
     * @throws Exception in case of any error
     */
    public static GxCreditControlRequest createCcrU_LostConnection(ScenarioContext context) throws Exception {
        ClientGxSession session = context.getGxSession();
        ConcurrentHashMap<String, Object> state = context.getState();
        int requestNumber = (int) state.get("cc_request_number");
        state.put("cc_request_number", requestNumber + 1);

        // *** CREATE REQUEST

        GxCreditControlRequestImpl req = new GxCreditControlRequestImpl(session, context.getGxRealm(), context.getGxServerUri());
        AvpSet avps = req.getMessage().getAvps();

        avps.addAvp(Avp.CC_REQUEST_TYPE, 2, true, false);
        avps.addAvp(Avp.CC_REQUEST_NUMBER, requestNumber, true, false);
        avps.addAvp(AvpCode.FRAMED_IP_ADDRESS, (Integer) state.get("framed_ip"), Vendor.COMMON, true, false);
        avps.addAvp(AvpCode.CALLED_STATION_ID, "ims", true, false, true);
        avps.addAvp(AvpCode.ACCESS_NETWORK_CHARGING_ADDRESS, (InetAddress) state.get("an_gw_address"), true, false);

        AvpSet subscrMSISDN = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 0, true, false);
        subscrMSISDN.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("msisdn"), true, false, true);

        AvpSet subscrIMSI = avps.addGroupedAvp(Avp.SUBSCRIPTION_ID, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 1, true, false);
        subscrIMSI.addAvp(Avp.SUBSCRIPTION_ID_DATA, (Integer) state.get("imsi"), true, false, true);

        AvpSet userEquipment = avps.addGroupedAvp(Avp.USER_EQUIPMENT_INFO, true, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_TYPE, 0, true, false);
        userEquipment.addAvp(Avp.USER_EQUIPMENT_INFO_VALUE, (String) state.get("imei"), true, false, true);

        if (state.get("first_charging_rule_name") != null && state.get("second_charging_rule_name") != null) {
            AvpSet chargRuleReport = avps.addGroupedAvp(AvpCode.CHARGING_RULE_REPORT, Vendor.TGPP, true, false);
            chargRuleReport.addAvp(AvpCode.CHARGING_RULE_NAME, (String) state.get("first_charging_rule_name"), Vendor.TGPP, true, false, true);
            chargRuleReport.addAvp(AvpCode.CHARGING_RULE_NAME, (String) state.get("second_charging_rule_name"), Vendor.TGPP, true, false, true);
            chargRuleReport.addAvp(AvpCode.PCC_RULE_STATUS, 1, Vendor.TGPP, true, false);
            chargRuleReport.addAvp(AvpCode.RULE_FAILURE_CODE, 10, Vendor.TGPP, true, false);

            state.remove("first_charging_rule_name");
            state.remove("second_charging_rule_name");
        }

        // *** RETURN REQUEST

        return req;
    }

}
