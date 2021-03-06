package network.thunder.core.communication.objects.messages.impl.message.lightningestablish;

import com.google.common.base.Preconditions;
import network.thunder.core.communication.objects.messages.interfaces.message.lightningestablish.LNEstablish;
import network.thunder.core.database.objects.Channel;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;

import java.util.Arrays;

/**
 * Created by matsjerratsch on 03/12/2015.
 */
public class LNEstablishBMessage implements LNEstablish {
    public byte[] pubKeyEscape;
    public byte[] pubKeyFastEscape;
    public byte[] secretHashFastEscape;
    public byte[] revocationHash;
    public byte[] anchorHash;
    public long serverAmount;

    public LNEstablishBMessage (byte[] pubKeyEscape, byte[] pubKeyFastEscape, byte[] secretHashFastEscape, byte[] revocationHash, byte[] anchorHash, long
            serverAmount) {
        this.pubKeyEscape = pubKeyEscape;
        this.pubKeyFastEscape = pubKeyFastEscape;
        this.secretHashFastEscape = secretHashFastEscape;
        this.revocationHash = revocationHash;
        this.anchorHash = anchorHash;
        this.serverAmount = serverAmount;
    }

    @Override
    public void saveToChannel (Channel channel) {
        channel.setKeyClient(ECKey.fromPublicOnly(this.pubKeyEscape));
        channel.setKeyClientA(ECKey.fromPublicOnly(this.pubKeyFastEscape));
        channel.setAnchorSecretHashClient(this.secretHashFastEscape);
        channel.setAnchorRevocationHashClient(this.revocationHash);
        channel.setAmountClient(this.serverAmount);
        channel.setInitialAmountClient(this.serverAmount);
        channel.setAnchorTxHashClient(Sha256Hash.wrap(this.anchorHash));
    }

    @Override
    public void verify () {
        Preconditions.checkNotNull(pubKeyEscape);
        Preconditions.checkNotNull(pubKeyFastEscape);
        Preconditions.checkNotNull(secretHashFastEscape);
        Preconditions.checkNotNull(revocationHash);
        Preconditions.checkNotNull(anchorHash);
    }

    @Override
    public String toString () {
        return "LNEstablishBMessage{" +
                "serverAmount=" + serverAmount +
                ", anchorHash=" + Arrays.toString(anchorHash) +
                '}';
    }
}
