package network.thunder.core.communication.objects.messages.impl.message.lightningestablish;

import com.google.common.base.Preconditions;
import network.thunder.core.communication.objects.messages.interfaces.message.lightningestablish.LNEstablish;
import network.thunder.core.database.objects.Channel;
import org.bitcoinj.crypto.TransactionSignature;

/**
 * Created by matsjerratsch on 03/12/2015.
 */
public class LNEstablishDMessage implements LNEstablish {
    public byte[] signatureEscape;
    public byte[] signatureFastEscape;

    public LNEstablishDMessage (byte[] signatureEscape, byte[] signatureFastEscape) {
        this.signatureEscape = signatureEscape;
        this.signatureFastEscape = signatureFastEscape;
    }

    @Override
    public void saveToChannel (Channel channel) {
        channel.setEscapeTxSig(TransactionSignature.decodeFromBitcoin(this.signatureEscape, true));
        channel.setFastEscapeTxSig(TransactionSignature.decodeFromBitcoin(this.signatureFastEscape, true));
    }

    @Override
    public void verify () {
        Preconditions.checkNotNull(signatureEscape);
        Preconditions.checkNotNull(signatureFastEscape);
    }

    @Override
    public String toString () {
        return "LNEstablishDMessage{}";
    }
}
