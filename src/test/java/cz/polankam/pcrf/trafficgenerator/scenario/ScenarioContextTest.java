package cz.polankam.pcrf.trafficgenerator.scenario;

import cz.polankam.pcrf.trafficgenerator.client.GxStack;
import cz.polankam.pcrf.trafficgenerator.client.RxStack;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.gx.ClientGxSession;
import org.jdiameter.api.rx.ClientRxSession;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScenarioContextTest {

    @Test
    void test() {
        GxStack gx = mock(GxStack.class);
        RxStack rx = mock(RxStack.class);
        Scenario scenario = mock(Scenario.class);
        SessionProvider creator = mock(SessionProvider.class);
        List<AppRequestEvent> events = new ArrayList<>();

        // fill a state a bit
        Map<String, Object> state = new HashMap<>();
        state.put("dummy", 42);

        // mock setup
        when(gx.getRealm()).thenReturn("gxRealm");
        when(rx.getRealm()).thenReturn("rxRealm");
        when(gx.getServerUri()).thenReturn("gxUri");
        when(rx.getServerUri()).thenReturn("rxUri");

        // assertions
        ScenarioContext context = new ScenarioContext(scenario, creator, gx, rx, events, state);
        assertEquals(gx, context.getGxStack());
        assertEquals(rx, context.getRxStack());
        assertNull(context.getGxSession());
        assertNull(context.getRxSession());
        assertEquals("gxRealm", context.getGxRealm());
        assertEquals("rxRealm", context.getRxRealm());
        assertEquals("gxUri", context.getGxServerUri());
        assertEquals("rxUri", context.getRxServerUri());
        assertEquals(events, context.getReceivedEvents());
        assertEquals(state, context.getState());
    }

    @Test
    void test_sessionCreation() throws Exception {
        GxStack gx = mock(GxStack.class);
        RxStack rx = mock(RxStack.class);
        ClientGxSession gxSession = mock(ClientGxSession.class);
        ClientRxSession rxSession = mock(ClientRxSession.class);
        Scenario scenario = mock(Scenario.class);
        SessionProvider creator = mock(SessionProvider.class);
        List<AppRequestEvent> events = new ArrayList<>();
        Map<String, Object> state = new HashMap<>();

        // mock setup
        when(creator.createGxSession(null, scenario)).thenReturn(gxSession);
        when(creator.createRxSession(null, scenario)).thenReturn(rxSession);

        // assertions
        ScenarioContext context = new ScenarioContext(scenario, creator, gx, rx, events, state);
        assertNull(context.getGxSession());
        assertNull(context.getRxSession());
        assertEquals(gxSession, context.createGxSession());
        assertEquals(rxSession, context.createRxSession());
        assertEquals(gxSession, context.getGxSession());
        assertEquals(rxSession, context.getRxSession());
    }

}