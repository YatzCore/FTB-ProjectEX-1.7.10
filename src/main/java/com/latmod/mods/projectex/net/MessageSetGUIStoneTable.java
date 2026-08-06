package com.latmod.mods.projectex.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageSetGUIStoneTable implements IMessage, IMessageHandler<MessageSetGUIStoneTable, IMessage> {
    public int progress;
    public int maxProgress;

    public MessageSetGUIStoneTable() {}

    public MessageSetGUIStoneTable(int progress, int maxProgress) {
        this.progress = progress;
        this.maxProgress = maxProgress;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.progress = buf.readInt();
        this.maxProgress = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.progress);
        buf.writeInt(this.maxProgress);
    }

    @Override
    public IMessage onMessage(MessageSetGUIStoneTable message, MessageContext ctx) {
        return null;
    }
}
