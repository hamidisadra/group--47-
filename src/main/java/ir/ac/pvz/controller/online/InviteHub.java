package ir.ac.pvz.controller.online;

import network.client.GameClient;
import network.protocol.MessageType;
import network.protocol.NetworkMessage;

import java.util.concurrent.atomic.AtomicReference;

public final class InviteHub {
    private static final AtomicReference<NetworkMessage> INVITE =
        new AtomicReference<>();
    private static final AtomicReference<NetworkMessage> START =
        new AtomicReference<>();
    private static final AtomicReference<NetworkMessage> DECLINED =
        new AtomicReference<>();

    private static boolean installed;

    private InviteHub() {
    }

    public static synchronized void install(GameClient client) {
        if (installed) {
            return;
        }

        installed = true;
        client.onPush(MessageType.MATCH_INVITE, INVITE::set);
        client.onPush(MessageType.MATCH_STARTED, START::set);
        client.onPush(MessageType.MATCH_DECLINED, DECLINED::set);
    }

    public static NetworkMessage pollInvite() {
        return INVITE.getAndSet(null);
    }

    public static NetworkMessage pollStart() {
        return START.getAndSet(null);
    }

    public static NetworkMessage pollDeclined() {
        return DECLINED.getAndSet(null);
    }

    public static void reset() {
        INVITE.set(null);
        START.set(null);
        DECLINED.set(null);
    }
}
