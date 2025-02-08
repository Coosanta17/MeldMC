package net.coosanta.totalityloader.server;

import net.querz.nbt.tag.ByteTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.StringTag;

import java.util.Base64;

public class ServerEntry {
    private byte hidden;
    private byte[] icon;
    private String ip;
    private String name;
    private ServerMetadata serverStat;


    public ServerEntry(ServerEntry serverEntry) {
        this.hidden = serverEntry.hidden;
        this.icon = serverEntry.icon.clone();
        this.ip = serverEntry.ip;
        this.name = serverEntry.name;
        this.serverStat = serverEntry.serverStat;
    }

    public ServerEntry(Byte hidden, String icon, String ip, String name) {
        this.hidden = hidden;
        this.icon = decodeIcon(icon);
        this.ip = ip;
        this.name = name;
    }

    public ServerEntry(ByteTag hidden, StringTag icon, StringTag ip, StringTag name) {
        this.hidden = hidden.asByte();
        this.icon = decodeIcon(icon.getValue());
        this.ip = ip.getValue();
        this.name = name.getValue();
    }

    public ServerEntry(CompoundTag nbt) {
        if (!validateNbt(nbt)) throw new IllegalArgumentException("servers.dat invalid - missing keys!");

        try {
            this.hidden = nbt.getByteTag("hidden").asByte();
            this.icon = decodeIcon(nbt.getStringTag("icon").getValue());
            this.ip = nbt.getStringTag("ip").getValue();
            this.name = nbt.getStringTag("name").getValue();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("servers.dat keys are either null or wrong type!", e);
        }
    }

    private boolean validateNbt(CompoundTag nbt) {
        if (nbt == null) return false;
        return nbt.containsKey("hidden") && nbt.containsKey("icon") && nbt.containsKey("ip") && nbt.containsKey("name");
    }

    private static byte[] decodeIcon(String iconBase64) {
        byte[] decodedIcon;
        try {
            decodedIcon = Base64.getDecoder().decode(iconBase64);
        } catch (IllegalArgumentException e) {
            decodedIcon = null; // TODO: Set Default icon.
            throw new IllegalArgumentException("Invalid Data for icon", e);
        }

        return decodedIcon;
    }

    public Byte getHidden() {
        return hidden;
    }

    public String getIp() {
        return ip;
    }

    public byte[] getIcon() {
        return icon.clone(); // Prevents modification of icon.
    }

    public String getName() {
        return name;
    }

    public ServerMetadata getServerStat() {
        return serverStat;
    }

    public void setHidden(byte hidden) {
        this.hidden = hidden;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServerStat(ServerMetadata serverStat) {
        this.serverStat = serverStat;
    }
}
