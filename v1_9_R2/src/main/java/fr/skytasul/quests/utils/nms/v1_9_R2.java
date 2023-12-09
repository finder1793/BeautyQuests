package fr.skytasul.quests.utils.nms;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_9_R2.EnumChatFormat;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketDataSerializer;
import net.minecraft.server.v1_9_R2.PacketPlayOutCustomPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class v1_9_R2 extends NMS{
	
	@Override
	public void openBookInHand(Player p) {
		ByteBuf buf = Unpooled.buffer(256);
		buf.setByte(0, (byte) 0);
		buf.writerIndex(1);

		PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public double entityNameplateHeight(Entity en){
		return ((CraftEntity) en).getHandle().length;
	}

	public Object getIChatBaseComponent(String text){
		return IChatBaseComponent.ChatSerializer.b(text);
	}

	public Object getEnumChatFormat(int value){
		return EnumChatFormat.a(value);
	}
	
}