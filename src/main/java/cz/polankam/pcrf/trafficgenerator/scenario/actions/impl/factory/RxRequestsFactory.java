package cz.polankam.pcrf.trafficgenerator.scenario.actions.impl.factory;

import cz.polankam.pcrf.trafficgenerator.scenario.ScenarioContext;
import cz.polankam.pcrf.trafficgenerator.utils.AvpUtils;
import cz.polankam.pcrf.trafficgenerator.utils.data.DataProvider;
import cz.polankam.pcrf.trafficgenerator.utils.Vendor;
import cz.polankam.pcrf.trafficgenerator.utils.data.flow.MediaSubComponents;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.rx.ClientRxSession;
import org.jdiameter.api.rx.events.RxAARequest;
import org.jdiameter.api.rx.events.RxSessionTermRequest;
import org.jdiameter.common.impl.app.rx.RxAARequestImpl;
import org.jdiameter.common.impl.app.rx.RxSessionTermRequestImpl;

import java.util.concurrent.ConcurrentHashMap;


public class RxRequestsFactory {

    public static RxAARequest createAar(ScenarioContext context, String[] codecData) throws Exception {
        ClientRxSession session = context.getRxSession();
        ConcurrentHashMap<String, Object> state = context.getState();

        // *** CREATE REQUEST

        RxAARequestImpl req = new RxAARequestImpl(session, context.getRxRealm(), context.getRxServerUri());
        AvpSet avps = req.getMessage().getAvps();

        avps.addAvp(AvpUtils.FRAMED_IP_ADDRESS, (Integer) state.get("framed_ip"), Vendor.COMMON, true, false);
        avps.addAvp(Avp.AF_CHARGING_IDENTIFIER, (String) state.get("af_charging_identifier"), Vendor.TGPP, true, false, true);
        avps.addAvp(AvpUtils.SPECIFIC_ACTION, 2, Vendor.TGPP, true, false);

        int bandwidth = DataProvider.randomBitrate();
        AvpSet mediaCompDesc = avps.addGroupedAvp(AvpUtils.MEDIA_COMPONENT_DESCRIPTION, Vendor.TGPP, true, false);
        mediaCompDesc.addAvp(AvpUtils.MEDIA_COMPONENT_NUMBER, DataProvider.randomMediaComponentNumber(), Vendor.TGPP, true, false);
        mediaCompDesc.addAvp(AvpUtils.MEDIA_TYPE, 0, Vendor.TGPP, true, false);
        mediaCompDesc.addAvp(Avp.MAX_REQUESTED_BANDWIDTH_UL, bandwidth, Vendor.TGPP, true, false);
        mediaCompDesc.addAvp(Avp.MAX_REQUESTED_BANDWIDTH_DL, bandwidth, Vendor.TGPP, true, false);
        mediaCompDesc.addAvp(AvpUtils.FLOW_STATUS, 2, Vendor.TGPP, true, false);
        mediaCompDesc.addAvp(AvpUtils.CODEC_DATA, codecData[0], Vendor.TGPP, true, false, true); // TODO: codec data?
        mediaCompDesc.addAvp(AvpUtils.CODEC_DATA, codecData[1], Vendor.TGPP, true, false, true); // TODO: codec data?
        mediaCompDesc.addAvp(AvpUtils.AF_APPLICATION_IDENTIFIER, "sbc", Vendor.TGPP, true, false, true);

        MediaSubComponents providedFlows = DataProvider.randomMediaSubComponents();
        AvpSet mediaSubComp = mediaCompDesc.addGroupedAvp(AvpUtils.MEDIA_SUB_COMPONENT, Vendor.TGPP, true, false);
        mediaSubComp.addAvp(AvpUtils.FLOW_NUMBER, providedFlows.getFirst().getFlowNumber(), Vendor.TGPP, true, false);
        mediaSubComp.addAvp(AvpUtils.FLOW_DESCRIPTION, providedFlows.getFirst().getFlowDescription().getIn(), Vendor.TGPP, true, false, true);
        mediaSubComp.addAvp(AvpUtils.FLOW_DESCRIPTION, providedFlows.getFirst().getFlowDescription().getOut(), Vendor.TGPP, true, false, true);
        mediaSubComp.addAvp(AvpUtils.FLOW_STATUS, 2, Vendor.TGPP, true, false);

        mediaSubComp = mediaCompDesc.addGroupedAvp(AvpUtils.MEDIA_SUB_COMPONENT, Vendor.TGPP, true, false);
        mediaSubComp.addAvp(AvpUtils.FLOW_NUMBER, providedFlows.getSecond().getFlowNumber(), Vendor.TGPP, true, false);
        mediaSubComp.addAvp(AvpUtils.FLOW_DESCRIPTION, providedFlows.getSecond().getFlowDescription().getIn(), Vendor.TGPP, true, false, true);
        mediaSubComp.addAvp(AvpUtils.FLOW_DESCRIPTION, providedFlows.getSecond().getFlowDescription().getOut(), Vendor.TGPP, true, false, true);
        mediaSubComp.addAvp(AvpUtils.FLOW_USAGE, 1, Vendor.TGPP, true, false);
        mediaSubComp.addAvp(AvpUtils.FLOW_STATUS, 2, Vendor.TGPP, true, false);

        // *** RETURN REQUEST

        return req;
    }

    public static RxSessionTermRequest createStr(ScenarioContext context) throws Exception {
        ClientRxSession session = context.getRxSession();
        ConcurrentHashMap<String, Object> state = context.getState();

        // *** CREATE REQUEST

        RxSessionTermRequestImpl req = new RxSessionTermRequestImpl(session, context.getRxRealm(), context.getRxServerUri());
        AvpSet avps = req.getMessage().getAvps();
        avps.addAvp(Avp.TERMINATION_CAUSE, 1, true, false);

        // *** RETURN REQUEST

        return req;
    }

}
