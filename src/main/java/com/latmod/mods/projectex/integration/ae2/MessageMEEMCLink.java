package com.latmod.mods.projectex.integration.ae2;

import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MessageMEEMCLink implements IMessage {

    public int x, y, z;
    public int side;
    public int action; // 0 = setPriority, 1 = setAccessMode, 2 = setFilterMode, 3 = rebindOwner
    public int value;

    public MessageMEEMCLink() {
    }

    public MessageMEEMCLink(int x, int y, int z, int side, int action, int value) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
        this.action = action;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        side = buf.readByte();
        action = buf.readByte();
        value = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(side);
        buf.writeByte(action);
        buf.writeInt(value);
    }

    public static class Handler implements IMessageHandler<MessageMEEMCLink, IMessage> {
        @Override
        public IMessage onMessage(MessageMEEMCLink message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) return null;
            World world = player.worldObj;
            if (world == null) return null;

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileMEEMCLink) {
                TileMEEMCLink link = (TileMEEMCLink) te;
                applyAction(link, message.action, message.value, player);
            }

            return null;
        }

        private void applyAction(TileMEEMCLink tile, int action, int value, EntityPlayerMP player) {
            if (action == 0) {
                tile.setPriority(value);
            } else if (action == 1) {
                tile.setAccessMode(value);
            } else if (action == 2) {
                tile.setFilterMode(value);
            } else if (action == 3) {
                tile.setOwner(player);
            } else if (action == 4) {
                tile.setFilterPrecision(value);
            }
        }
    }
}
