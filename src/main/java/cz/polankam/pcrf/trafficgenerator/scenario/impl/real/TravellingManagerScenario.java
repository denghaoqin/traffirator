package cz.polankam.pcrf.trafficgenerator.scenario.impl.real;

import cz.polankam.pcrf.trafficgenerator.scenario.NodeBuilder;
import cz.polankam.pcrf.trafficgenerator.scenario.Scenario;
import cz.polankam.pcrf.trafficgenerator.scenario.ScenarioNode;
import cz.polankam.pcrf.trafficgenerator.scenario.actions.impl.LoggerPrintAction;
import cz.polankam.pcrf.trafficgenerator.scenario.actions.impl.call.*;
import cz.polankam.pcrf.trafficgenerator.scenario.actions.impl.control.*;
import cz.polankam.pcrf.trafficgenerator.utils.data.DataProvider;

import java.util.HashMap;


public class TravellingManagerScenario extends Scenario {

    public static final String TYPE = "TravellingManager";
    private static ScenarioNode rootNode;

    private static void init() throws Exception {
        if (rootNode != null) {
            return;
        }

        ScenarioNode START = rootNode = new NodeBuilder().addSendAction(new LoggerPrintAction("*** START ***")).build();
        ScenarioNode END = new NodeBuilder().addSendAction(new LoggerPrintAction("*** END ***")).build();

        ScenarioNode connect = new NodeBuilder()
                .addSendAction(new GxCcrI_SendAction())
                .addReceiveGxAction(new GxCcaI_Success_ReceiveAction())
                .build();
        ScenarioNode update = new NodeBuilder()
                .addSendAction(new GxCcrU_SendAction())
                .addReceiveGxAction(new GxCcaU_Success_ReceiveAction())
                .build();
        ScenarioNode disconnect = new NodeBuilder()
                .addSendAction(new GxCcrT_SendAction())
                .addReceiveGxAction(new GxCcaT_Success_ReceiveAction())
                .build();
        ScenarioNode lostConnection = new NodeBuilder()
                .addSendAction(new GxCcrU_LostConnection_SendAction())
                .addReceiveGxAction(new GxCcaU_Success_ReceiveAction())
                .build();
        ScenarioNode callInit = new NodeBuilder()
                .addSendAction(new RxAar_SendAction())
                .addReceiveAction(new GxRar_ReceiveAction(), new RxAaa_Success_ReceiveAction())
                .addSendAction(new GxRaa_Success_SendAction())
                .addSendAction(new RxAar_SendAction())
                .addReceiveRxAction(new RxAaa_Success_ReceiveAction())
                .build();
        ScenarioNode callUpdate = new NodeBuilder()
                .addSendAction(new GxCcrU_SendAction())
                .addReceiveGxAction(new GxCcaU_Success_ReceiveAction())
                .build();
        ScenarioNode callUpdateCodec = new NodeBuilder() // TODO: what to do here?
                .build();
        ScenarioNode callTermination = new NodeBuilder()
                .addSendAction(new RxStr_SendAction())
                .addReceiveAction(new GxRar_ReceiveAction(), new RxSta_Success_ReceiveAction())
                .addSendAction(new GxRaa_Success_SendAction())
                .build();
        ScenarioNode callLostConnection = new NodeBuilder()
                .addSendAction(new GxCcrU_LostConnection_SendAction())
                .addReceiveAction(new GxCcaU_Success_ReceiveAction(), new RxAsr_ReceiveAction())
                .addSendAction(new RxAsa_Success_SendAction())
                .build();

        /*
         *  Simplified image, some relations are missing
         *
         *
         *                         END
         *                         /
         *           LOST CONNECTION ------------------------------------ CALL TERMINATION -------- CALL UPDATE
         * START      /            \                                     /                \           /
         *     \     /              \                                   /                  \         /
         *     CONNECT --------- UPDATE ------------------------ CALL INIT ---------- CALL UPDATE CODEC
         *          \             /                                    \                  /
         *           \           /                                      \                /
         *            DISCONNECT ---------------- END ---------------- CALL LOST CONNECTION
         *
         */

        START.addChild(100, connect).validateProbabilities();
        connect.addChild(50, update).addChild(10, disconnect).addChild(10, lostConnection).addChild(30, callInit).validateProbabilities();
        update.addChild(30, update).addChild(10, disconnect).addChild(10, lostConnection).addChild(50, callInit).validateProbabilities();
        disconnect.addChild(50, connect).addChild(50, END).validateProbabilities();
        lostConnection.addChild(50, connect).addChild(50, END).validateProbabilities();
        callInit.addChild(50, callUpdate).addChild(20, callUpdateCodec).addChild(10, callTermination).addChild(20, callLostConnection).validateProbabilities();
        callUpdate.addChild(60, callUpdate).addChild(20, callUpdateCodec).addChild(20, callTermination).validateProbabilities();
        callUpdateCodec.addChild(20, callUpdateCodec).addChild(40, callUpdate).addChild(20, callTermination).addChild(20, callLostConnection).validateProbabilities();
        callTermination.addChild(20, callInit).addChild(60, update).addChild(10, disconnect).addChild(10, lostConnection).validateProbabilities();
        callLostConnection.addChild(50, connect).addChild(50, END).validateProbabilities();
    }

    @Override
    protected HashMap<String, Object> createNewScenarioState() throws Exception {
        HashMap<String, Object> state = new HashMap<>();

        state.put("cc_request_number", 0);
        state.put("framed_ip", DataProvider.randomFramedIp());
        state.put("imei", DataProvider.randomIMEI());
        state.put("msisdn", DataProvider.randomMSISDN());
        state.put("an_gw_address", DataProvider.randomAnGwAddress());
        state.put("an_ci_gx", DataProvider.randomAnCiGx());
        state.put("called_station", "ims");
        state.put("af_charging_identifier", DataProvider.randomAfChargingIdentifier());

        return state;
    }

    @Override
    protected ScenarioNode getRootNode() throws Exception {
        init();
        return rootNode;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}