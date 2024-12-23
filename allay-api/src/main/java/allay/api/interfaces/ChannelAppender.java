package allay.api.interfaces;

import allay.api.network.packet.Packet;

public interface ChannelAppender {

    void sendPacket(Packet packet);

}
