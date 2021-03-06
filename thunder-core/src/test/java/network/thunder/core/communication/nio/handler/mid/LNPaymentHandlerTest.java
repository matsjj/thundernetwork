package network.thunder.core.communication.nio.handler.mid;

import io.netty.channel.embedded.EmbeddedChannel;
import network.thunder.core.communication.Message;
import network.thunder.core.communication.nio.handler.ProcessorHandler;
import network.thunder.core.communication.objects.lightning.subobjects.PaymentData;
import network.thunder.core.communication.objects.messages.impl.message.lnpayment.LNPaymentAMessage;
import network.thunder.core.communication.objects.messages.impl.message.lnpayment.LNPaymentBMessage;
import network.thunder.core.communication.objects.messages.impl.message.lnpayment.LNPaymentCMessage;
import network.thunder.core.communication.objects.messages.impl.message.lnpayment.LNPaymentDMessage;
import network.thunder.core.communication.objects.messages.interfaces.factories.ContextFactory;
import network.thunder.core.communication.objects.messages.interfaces.factories.LNPaymentMessageFactory;
import network.thunder.core.communication.objects.messages.interfaces.helper.LNPaymentHelper;
import network.thunder.core.communication.objects.subobjects.PaymentSecret;
import network.thunder.core.communication.processor.implementations.lnpayment.LNPaymentProcessorImpl;
import network.thunder.core.communication.processor.interfaces.lnpayment.LNPaymentLogic;
import network.thunder.core.communication.processor.interfaces.lnpayment.LNPaymentProcessor;
import network.thunder.core.database.DBHandler;
import network.thunder.core.etc.*;
import network.thunder.core.mesh.NodeClient;
import network.thunder.core.mesh.NodeServer;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by matsjerratsch on 02/11/2015.
 */
public class LNPaymentHandlerTest {

    EmbeddedChannel channel12;
    EmbeddedChannel channel21;

    NodeServer nodeServer1 = new NodeServer();
    NodeServer nodeServer2 = new NodeServer();

    NodeClient node1 = new NodeClient(nodeServer2);
    NodeClient node2 = new NodeClient(nodeServer1);

    LNPaymentProcessorImpl processor12;
    LNPaymentProcessorImpl processor21;

    DBHandler dbHandler1 = new LNPaymentDBHandlerMock();
    DBHandler dbHandler2 = new LNPaymentDBHandlerMock();

    ContextFactory contextFactory12 = new MockLNPaymentContextFactory(nodeServer1, dbHandler1);
    ContextFactory contextFactory21 = new MockLNPaymentContextFactory(nodeServer2, dbHandler2);

    @Before
    public void prepare () throws PropertyVetoException, SQLException {
        node1.isServer = false;
        node2.isServer = true;

        this.node1.name = "LNPayment12";
        this.node2.name = "LNPayment21";

        processor12 = new LNPaymentProcessorImpl(contextFactory12, dbHandler1, this.node1);
        processor21 = new LNPaymentProcessorImpl(contextFactory21, dbHandler2, this.node2);

        channel12 = new EmbeddedChannel(new ProcessorHandler(processor12, "LNPayment12"));
        channel21 = new EmbeddedChannel(new ProcessorHandler(processor21, "LNPayment21"));

        Message m = (Message) channel21.readOutbound();
        assertNull(m);

    }

    public void after () {
        channel12.checkException();
        channel21.checkException();
    }

    @Test
    public void fullExchangeWithNoDisturbanceWithinTimeframe () throws NoSuchProviderException, NoSuchAlgorithmException, InterruptedException {
        processor12.makePayment(getMockPaymentData());
        Thread.sleep(200);

        exchangeMessages(channel12, channel21, LNPaymentAMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentBMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentCMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentCMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentDMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentDMessage.class);

        assertNull(channel12.readOutbound());
        assertNull(channel21.readOutbound());

        after();
    }

    @Test
    public void exchangeWithDelayShouldRestart () throws NoSuchProviderException, NoSuchAlgorithmException, InterruptedException {
        processor12.makePayment(getMockPaymentData());
        Thread.sleep(2000);

        exchangeMessages(channel12, channel21);
        exchangeMessages(channel21, channel12);
        Message message = (Message) channel12.readOutbound();

        Thread.sleep(LNPaymentProcessor.TIMEOUT_NEGOTIATION + 5000);

        System.out.println(message);

        channel21.writeInbound(message);
        assertNull(channel21.readOutbound());

        exchangeMessages(channel12, channel21, LNPaymentAMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentBMessage.class);

        after();
    }

    @Test
    public void exchangeWithOtherPartyStartingOwnExchange () throws NoSuchProviderException, NoSuchAlgorithmException, InterruptedException {
        processor12.makePayment(getMockPaymentData());
        Thread.sleep(200);

        exchangeMessages(channel12, channel21, LNPaymentAMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentBMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentCMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentCMessage.class);

        Message message = (Message) channel12.readOutbound();
        assertThat(message, instanceOf(LNPaymentDMessage.class));

        System.out.println("abort..");
        Thread.sleep(200);

        processor21.makePayment(getMockPaymentData());
        processor21.abortCurrentExchange();
        Thread.sleep(500);

        exchangeMessages(channel21, channel12, LNPaymentAMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentBMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentCMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentCMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentDMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentDMessage.class);

        after();
    }

    @Test
    public void exchangeConcurrentWithOneThrowingHigherDice () throws NoSuchProviderException, NoSuchAlgorithmException, InterruptedException {

        processor12.makePayment(getMockPaymentData());
        processor21.makePayment(getMockPaymentData());
        Thread.sleep(200);

        LNPaymentAMessage message1 = (LNPaymentAMessage) channel12.readOutbound();
        assertThat(message1, instanceOf(LNPaymentAMessage.class));

        LNPaymentAMessage message2 = (LNPaymentAMessage) channel21.readOutbound();
        assertThat(message2, instanceOf(LNPaymentAMessage.class));

        int dice1 = message1.dice;
        int dice2 = message2.dice;

        channel12.writeInbound(message2);
        channel21.writeInbound(message1);

        if (dice2 > dice1) {
            EmbeddedChannel channel3 = channel12;
            channel12 = channel21;
            channel21 = channel3;
        }

        exchangeMessages(channel21, channel12, LNPaymentBMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentCMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentCMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentDMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentDMessage.class);

        Thread.sleep(200);

        exchangeMessages(channel21, channel12, LNPaymentAMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentBMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentCMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentCMessage.class);
        exchangeMessages(channel21, channel12, LNPaymentDMessage.class);
        exchangeMessages(channel12, channel21, LNPaymentDMessage.class);

        after();
    }

    public void exchangePayment (EmbeddedChannel from, EmbeddedChannel to) {
        exchangeMessages(from, to, LNPaymentAMessage.class);
        exchangeMessages(to, from, LNPaymentBMessage.class);
        exchangeMessages(from, to, LNPaymentCMessage.class);
        exchangeMessages(to, from, LNPaymentCMessage.class);
        exchangeMessages(from, to, LNPaymentDMessage.class);
        exchangeMessages(to, from, LNPaymentDMessage.class);
    }

    public static void exchangeMessages (EmbeddedChannel from, EmbeddedChannel to) {
        Object message = from.readOutbound();
        System.out.println(message);
        if (message != null) {
            to.writeInbound(message);
        }
    }

    public static void exchangeMessages (EmbeddedChannel from, EmbeddedChannel to, Class expectedMessage) {
        Object message = from.readOutbound();
        assertThat(message, instanceOf(expectedMessage));
        if (message != null) {
            to.writeInbound(message);
        }
    }

    public static void exchangeMessagesDuplex (EmbeddedChannel from, EmbeddedChannel to) {
        exchangeMessages(from, to);
        exchangeMessages(to, from);
    }

    public PaymentData getMockPaymentData () {
        PaymentData paymentData = new PaymentData();
        paymentData.sending = true;
        paymentData.amount = 10000;
        paymentData.secret = new PaymentSecret(Tools.getRandomByte(20));

        return paymentData;
    }

    public void connectChannel (EmbeddedChannel from, EmbeddedChannel to) {
        new Thread(new Runnable() {
            @Override
            public void run () {
                while (true) {
                    exchangeMessagesDuplex(from, to);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    class MockLNPaymentContextFactory extends MockContextFactory {

        public MockLNPaymentContextFactory (NodeServer node, DBHandler dbHandler) {
            super(node, dbHandler);
        }

        @Override
        public LNPaymentLogic getLNPaymentLogic () {
            return new MockLNPaymentLogic(getLNPaymentMessageFactory());
        }

        @Override
        public LNPaymentMessageFactory getLNPaymentMessageFactory () {
            return new LNPaymentMessageFactoryMock();
        }

        @Override
        public LNPaymentHelper getPaymentHelper () {
            return new MockLNPaymentHelper();
        }
    }

}